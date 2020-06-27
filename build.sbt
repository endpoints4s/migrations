lazy val V = _root_.scalafix.sbt.BuildInfo
inThisBuild(
  List(
    organization := "org.endpoints4s",
    homepage := Some(url("https://github.com/endpoints4s")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "julienrf",
        "Julien Richard-Foy",
        "julien@richard-foy.fr",
        url("http://julien.richard-foy.fr")
      )
    ),
    scalaVersion := V.scala212,
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions ++= List(
      "-Yrangepos",
      "-P:semanticdb:synthetics:on"
    )
  )
)

skip in publish := true

lazy val `to-1_0_0-rules` = project.in(file("to-1.0.0/rules")).settings(
  name := "to-1.0.0",
  libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
)

lazy val `to-1_0_0-input` = project.in(file("to-1.0.0/input")).settings(
  skip in publish := true,
  libraryDependencies += "org.julienrf" %% "endpoints-algebra" % "0.15.0",
  sbtPlugin := true
)

lazy val `to-1_0_0-output` = project.in(file("to-1.0.0/output")).settings(
  skip in publish := true
)

lazy val `to-1_0_0-tests` = project.in(file("to-1.0.0/tests"))
  .settings(
    skip in publish := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
    (Compile / compile) :=
      (Compile / compile).dependsOn(`to-1_0_0-input` / Compile / compile).value,
    scalafixTestkitOutputSourceDirectories :=
      (`to-1_0_0-output` / Compile / sourceDirectories).value,
    scalafixTestkitInputSourceDirectories :=
      (`to-1_0_0-input` / Compile / sourceDirectories).value,
    scalafixTestkitInputClasspath :=
      (`to-1_0_0-input` / Compile / fullClasspath).value,
  )
  .dependsOn(`to-1_0_0-rules`)
  .enablePlugins(ScalafixTestkitPlugin)
