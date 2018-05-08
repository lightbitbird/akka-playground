package com.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import com.akka.models.JsonSupport

import scala.concurrent.ExecutionContext

class ApiRoute(implicit system: ActorSystem,
               ec: ExecutionContext,
               materializer: ActorMaterializer) extends Directives with JsonSupport {

  def route: Route = {
    pathPrefix("api") {
      get {
        path("stream") {
          complete(ToResponseMarshallable(ClientApi.runFromSource))
//          complete(ToResponseMarshallable(""))
        }
      }
    }
  }
}
