import org.scalajs.linker.interface.ESVersion

val circeVersion = "0.14.5"
val http4sVersion = "0.23.33"
val logbackVersion = "1.5.32"

lazy val backend = (project in file("./backend"))
  .dependsOn(shared.jvm)
  .settings(
    organization := "io.sommers.twodee.web.backend",
    name := "TwoDeeWeb Backend",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.3.7",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "org.fusesource.jansi" % "jansi" % "2.4.2"
    ) ++ catsDependencies ++ configDependencies ++ databaseDependencies ++ http4sDependencies ++ oauthDependencies
  )

lazy val frontend = (project in file("./frontend"))
  .dependsOn(shared.js)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    organization := "io.sommers.twodee.web.front",
    name := "TwoDeeWeb Front",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.3.7",
    libraryDependencies ++= Seq(
      "org.http4s" %%% "http4s-client" % http4sVersion,
      "org.http4s" %%% "http4s-circe" % http4sVersion,
      "org.http4s" %%% "http4s-dom" % "0.2.12",
      "com.raquo" %%% "laminar" % "17.2.1",
      "com.raquo" %%% "waypoint" % "10.0.0-M1"
    ),
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withESFeatures(_.withESVersion(ESVersion.ES2021))
    },
    scalaJSUseMainModuleInitializer := true,
    watchSources := watchSources.value.filterNot { source =>
      source.base.getName.endsWith(".less") || source.base.getName.endsWith(".css")
    }
  )

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .in(file("./shared"))
  .settings(
    organization := "io.sommers.twodee.web.shared",
    name := "TwoDeeWeb Shared",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.3.7",
    libraryDependencies ++= Seq("io.circe" %%% "circe-core" % circeVersion)
  )
  .jvmSettings(
    libraryDependencies ++= List(
      // This dependency lets us put @JSExportAll and similar Scala.js
      // annotations on data structures shared between JS and JVM.
      // With this library, on the JVM, these annotations compile to
      // no-op, which is exactly what we need.
      "org.scala-js" %% "scalajs-stubs" % "1.1.0"
    )
  )

lazy val root = (project in file("."))
  .aggregate(backend, frontend, shared.js, shared.jvm)
  .settings(
    organization := "io.sommers.twodee.web",
    name := "TwoDeeWeb",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.3.7",
    libraryDependencies ++= Seq()
  )

val catsDependencies = Seq(
  "org.typelevel" %% "log4cats-slf4j" % "2.7.1"
)
val configDependencies = Seq(
  "is.cir" %% "ciris" % "3.12.0",
  "is.cir" %% "ciris-circe" % "3.12.0",
  "is.cir" %% "ciris-circe-yaml" % "3.12.0",
)
val databaseDependencies = Seq(
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC12",
  "org.tpolecat" %% "doobie-log4cats" % "1.0.0-RC12",
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC12",
  "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC12" % "test",
  "org.xerial" % "sqlite-jdbc" % "3.51.2.0"
)
val http4sDependencies = Seq(
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-generic" % "0.14.15",
  "org.http4s" %% "http4s-dsl" % http4sVersion
)
val oauthDependencies = Seq(
  "com.google.oauth-client" % "google-oauth-client" % "1.39.0",
  "com.google.auth" % "google-auth-library-oauth2-http" % "1.43.0"
)
