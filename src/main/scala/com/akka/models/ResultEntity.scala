package com.akka.models

import akka.http.scaladsl.model.{ContentTypes, ResponseEntity}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

trait ResultEntity[T] extends JsonSupport {
  def unmarshal(entity: ResponseEntity)(implicit ec: ExecutionContext, mat: Materializer): Future[T]
}

object GitHubEntity extends ResultEntity[GitResult] {
  override def unmarshal(entity: ResponseEntity)(
    implicit ec: ExecutionContext,
    mat: Materializer): Future[GitResult] = Unmarshal(entity).to[GitResult]
}

object GitHubV2Entity extends ResultEntity[GitResultV2] {
  override def unmarshal(entity: ResponseEntity)(
    implicit ec: ExecutionContext,
    mat: Materializer): Future[GitResultV2] = Unmarshal(entity).to[GitResultV2]
}

object WikiResultEntity extends ResultEntity[List[WikiEntity]] {
  override def unmarshal(entity: ResponseEntity)(
    implicit ec: ExecutionContext,
    mat: Materializer): Future[List[WikiEntity]] = Unmarshal(entity.withContentType(ContentTypes.`application/json`)).to[List[WikiEntity]]
}

object OpenBdResultEntity extends ResultEntity[List[OpenBdEntity]] {
  override def unmarshal(entity: ResponseEntity)(
    implicit ec: ExecutionContext,
    mat: Materializer): Future[List[OpenBdEntity]] = Unmarshal(entity.withContentType(ContentTypes.`application/json`)).to[List[OpenBdEntity]]
}
