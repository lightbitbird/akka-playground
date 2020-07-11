name := "akka-playground"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= {
  val akkaHttp = "10.1.12"
  val akkaStream = "2.6.7"
  val scalaTest = "3.2.0"

  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaHttp,
    "com.typesafe.akka" %% "akka-http" % akkaHttp,
    "com.typesafe.akka" %% "akka-stream" % akkaStream,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttp,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttp,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.akka" %% "akka-slf4j" % "2.6.7",
    "org.slf4j" % "log4j-over-slf4j" % "1.7.25",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "org.scalatest"     %% "scalatest"  % scalaTest % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % Test
  )
}
