package com.akka.http

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream._
import akka.stream.scaladsl.{Concat, Flow, GraphDSL, Sink, Source}
import com.akka.models.JsonSupport

import scala.concurrent.{ExecutionContext, Future}

class ApiRoute(implicit system: ActorSystem,
               ec: ExecutionContext,
               materializer: ActorMaterializer) extends Directives with JsonSupport {

  def route: Route = {
    pathPrefix("api") {
      get {
        path("stream") {
          complete(ToResponseMarshallable(runFromSource))
        }
      }
    }
  }

  def compoundFlowFrom(sources: Seq[Source[Int, NotUsed]]) = {
    GraphDSL.create() { implicit b =>
      import akka.stream.scaladsl.GraphDSL.Implicits._

      def f1 = Flow[Int].map {v =>
        println(s"v -> $v")
        if (v % 2 != 0) Thread.sleep(500)
        else Thread.sleep(100)
        v + 100
      }.async

      val concat = b.add(Concat[Int](3))
      sources.map(b.add(_)).foreach(s => s ~> f1 ~> concat)

      SourceShape(concat.out)
    }
  }

  def runFromSource: Future[Seq[Int]] = {
    Source.fromGraph(
      compoundFlowFrom(Seq(Source(1 to 5), Source(6 to 10), Source(11 to 15)))
    ).runWith(Sink.seq[Int])
//    Source.fromGraph(sourceShape).runWith(Sink.foreach(println))
  }
}
