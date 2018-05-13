package com.akka.streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.{Concat, Flow, GraphDSL, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy, SourceShape, ThrottleMode}
import com.akka.http.{GitRestClinet, OpenBdRestClient, WikiRestClient}
import com.akka.models._
import com.config.AkkaPlaygroundConfig

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object ClientGraph extends AkkaPlaygroundConfig {

  def runFromSource(implicit materializer: ActorMaterializer): Future[Seq[Int]] = {
    Source.fromGraph(
      graphFromSource(Seq(Source(1 to 5), Source(6 to 10), Source(11 to 15)))
    ).runWith(Sink.seq[Int])
  }

  def graphFromSource(sources: Seq[Source[Int, NotUsed]]) = {
    GraphDSL.create() { implicit b =>
      import akka.stream.scaladsl.GraphDSL.Implicits._

      def f1 = Flow[Int].map { v =>
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

  def runRestMultiSources(names: Seq[String])(implicit sytem: ActorSystem,
                     ec: ExecutionContext,
                     materializer: ActorMaterializer): Future[Seq[GitRepo]] = {
    //    val users = Seq("akka+user:lightbitbird", "akka+user:lightbend")
    Source.fromGraph(
      graphFromRestMultiSources(GitRestClinet.createSingleSources(names))
    ).runWith(Sink.fold(List.empty[GitRepo])((list, g) => list ::: g))
  }

  def graphFromRestMultiSources(sources: Seq[Source[HttpRequest, NotUsed]])
                          (implicit sytem: ActorSystem,
                           ec: ExecutionContext,
                           materializer: ActorMaterializer) = {
    GraphDSL.create() { implicit b =>
      import akka.stream.scaladsl.GraphDSL.Implicits._

      def f1 = Flow[HttpRequest].map { req =>
        println(s"---------------- f1 ----------------------  ${req.uri.toString}")
        GitRestClinet.run(req).flatMap(res => GitHubV2Entity.unmarshal(res.entity))
      }.throttle(elements = 16, per = 1 second, maximumBurst = 0, mode = ThrottleMode.Shaping).async

      def f2 = Flow[Future[GitResultV2]].async.mapAsyncUnordered(3) { f =>
        for (entity <- f) yield entity.items
      }

      val concat = b.add(Concat[List[GitRepo]](sources.size))
      sources.map(b.add(_)).foreach(s => s ~> f1 ~> f2 ~> concat)

      SourceShape(concat.out)
    }
  }

  def runGitApiSource(names: Seq[String])(implicit sytem: ActorSystem,
                                          ec: ExecutionContext,
                                          materializer: ActorMaterializer): Future[Seq[GitRepo]] = {
    //    val users = Seq("akka+user:lightbitbird", "akka+user:lightbend")
    Source.fromGraph(
      graphFromGitApiSource(GitRestClinet.createRestSources(names))
    ).runWith(Sink.fold(List.empty[GitRepo])((list, g) => list ::: g))
  }

  def graphFromGitApiSource(source: Source[HttpRequest, NotUsed])
                           (implicit sytem: ActorSystem,
                            ec: ExecutionContext,
                            materializer: ActorMaterializer) = {
    GraphDSL.create() { implicit b =>
      import akka.stream.scaladsl.GraphDSL.Implicits._

      def f1 = Flow[HttpRequest].map { req =>
        println(s"---------------- f1 ----------------------  ${req.uri.toString}")
        GitRestClinet.run(req).flatMap(res => GitHubV2Entity.unmarshal(res.entity))
      }.buffer(10, OverflowStrategy.backpressure).async

      def f2 = Flow[Future[GitResultV2]].async.mapAsync(3) { f =>
        for (entity <- f) yield entity.items
      }
      val s = source.throttle(elements = 16, per = 1 second, maximumBurst = 0, mode = ThrottleMode.Shaping).async
      val s1 = b.add(s)
      val flow = b.add(f2)

      s1 ~> f1 ~> flow

      SourceShape(flow.out)
    }
  }

  def runOpenBdApiSource(names: Seq[String])(implicit sytem: ActorSystem,
                                           ec: ExecutionContext,
                                           materializer: ActorMaterializer): Future[Seq[Summary]] = {
    Source.fromGraph(
      graphFromOpenBdApiSource(OpenBdRestClient.createRestSources(names))
    ).runWith(Sink.fold(List.empty[Summary])((list, g) => list ::: g.map(v => v.summary)))
  }

  def graphFromOpenBdApiSource(source: Source[HttpRequest, NotUsed])
                            (implicit sytem: ActorSystem,
                             ec: ExecutionContext,
                             materializer: ActorMaterializer) = {
    GraphDSL.create() { implicit b =>
      import akka.stream.scaladsl.GraphDSL.Implicits._

      def f1 = Flow[HttpRequest].map { req =>
        println(s"---------------- f1 ----------------------  ${req.uri.toString}")
        OpenBdRestClient.run(req).flatMap(res => OpenBdResultEntity.unmarshal(res.entity))
      }.buffer(10, OverflowStrategy.backpressure).async

      def f2 = Flow[Future[List[OpenBdEntity]]].mapAsync(3) { f =>
        for (e <- f) yield e
      }

      val s = source.throttle(elements = 16, per = 1 second, maximumBurst = 0, mode = ThrottleMode.Shaping).async
      val s1 = b.add(s)
      val flow = b.add(f2)

      s1 ~> f1 ~> flow

      SourceShape(flow.out)
    }
  }

  def runWikiApiSource(names: Seq[String])(implicit sytem: ActorSystem,
                      ec: ExecutionContext,
                      materializer: ActorMaterializer): Future[Seq[WikiEntity]] = {
    //    val users = Seq("akka+user:lightbitbird", "akka+user:lightbend")
    Source.fromGraph(
      graphFromWikiApiSource(WikiRestClient.createRestSources(names))
    ).runWith(Sink.fold(List.empty[WikiEntity])((list, g) => list ::: g))
    //    ).runWith(Sink.seq[WikiEntity])
  }

  def graphFromWikiApiSource(source: Source[HttpRequest, NotUsed])
                           (implicit sytem: ActorSystem,
                            ec: ExecutionContext,
                            materializer: ActorMaterializer) = {
    GraphDSL.create() { implicit b =>
      import akka.stream.scaladsl.GraphDSL.Implicits._

      def f1 = Flow[HttpRequest].map { req =>
        println(s"---------------- f1 ----------------------  ${req.uri.toString}")
        WikiRestClient.run(req).flatMap(res => WikiResultEntity.unmarshal(res.entity))
      }.buffer(10, OverflowStrategy.backpressure).async

      def f2 = Flow[Future[List[WikiEntity]]].mapAsync(1) { f =>
        for (e <- f) yield e
      }

      val s = source.throttle(elements = 16, per = 1 second, maximumBurst = 0, mode = ThrottleMode.Shaping).async
      val s1 = b.add(s)
      val flow = b.add(f2)

      s1 ~> f1 ~> flow

      SourceShape(flow.out)
    }
  }

}
