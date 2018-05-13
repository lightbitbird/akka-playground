package com.akka.http

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.akka.models.JsonSupport
import com.akka.streams.ClientGraph
import com.config.AkkaPlaygroundConfig

import scala.concurrent.{ExecutionContext, Future}

object ClientApi extends App with JsonSupport {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

//  ClientGraph.runFromSource.foreach(println)
  ClientGraph.runGitApiSource.foreach(println)
//  ClientGraph.runRestMultiSources.foreach(println)
}

trait RestClient extends AkkaPlaygroundConfig {
  def getUrl(name: String): Uri

  def request(uri: Uri): HttpRequest = {
    HttpRequest(method = HttpMethods.GET, uri)
  }

  def run(request: HttpRequest)(implicit system: ActorSystem): Future[HttpResponse] = {
    Http().singleRequest(request)
  }
}

object GitRestClinet extends RestClient {
  def getUrl(name: String): Uri = {
    Uri("https://" + config.getString("rest.api.url") + Uri(config.getString("rest.api.uri") + name))
  }

  //create one source for many request
  def createRestSources(names: Seq[String])
                       (implicit system: ActorSystem): Source[HttpRequest, NotUsed] = {
    Source[HttpRequest](names.map(n => GitRestClinet.request(GitRestClinet.getUrl(n))).toList)
  }

  //create multiple single sources
  def createSingleSources(names: Seq[String])
                         (implicit system: ActorSystem): Seq[Source[HttpRequest, NotUsed]] = {
    names.map(n => singleSource(n))
  }

  def singleSource(name: String)
                  (implicit system: ActorSystem): Source[HttpRequest, NotUsed] = {
    Source.single[HttpRequest](GitRestClinet.request(GitRestClinet.getUrl(name)))
  }
}

