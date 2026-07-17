import org.scalajs.linker.interface.ESVersion

val circeVersion = "0.14.16"
val http4sVersion = "0.23.34"
val logbackVersion = "1.5.37"
val sttpVersion = "4.0.19"
val calibanVersion = "3.1.4"

lazy val backend = (project in file("./backend"))
  .dependsOn(shared.jvm)
  .settings(
    organization := "io.sommers.twodee.web.backend",
    name := "TwoDeeWeb Backend",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.3.7",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "org.fusesource.jansi" % "jansi" % "2.4.3"
    ) ++ catsDependencies ++ configDependencies ++ databaseDependencies ++ http4sDependencies ++ googleDependencies ++
      calibanDependencies
  )

lazy val frontend = (project in file("./frontend"))
  .dependsOn(shared.js)
  .enablePlugins(ScalaJSPlugin, ScalablyTypedConverterPlugin)
  .settings(
    organization := "io.sommers.twodee.web.front",
    name := "TwoDeeWeb Front",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.3.7",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client4" %%% "core" % sttpVersion,
      "com.softwaremill.sttp.client4" %%% "circe" % sttpVersion,
      "org.scala-js" %%% "scala-js-macrotask-executor" % "1.1.1",
      "com.raquo" %%% "laminar" % "17.2.1",
      "com.raquo" %%% "waypoint" % "10.0.0-M1",
      "dev.laminext" %%% "fetch-circe" % "0.17.1"
    ),
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.CommonJSModule)
        .withESFeatures(_.withESVersion(ESVersion.ES2021))
    },
    scalaJSUseMainModuleInitializer := true,
    watchSources := watchSources.value.filterNot { source =>
      source.base.getName.endsWith(".less") || source.base.getName
        .endsWith(".css")
    },
    Compile / npmDependencies ++= Seq(
      "@types/google.accounts" -> "0.0.18"
    )
  )

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .in(file("./shared"))
  .settings(
    organization := "io.sommers.twodee.web.shared",
    name := "TwoDeeWeb Shared",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.3.7",
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion
    )
  )
  .jvmSettings(
    libraryDependencies ++= List(
      "org.scala-js" %% "scalajs-stubs" % "1.1.0"
    )
  )

lazy val simplyDoom = (project in file("simply_doom"))
  .settings(
    organization := "io.sommers.twodee.web.simplydoom",
    name := "TwoDeeWeb Simply Doom",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.3.7",
    libraryDependencies ++= logbackDependencies ++ http4sDependencies ++ databaseDependencies ++ configDependencies ++
      circeDependencies ++ googleDependencies ++ Seq(
        "com.google.oauth-client" % "google-oauth-client-jetty" % "1.39.0",
        "io.chrisdavenport" %% "mules" % "0.7.0"
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
  "org.typelevel" %% "log4cats-slf4j" % "2.8.0"
)
val configDependencies = Seq(
  "is.cir" %% "ciris" % "3.15.0",
  "is.cir" %% "ciris-circe" % "3.15.0",
  "is.cir" %% "ciris-circe-yaml" % "3.15.0"
)
val databaseDependencies = Seq(
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC12",
  "org.tpolecat" %% "doobie-log4cats" % "1.0.0-RC12",
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC12",
  "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC12" % "test",
  "org.xerial" % "sqlite-jdbc" % "3.53.2.0"
)
val http4sDependencies = Seq(
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "dev.profunktor" %% "http4s-jwt-auth" % "2.0.15"
)
val circeDependencies = Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)
val googleDependencies = Seq(
  "com.google.api-client" % "google-api-client" % "2.9.0",
  "com.google.apis" % "google-api-services-sheets" % "v4-rev20260610-2.0.0"
)

val calibanDependencies = Seq(
  "com.github.ghostdogpr" %% "caliban" % calibanVersion,
  "com.github.ghostdogpr" %% "caliban-http4s" % calibanVersion
)

val logbackDependencies = Seq(
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.fusesource.jansi" % "jansi" % "2.4.3"
)
