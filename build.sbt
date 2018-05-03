name := "akka-playground"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= {
  val akkaHttp = "10.1.1"
  val scalaTest = "3.0.1"
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaHttp,
    "com.typesafe.akka" %% "akka-http" % akkaHttp,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttp,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttp,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.akka" %% "akka-slf4j" % "2.5.12",
    "org.slf4j" % "log4j-over-slf4j" % "1.7.25",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
  )
}
