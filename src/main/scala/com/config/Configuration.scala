package com.config

import com.typesafe.config.{Config, ConfigFactory}

trait Configuration {
  def config: Config
}


trait AkkaPlaygroundConfig extends Configuration {

  private def innerConfig: Config = {
    val env = System.getProperty("DEVELOP", "akka-config")
    val default = ConfigFactory.load()

    default.hasPath(env) match {
      case true => default.getConfig(env).withFallback(default)
      case false => default
    }
  }

  val config: Config = innerConfig
}
