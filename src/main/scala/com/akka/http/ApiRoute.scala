package com.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import akka.stream._
import com.akka.models.{SearchKeys, JsonSupport}
import com.akka.streams.ClientGraph

import scala.concurrent.ExecutionContext

class ApiRoute(implicit system: ActorSystem,
               ec: ExecutionContext,
               materializer: ActorMaterializer) extends Directives with JsonSupport {

  val handler = ExceptionHandler {
    case e: Exception => complete((StatusCodes.BadRequest, s"You've got an error: ${e.getMessage}"))
    case _: Throwable => complete((StatusCodes.BadRequest, "You've got an error!"))
  }

  def route: Route = {
    pathPrefix("api") {
      handleExceptions(handler) {
        get {
          path("stream") {
            complete(ToResponseMarshallable(ClientGraph.runWikiApiSource(SearchKeys.wikis)))
            //complete(ToResponseMarshallable(ClientGraph.runGitApiSource(SearchKeys.names)))
            //complete(ToResponseMarshallable(ClientGraph.runRestMultiSources(SearchKeys.names)))
            //complete(ToResponseMarshallable(ClientGraph.runOpenBdApiSource(SearchKeys.books)))
          }
        }
      }
    }
  }

}
