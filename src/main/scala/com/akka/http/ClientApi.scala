package com.akka.http

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import akka.stream.{ActorMaterializer, SourceShape}
import akka.stream.scaladsl.{Concat, Flow, GraphDSL, Keep, Sink, Source}
import com.akka.models.{GitHubV2Entity, JsonSupport}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{ExecutionContext, Future}

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

  def compoundFlowFrom(sources: Seq[Source[Int, NotUsed]]) = {
    GraphDSL.create() { implicit b =>
      import akka.stream.scaladsl.GraphDSL.Implicits._

      println("------------------------")
//      sources.foreach(s => println(s"source -> $s"))

      val f1 = Flow[Int].map {v =>
        println(s"v -> $v")
        if (v % 2 == 0)
          Thread.sleep(300)
        else
          Thread.sleep(100)
        v + 100
      }

      val f2 = Flow[Int].map {v =>
        Thread.sleep(100)
        v + 10
      }
      val concat = b.add(Concat[Int](3))
      sources.foreach(s => s ~> f1 ~> concat)
      println("----------2--------------")

      SourceShape(concat.out)
    }
  }

//  def runFromSource: Source[Int, NotUsed] = {
  def runFromSource: Future[Seq[Int]] = {
    val source = Source(1 to 5).async
    val source2 = Source(6 to 10).async
    val source3 = Source(11 to 15).async
    val sourceShape = compoundFlowFrom(Seq(source, source2, source3))
//    Source.fromGraph(sourceShape).runWith(Sink.collection)

    Source.fromGraph(sourceShape).toMat(Sink.fold(Seq.empty[Int])(_ :+ _))(Keep.right).run()
//    Source.fromGraph(sourceShape).runWith(Sink.foreach(println))
  }
}
