import sbt.Keys._
import com.typesafe.sbt.SbtNativePackager.autoImport.executableScriptName
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.stage
import Dependencies._

lazy val root =
  Project(id = "root", base = file("."))
    .settings(
      name := "root",
    )
    .withId("root")
    .settings(commonSettings)
    .aggregate(voicebot, domain, scripts, migrations)
    .enablePlugins(BuildInfoPlugin)

lazy val domain = appModule(moduleID = "domain")
  .settings(
    libraryDependencies ++= Seq(akkaHttpSprayJson)
  )

lazy val scripts = appModule(moduleID = "scripts")
  .settings(libraryDependencies ++= Seq(
    akkaPersistence,
    akkaActorTestkit,
    scalaTest,
    akkaPersistenceTestkit,
    jackson
  )
  )
  .dependsOn(domain)

lazy val voicebot = appModule(moduleID = "voicebot")
  .settings(
    dockerSettings,
    libraryDependencies ++= Seq(
      postgresDriver,
      persistencePostgres,
      akkaPersistence,
      jackson
    )
  )
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaAppPackaging)
  .dependsOn(domain)
  .dependsOn(scripts)

lazy val migrations = appModule(moduleID = "migrations")
  .settings(
    libraryDependencies ++= Seq(postgresDriver),
    flywayUrl := "jdbc:postgresql://localhost:5432/voicebot",
    flywayUser := "docker",
    flywayPassword := "docker",
    flywayLocations += "db/migration"
  )
  .settings(dockerSettings)
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(FlywayPlugin)

lazy val commonSettings = Seq(
  scalaVersion := "2.13.4",
  scalacOptions := Seq(
    "-unchecked",
    "-deprecation",
    "-encoding", "utf8",
    "-feature",
    "-language:implicitConversions"
  ),
  libraryDependencies ++= Seq(
    akkaHttp,
    akkaActorTyped,
    akkaHttpSprayJson,
    logback,
    jacksonDatabind,
    jacksonScala
  )
)

lazy val dockerSettings = Seq(
  Docker / version  := "latest",
  dockerBaseImage := "openjdk:11-jre-slim",
  dockerExposedPorts ++= Seq(8080),
  libraryDependencies ++= Seq(akkaManagement)
)

def appModule(moduleID: String): Project =
  Project(id = moduleID, base = file(moduleID))
    .settings(name := moduleID)
    .withId(moduleID)
    .settings(commonSettings)
