package com.akka.streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, ClosedShape, Supervision}

object SupervisorGraph extends App {
  implicit val system = ActorSystem("supervisor-stream")
  implicit val executionContext = system.dispatcher

  val decider: Supervision.Decider = {
    case _: IllegalArgumentException => Supervision.Resume
    case _ => Supervision.Stop
  }
  //Add a SuperviserStrategy to Materializer
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  )

  val graph: RunnableGraph[NotUsed] = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val source = Source(1 to 5)
      val flow = Flow[Int].map { f =>
        if (f == 3) throw new IllegalArgumentException("Error occured!") else f
      }
      val flow2 = Flow[Int].map(_ + 10)
      val sink = Sink.foreach(println)

      source ~> flow ~> flow2 ~> sink

      ClosedShape
  })

  graph.run()

  Thread.sleep(5000)
  system.terminate()
}
