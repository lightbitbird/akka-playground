package com.akka.models

trait BaseEntity[A] {
  val id: A
}

trait BaseResult[T <: BaseEntity[A], A] {
  val items: List[T]
}
