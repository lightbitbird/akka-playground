package com.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import akka.stream.ActorMaterializer
import com.akka.models.{GitHubV2Entity, JsonSupport}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContext

object ClientApi extends App with JsonSupport {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  val config: Config = innerConfig

  val user = "lightbitbird"
  val uri = Uri("https://" + config.getString("rest.api.url") + Uri(config.getString("rest.api.uri") + user))

  println("https://" + config.getString("rest.api.url") + Uri(config.getString("rest.api.uri") + user))

  val request: HttpRequest = HttpRequest(method = HttpMethods.GET, uri)
  val response = Http().singleRequest(request)
//  val result = response.flatMap(res => GitHubEntity.unmarshal(res.entity)(ec, materializer))
////  val result = response.flatMap(res => Unmarshal(res.entity).to[GitResult])
//  result.recover {
//    case e: Exception => println(s"""exception -> ${e.getMessage}""")
//    case t => println(s"""error -> ${t.getMessage}""")
//  }
//  result.onComplete(f => println(s"GitResult:::  ${f.get}"))

  val result2 = response.flatMap(res => GitHubV2Entity.unmarshal(res.entity))
  result2.recover {
    case e: Exception => println(s"""exception -> ${e.getMessage}""")
    case t => println(s"""error -> ${t.getMessage}""")
  }
  result2.onComplete(f => println(s"GitResultV2:::  ${f.get}"))

  private def innerConfig: Config = {
    val env = System.getProperty("DEVELOP", "akka-playground")
    val default = ConfigFactory.load()
    default.hasPath(env) match {
      case true => default.getConfig(env).withFallback(default)
      case false => default
    }
  }
}
