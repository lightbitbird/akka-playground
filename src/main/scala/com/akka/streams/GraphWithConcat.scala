package com.akka.streams

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL, RunnableGraph, Sink, Source, ZipWith}
import akka.stream.{ActorMaterializer, ClosedShape}

object GraphWithConcat extends App {
  implicit val system = ActorSystem("streams-actor")
  implicit val materializer = ActorMaterializer()

  val concatDoc = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._
    val source = Source(1 to 5)

    val zip = b.add(ZipWith((left: Int, right: Int) => left))
    val bcast = b.add(Broadcast[Int](2))
    val concat = b.add(Concat[Int]())
    //    val start = Source(6 to 10)
    val start = Source.single(0)

    source ~> zip.in0
    zip.out.map { s => println(s); s } ~> bcast ~> Sink.ignore
    zip.in1 <~ concat <~ start
    concat         <~          bcast

    //    source ~> zip.in0
    //    zip.out.map { s => println(s); s } ~> bcast ~> Sink.ignore
    //    zip.in1             <~                bcast

    ClosedShape
  })

  val concat = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

//    val sources = buckets.map(bucket => {
//      val future: Future[List[Any]] = session.execute(....)
//      Source.fromFuture(future).mapConcat(identity)
//    }
//
//    val concat = builder.add(Concat[Any](sources.size))
//    sources.foreach(s => {
//      ...
//
//      s ~>... ~> concat
//    })
//    concat ~> sink

    val source = Source(1 to 5).async
    val source2 = Source(6 to 10).async
    val source3 = Source(11 to 15).async
    val f1 = Flow[Int].map {v =>
      Thread.sleep(300)
      v + 100
    }
    val f2 = Flow[Int].map {v =>
      Thread.sleep(100)
      v + 10
    }
//    val concat = b.add(Merge[Any](2))
    val concat = b.add(Concat[Any](3))
    source ~> f1 ~> concat ~> Sink.foreach(println)
    source2 ~> f2 ~> concat
    source3 ~> f2 ~> concat

    ClosedShape
  })

//  concatDoc.run()
  concat.run()

  Thread.sleep(10000)
  system.terminate()

}

