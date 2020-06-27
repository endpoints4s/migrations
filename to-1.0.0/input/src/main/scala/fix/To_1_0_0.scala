/*
rule = To_1_0_0
*/
package fix

import endpoints.algebra.Endpoints

trait MyEndpoints extends Endpoints with endpoints.algebra.JsonEntities {
  val foo = endpoint(
    get(path / "foo"),
    ok(emptyResponse),
    docs = EndpointDocs(
      Some("Foo"),
      description = Some("Foo resource"),
      tags = List("resources"),
      callbacks = Map(
        "bar" -> Map(
          "baz" -> CallbackDocs(
            Post,
            textRequest,
            ok(textResponse),
            requestDocs = Some("webhook")
          )
        )
      )
    )
  )

  object endpoints {
    def bar(): Unit = ()
  }
  endpoints.bar()
}

import sbt._
import sbt.Keys._

object Build {
  val endpointsVersion = "0.15.0"

  libraryDependencies ++= Seq(
    "org.julienrf" %% "endpoints-algebra" % endpointsVersion,
    "org.julienrf" %% "endpoints-openapi" % endpointsVersion,
    "org.julienrf" %% "endpoints-json-schema-generic" % "0.15.0",
    "org.julienrf" %% "play-json-derived-codecs" % "6.0.0"
  )
}
