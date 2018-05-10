package com.akka.http

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream._
import akka.stream.scaladsl.{Concat, Flow, GraphDSL, Keep, Sink, Source}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.util.ByteString
import com.akka.models.JsonSupport

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ApiRoute(implicit system: ActorSystem,
               ec: ExecutionContext,
               materializer: ActorMaterializer) extends Directives with JsonSupport {

  def route: Route = {
    pathPrefix("api") {
      get {
        path("stream") {
          val sourceOfInformation = Source("Prepare to scroll!")
          val sourceOfNumbers = Source(1 to 1000)
          val byteStringSource = sourceOfInformation.concat(sourceOfNumbers) // merge the two sources
            .throttle(elements = 1000, per = 1 second, maximumBurst = 1, mode = ThrottleMode.Shaping)
            .map(_.toString)
            .map(s => ByteString(s + "\n"))

          complete(HttpEntity(`text/plain(UTF-8)`, byteStringSource))
//          complete(ToResponseMarshallable(runFromSource))
//          complete(ToResponseMarshallable(""))
        }
      }
    }
  }

  //  def compoundFlowFrom() = {
  def compoundFlowFrom(sources: Seq[Source[Int, NotUsed]]) = {
    GraphDSL.create() { implicit b =>
      import akka.stream.scaladsl.GraphDSL.Implicits._

      println("------------------------")
      //      sources.foreach(s => println(s"source -> $s"))
      //      val source = Source(1 to 5).async
      //      val source2 = Source(6 to 10).async
      //      val source3 = Source(11 to 15).async
      //      val sources = Seq(source, source2, source3)

      def f1 = Flow[Int].via(op[Int, Int]).named("f1")
      def f3 = Flow[Int].via(op[Int, Int]).named("f3")
      def f4 = Flow[Int].via(op[Int, Int]).named("f4")
//      def f1 = Flow[Int].via(op[Int, Int]).named("f1").map {v =>
//        println(s"v -> $v")
//        if (v % 2 == 0)
//          Thread.sleep(300)
//        else
//          Thread.sleep(100)
//        v + 100
//      }

      def f2 = Flow[Int].via(op[Int, Int]).named("f2").map {v =>
        Thread.sleep(100)
        v + 10
      }

      val flow = b.add(f1)
      val concat = b.add(Concat[Int](3))
      sources(0) ~> f1 ~> concat.in(0)
      sources(1) ~> f3 ~> concat.in(1)
      sources(2) ~> f4 ~> concat.in(2)
      println("----------2--------------")

      SourceShape(concat.out)
    }
  }

  def op[In, Out] = new GraphStage[FlowShape[In, Out]] {
    val in = Inlet[In]("op.in")
    val out = Outlet[Out]("op.out")
    override val shape = FlowShape[In, Out](in, out)
    override def createLogic(inheritedAttributes: Attributes) = new GraphStageLogic(shape) with InHandler with OutHandler {
      override def onPush() = push(out, grab(in).asInstanceOf[Out])
      override def onPull(): Unit = pull(in)
      setHandlers(in, out, this)
    }
  }

  //  def runFromSource: Source[Int, NotUsed] = {
  def runFromSource: Future[Seq[Int]] = {
    val source = Source(1 to 5)
    val source2 = Source(6 to 10)
    val source3 = Source(11 to 15)
    //    val sourceShape = compoundFlowFrom()
    val sourceShape = compoundFlowFrom(Seq(source, source2, source3))
    //    Source.fromGraph(sourceShape).runWith(Sink.collection)

    Source.fromGraph(sourceShape).toMat(Sink.fold(Seq.empty[Int])(_ :+ _))(Keep.right).run()
    //    Source.fromGraph(sourceShape).runWith(Sink.foreach(println))
  }
}
