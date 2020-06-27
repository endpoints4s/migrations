package fix

import endpoints4s.algebra.Endpoints
import endpoints4s.algebra.Tag

trait MyEndpoints extends Endpoints with endpoints4s.algebra.JsonEntities {
  val foo = endpoint(
    get(path / "foo"),
    ok(emptyResponse),
    docs = EndpointDocs().withSummary(Some("Foo")).withDescription(Some("Foo resource")).withTags(List(Tag("resources"))).withCallbacks(Map("bar" -> Map("baz" -> CallbackDocs(Post, textRequest, ok(textResponse)).withRequestDocs(requestDocs = Some("webhook")))))
  )

  object endpoints {
    def bar(): Unit = ()
  }
  endpoints.bar()
}

import sbt._
import sbt.Keys._

object Build {
  val endpointsVersion = "1.0.0"

  libraryDependencies ++= Seq(
    "org.endpoints4s" %% "algebra" % endpointsVersion,
    "org.endpoints4s" %% "openapi" % endpointsVersion,
    "org.endpoints4s" %% "json-schema-generic" % "1.0.0",
    "org.julienrf" %% "play-json-derived-codecs" % "6.0.0"
  )
}
