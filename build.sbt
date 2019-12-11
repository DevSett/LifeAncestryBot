
import _root_.sbt.Keys._
scalaVersion := "2.12.6"
name := "LifeAncestryBot"
version := "0.1"

scalacOptions := List(
  "-encoding",
  "utf8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-target:jvm-1.8",
  "-language:_"
)

val akkaVersion       = "2.6.0"
val akkaHttpVersion   = "10.1.10"
val circeVersion      = "0.9.3"
val catsVersion       = "2.0.0"
val catsEffectVersion = "2.0.0"
val fs2Version        = "2.0.0"
val shapelessVersion  = "2.3.3"
val simulacrumVersion = "0.19.0"
val zioVersion        = "1.0.0-RC17"
val zioCatsVersion    = "2.0.0.0-RC8"
val zioMacrosVersion  = "0.5.0"

libraryDependencies += "org.scalactic"  %% "scalactic"   % "3.0.8"
libraryDependencies += "org.scalatest"  %% "scalatest"   % "3.0.8" % "test"
libraryDependencies += "org.scalacheck" %% "scalacheck"  % "1.14.1-RC2" % "test"
libraryDependencies += "org.mockito"    % "mockito-core" % "3.0.0" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor"       % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream"      % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-remote"      % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j"       % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http"        % akkaHttpVersion

libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit"      % akkaVersion     % "test"

libraryDependencies += "org.typelevel" %% "cats-core"        % catsVersion
libraryDependencies += "org.typelevel" %% "cats-free"        % catsVersion
libraryDependencies += "org.typelevel" %% "cats-effect"      % catsEffectVersion
libraryDependencies += "co.fs2"        %% "fs2-core"         % fs2Version
libraryDependencies += "co.fs2"        %% "fs2-io"           % fs2Version
libraryDependencies += "dev.zio"       %% "zio"              % zioVersion
libraryDependencies += "dev.zio"       %% "zio-streams"      % zioVersion
libraryDependencies += "dev.zio"       %% "zio-interop-cats" % zioCatsVersion

libraryDependencies += "io.circe" %% "circe-core"                   % circeVersion
libraryDependencies += "io.circe" %% "circe-generic"                % circeVersion
libraryDependencies += "io.circe" %% "circe-parser"                 % circeVersion
libraryDependencies += "io.circe" %% "circe-generic-extras"         % circeVersion

libraryDependencies += scalaOrganization.value % "scala-reflect"  % scalaVersion.value
libraryDependencies += scalaOrganization.value % "scala-compiler" % scalaVersion.value

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging"  % "3.9.2"
libraryDependencies += "ch.qos.logback"             % "logback-classic" % "1.2.3"

// Core with minimal dependencies, enough to spawn your first bot.
libraryDependencies += "com.bot4s" %% "telegram-core" % "4.4.0-RC2"

// Extra goodies: Webhooks, support for games, bindings for actors.
libraryDependencies += "com.bot4s" %% "telegram-akka" % "4.4.0-RC2"
libraryDependencies += "com.softwaremill.sttp" %% "async-http-client-backend-cats" % "1.7.2"

addCompilerPlugin("org.scalamacros" %% "paradise"  % "2.1.1" cross CrossVersion.patch)

libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.2"
libraryDependencies += "com.h2database" % "h2" % "1.4.199"
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.27.2.1"
