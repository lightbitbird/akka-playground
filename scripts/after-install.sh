#!/bin/bash

set -e

SRC_LOCATION=/var/local/akka-playground/
APP=akka-playground-assembly-0.1.jar
DEPLOY_JAR="${SRC_LOCATION}/bin/http-server"

if [ ! -e "${SRC_LOCATION}/bin" ]; then
  sudo mkdir "${SRC_LOCATION}/bin"
fi

sudo mv "${SRC_LOCATION}${APP}" $DEPLOY_JAR
sudo chmod 755 $DEPLOY_JAR
