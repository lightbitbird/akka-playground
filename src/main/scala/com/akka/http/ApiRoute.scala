package com.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream._
import com.akka.models.JsonSupport
import com.akka.streams.ClientGraph

import scala.concurrent.ExecutionContext

class ApiRoute(implicit system: ActorSystem,
               ec: ExecutionContext,
               materializer: ActorMaterializer) extends Directives with JsonSupport {

  def route: Route = {
    pathPrefix("api") {
      get {
        path("stream") {
          //  val result = ClientGraph.runGitApiSource
          //  result.recover {
          //    case e: Exception => println(s"""exception -> ${e.getMessage}""")
          //    case t => println(s"""error -> ${t.getMessage}""")
          //  }
          //  result.onComplete(f => println(s"GitResultV2:::  ${f.get}"))

          complete(ToResponseMarshallable(ClientGraph.runGitApiSource))
//          complete(ToResponseMarshallable(ClientGraph.runRestSources))
        }
      }
    }
  }

}
