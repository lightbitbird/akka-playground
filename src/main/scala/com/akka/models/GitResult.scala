package com.akka.models

case class Owner(id: Long, login: String, url: String) extends BaseEntity[Long]
case class GitRepo(id: Long, name: String, owner: Owner, url: String, language: String) extends BaseEntity[Long]
case class GitResult(total_count: Long, items: List[GitRepo]) extends BaseResult[GitRepo, Long]
case class GitResultV2(total_count: Long, items: List[GitRepo], incomplete_results: Boolean) extends BaseResult[GitRepo, Long]
