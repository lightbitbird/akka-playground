package com.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.akka.models.JsonSupport
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ApiRouteTest extends WordSpec with Matchers with ScalatestRouteTest with MockitoSugar with JsonSupport {
  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(new DurationInt(5).second)

  "HttpServer" when {
    "GET /api/stream" should {
      "return 200 OK" in {
        Get("http://localhost:8888/api/stream") ~> new ApiRoute().route ~> check {
          status.intValue() shouldBe 200

        }
      }
    }
  }

  def awaitForResult[T](futureResult: Future[T]): T = Await.result(futureResult, 5.seconds)
}
