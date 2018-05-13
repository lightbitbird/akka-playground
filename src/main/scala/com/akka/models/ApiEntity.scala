package com.akka.models

case class WikiEntity(id: String, language: Option[String], url: Option[String], title: Option[String], body: Option[String]) extends BaseEntity[String]
case class Summary(isbn: String, title: String, volume: String, series: String, publisher: String, pubdate: String, cover: String, author: String)
case class OpenBdEntity(summary: Summary)
