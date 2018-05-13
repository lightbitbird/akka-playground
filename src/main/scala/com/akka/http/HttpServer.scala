package com.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try

trait Configuration {
  def config: Config
}

trait AkkaHttpConfig extends Configuration {
  val config: Config = innerConfig

  private def innerConfig: Config = {
    val env = System.getProperty("DEVELOP", "elastic")
    val default = ConfigFactory.load()

    default.hasPath(env) match {
      case true => default.getConfig(env).withFallback(default)
      case false => default
    }
  }
}

object HttpServer extends App with AkkaHttpConfig {
  implicit lazy val system = ActorSystem("kafka-producer-api")
  implicit lazy val materializer = ActorMaterializer(ActorMaterializerSettings(system)
    .withInputBuffer(initialSize = 5, maxSize = 16))
  implicit val ec = system.dispatcher

  val host = Try(config.getString("http.host")).getOrElse("127.0.0.1")
  val port = Try(config.getInt("http.port")).getOrElse(5000)

  def startApp = {
    Http().bindAndHandle(new ApiRoute().route, host, port)
  }

  startApp
}

