package fix

import scalafix.v1._
import scala.meta._

class To_1_0_0 extends SemanticRule("To_1_0_0") {

  override def fix(implicit doc: SemanticDocument): Patch = {
    updateEndpointsPackage +
    updateEndpointDocsConstructor +
    updateModuleID(doc.tree)
  }

  def updateEndpointsPackage(implicit doc: SemanticDocument): Patch = {
    val endpointsPackage = SymbolMatcher.exact("endpoints/")
    doc.tree.collect {
      case endpointsPackage(tree @ Name(_)) => Patch.replaceTree(tree, "endpoints4s")
    }.asPatch
  }

  def updateEndpointDocsConstructor(implicit doc: SemanticDocument): Patch = {
    val endpointDocsCtor = SymbolMatcher.exact("endpoints/algebra/EndpointsWithCustomErrors#EndpointDocs.")
    doc.tree.collect {
      case tree @ Term.Apply(endpointDocsCtor(Name(_)), args) =>
        val parameterNames =
          List("summary", "description", "tags", "callbacks", "deprecated")
        val updatedArgs =
          args.zip(parameterNames).map {
            case (Term.Assign(Term.Name(name), arg), _) => name -> arg
            case (arg: Term.Apply, name)                => name -> arg
          }.map { case (name, arg) =>
            val adaptedArg =
              (name, arg) match {
                case ("tags", Term.Apply(Term.Name("List"), tagArgs)) =>
                  tagArgs.map(tagArg => s"Tag(${tagArg.syntax})")
                case ("callbacks", callbacksArg) => updateCallbacksArg(callbacksArg)
                case _ => arg.syntax
              }
            s".with${name.head.toUpper}${name.tail}($adaptedArg)"
          }
        val maybeAddImport =
          if (updatedArgs.exists(_.startsWith(".withTags"))) Some(Patch.addGlobalImport(Symbol("endpoints4s/algebra/Tag.")))
          else None
        Patch.replaceTree(
          tree,
          "EndpointDocs()" + updatedArgs.mkString
        ) + maybeAddImport
    }.asPatch
  }

  def updateCallbacksArg(arg: Tree): String = {
    arg match {
      case Term.Apply(Term.Name("Map"), callbackArgs) =>
        callbackArgs.map {
          case Term.ApplyInfix(event, Term.Name("->"), _, urls) =>
            val adaptedUrls = urls.head match {
              case Term.Apply(Term.Name("Map"), urlArgs) =>
                urlArgs.map {
                  case Term.ApplyInfix(url, Term.Name("->"), _, callbackDocs) =>
                    val adaptedCallbackDocs = callbackDocs.head match {
                      case Term.Apply(Term.Name("CallbackDocs"), args) =>
                        def namedRequestDocsArg: PartialFunction[Term, Term] = {
                          case Term.Assign(Term.Name("requestDocs"), arg) => arg
                        }
                        val maybeRequestDocsArg =
                          if (args.size == 4) Some(args.last)
                          else args.collectFirst(namedRequestDocsArg)
                        maybeRequestDocsArg match {
                          case None => callbackDocs.head.syntax
                          case Some(requestDocsArg) =>
                            val filteredArgs = args.filterNot(namedRequestDocsArg.isDefinedAt).take(3)
                            q"CallbackDocs(..${filteredArgs}).withRequestDocs(${requestDocsArg})".syntax
                        }
                      case arg => arg.syntax
                    }
                    s"${url.syntax} -> ${adaptedCallbackDocs}"
                  case arg => arg.syntax
                }.mkString("Map(", ", ", ")")
              case arg => arg.syntax
            }
            s"${event.syntax} -> ${adaptedUrls}"
          case arg => arg.syntax
        }.mkString("Map(", ", ", ")")
      case arg => arg.syntax
    }
  }

  def updateModuleID(docTree: Tree): Patch = {
    docTree.collect {
      case q"$organization %%  $artifact % $revision" => updateModuleID(docTree, organization, artifact, revision)
      case q"$organization %%% $artifact % $revision" => updateModuleID(docTree, organization, artifact, revision)
    }.asPatch
  }

  private val updatedRevions = collection.mutable.Set.empty[String]

  def updateModuleID(docTree: Tree, organization: Tree, artifact: Tree, revision: Tree): Patch = {
    (organization, artifact) match {
      case (Lit.String("org.julienrf"), Lit.String(id)) if id.startsWith("endpoints-") =>
        val updateOrganization =
          Patch.replaceTree(organization, "\"org.endpoints4s\"")
        val updateArtifact =
          Patch.replaceTree(artifact, "\"" + id.stripPrefix("endpoints-") + "\"")
        val updateRevision =
          revision match {
            case Lit.String(_) => Patch.replaceTree(revision, "\"1.0.0\"")
            case Term.Name(ident) =>
              if (!updatedRevions(ident)) {
                // Try to find the definition (in the same file only)
                docTree.collect {
                  case Defn.Val(_, List(Pat.Var(Term.Name(`ident`))), _, revision2) =>
                    updatedRevions += ident
                    Patch.replaceTree(revision2, "\"1.0.0\"")
                }.asPatch
              } else Patch.empty
            case _ => Patch.empty
          }

        updateOrganization +
        updateArtifact +
        updateRevision
      case _ => Patch.empty
    }
  }

}
