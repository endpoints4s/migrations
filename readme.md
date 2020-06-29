# Automatic rewrite rules for upgrading to new releases of endpoints4s 

## Migrating from 0.15.0 to 1.0.0

Add the scalafix plugin to your build definition. For sbt:

~~~ scala
// project/plugins.sbt
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.17")
~~~

Start the sbt shell and run the following commands:

~~~
// sbt shell
scalafixEnable
scalafix dependency:To_1_0_0@org.endpoints4s:to-1-0-0:1.0.0 --files .
~~~

The rule applies the following changes:

- rename references to package `endpoints` to `endpoints4s`,
- update imports of `endpoints.algebra.Codec` to `endpoints4s.Codec`,
- rewrite constructor calls of `EndpointDocs` and `CallbackDocs` to
  avoid deprecations,
- bump version number in build dependencies from `0.15.0` to `1.0.0`,
- update modules organization from `org.julienrf` to `org.endpoints4s`,
- update artifact names to remove the `endpoints-` prefix.

Note that the rules sometimes overlap or might be incomplete. You will
probably have to do some manual edits.

## Contributing

To develop rule:

```
sbt ~to-1_0_0-tests/test
# edit to-1.0.0/rules/src/main/scala/fix/To_1_0_0.scala
```
