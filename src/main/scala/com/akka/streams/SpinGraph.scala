package com.akka.streams

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Balance, Broadcast, Concat, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source, ZipWith}

import scala.concurrent.Future

object SpinGraph extends App {
  implicit val system = ActorSystem("streams-actor")
  implicit val materializer = ActorMaterializer()

  val basicGraph: RunnableGraph[NotUsed] =
    RunnableGraph.fromGraph(GraphDSL.create() {
      implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._

        val source = Source(1 to 5)
        val sink = Sink.foreach(println)

        val balance = builder.add(Balance[String](3))
        val merge = builder.add(Merge[String](3))
        val flow1 = Flow[Int].map(f => s"$f ~> flow1")
        val flow2_1 = Flow[String].map(f => s"$f ~> flow2_1")
        val flow2_2 = Flow[String].map(f => s"$f ~> flow2_2")
        val flow2_3 = Flow[String].map(f => s"$f ~> flow2_3")
        val flow3 = Flow[String].map(f => s"$f ~> flow3")

        source ~> flow1 ~> balance ~> flow2_1 ~> merge ~> flow3 ~> sink
        balance ~> flow2_2 ~> merge
        balance ~> flow2_3 ~> merge

        ClosedShape
    })

  val basicGraph2 = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._
    val in = Source(1 to 10)
//    val out: Sink[Any, Future[Done]] = Sink.foreach(println)
    val out: Sink[Any, Future[Done]] = Sink.foreach(a => println(s"g -----> $a"))

    val bcast = builder.add(Broadcast[Int](2))
    val merge = builder.add(Merge[Int](2))

    val f1, f2 = Flow[Int].map(_ + 10)
    val f3 = Flow[Int].map(_ + 50)
    val f4 = Flow[Int].map(_ + 100)

    in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
    bcast ~> f4 ~> merge

    ClosedShape
  })

  basicGraph.run()
//  basicGraph2.run()

  Thread.sleep(1000)
  system.terminate()

}

