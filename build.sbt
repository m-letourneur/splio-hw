name := "splio-hw"

version := "0.1"

scalaVersion := "2.12.12"

lazy val AkkaVersion      = "2.6.8"
lazy val AkkaHttpVersion  = "10.2.0"
lazy val akkaDependencies = Seq(
	"com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
	"com.typesafe.akka" %% "akka-stream" % AkkaVersion,
	"com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
	"com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
	"io.spray" %% "spray-json" % "1.3.5"
	)
libraryDependencies ++= akkaDependencies

enablePlugins(JavaAppPackaging)

