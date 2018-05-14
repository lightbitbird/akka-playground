package com.akka.http

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.akka.models.{JsonSupport, OpenBdEntity, SearchKeys, Summary}
import com.akka.streams.ClientGraph
import com.config.AkkaPlaygroundConfig

import scala.concurrent.{ExecutionContext, Future}

object ClientApi extends App with JsonSupport {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  ClientGraph.runFromSource.foreach(println)

//  ClientGraph.runRestMultiSources(names).foreach(println)
//  val result = ClientGraph.runGitApiSource(SearchKeys.names)
//  val result = ClientGraph.runWikiApiSource(SearchKeys.wikis)
  val result = ClientGraph.runWikiMultiSources(SearchKeys.wikis)
//  val result = ClientGraph.runOpenBdApiSource(SearchKeys.books)
  result.recover {
    case e: Throwable => println(s"Error: ${e.getMessage}")
    case _ => println(s"Unexpected Error")
  }
  result.onComplete(println)
//  Source(List.empty[List[OpenBdEntity]]).via(Flow[List[OpenBdEntity]].mapConcat(o => o.map(a => a.summary))).runWith(Sink.seq[Summary])
}

trait RestClient extends AkkaPlaygroundConfig {
  def getUrl(name: String): Uri

  def request(uri: Uri): HttpRequest = {
    HttpRequest(method = HttpMethods.GET, uri)
  }

  def run(request: HttpRequest)(implicit system: ActorSystem): Future[HttpResponse] = {
    Http().singleRequest(request)
  }

  //create one source for many request
  def createRestSource(names: Seq[String])
                      (implicit system: ActorSystem): Source[HttpRequest, NotUsed] = {
    Source[HttpRequest](names.map(n => request(getUrl(n))).toList)
  }

  //create multiple single sources
  def createSingleSources(names: Seq[String])
                         (implicit system: ActorSystem): Seq[Source[HttpRequest, NotUsed]] = {
    names.map(n => singleSource(n))
  }

  def singleSource(name: String)
                  (implicit system: ActorSystem): Source[HttpRequest, NotUsed] = {
    Source.single[HttpRequest](request(getUrl(name)))
  }
}

object GitRestClient extends RestClient {
  def getUrl(name: String): Uri = {
    Uri("https://" + config.getString("rest.api.url") + config.getString("rest.api.uri") + name)
  }
}

object OpenBdRestClient extends RestClient {
  def getUrl(name: String): Uri = {
    Uri(s"https://api.openbd.jp/v1/get?isbn=$name&pretty")
  }
}

object WikiRestClient extends RestClient {
  def getUrl(name: String): Uri = {
    Uri(s"http://wikipedia.simpleapi.net/api?keyword=$name&output=json")
  }

}
