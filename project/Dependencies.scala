import sbt._

object Dependencies {
  val akkaVersion = "2.6.15"
  val akkaHttpVersion = "10.2.6"

  val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

  val jacksonVersion = "2.12.4"
  val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
  val jacksonScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion

  val akkaManagement = "com.lightbend.akka.management" %% "akka-management" % "1.1.1"
  val akkaActorTyped = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
  val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

  val postgresDriver = "org.postgresql" % "postgresql" % "42.2.23"
  val persistencePostgres = "com.swissborg" %% "akka-persistence-postgres" % "0.5.0-M1"
  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion
  val jackson = "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion

  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.9" % Test
  val akkaActorTestkit = "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test
  val akkaPersistenceTestkit = "com.typesafe.akka" %% "akka-persistence-testkit" % akkaVersion % Test
}
