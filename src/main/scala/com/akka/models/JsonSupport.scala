package com.akka.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val owFormat = jsonFormat3(Owner.apply)
  implicit val gitRepoFormat = jsonFormat5(GitRepo.apply)
  implicit val resFormat = jsonFormat2(GitResult)
  implicit val res2Format = jsonFormat3(GitResultV2)
}

