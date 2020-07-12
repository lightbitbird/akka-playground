package com.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.akka.models.JsonSupport
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ApiRouteTest extends AnyWordSpec with Matchers with ScalatestRouteTest with BeforeAndAfterAll with JsonSupport {
  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(new DurationInt(5).second)
  val routes = new ApiRoute().route
  val server = Http().bindAndHandle(routes, "localhost", 0)

  override protected def afterAll(): Unit = {
    server
      .flatMap(_.unbind)
      .onComplete(_ => system.terminate)
  }

  "HttpServer" when {
    "GET /api/stream" should {
      "return 200 OK" in {
        Get("http://localhost:8888/api/stream") ~> routes ~> check {
          status.intValue() shouldBe 200

        }
      }
    }
  }

  def awaitForResult[T](futureResult: Future[T]): T = Await.result(futureResult, 5.seconds)
}
