#!/bin/bash

set -e

SRC_LOCATION=/var/local/akka-playground/
APP=akka-playground-assembly-0.1.jar
DEPLOY_JAR="${SRC_LOCATION}/bin/http-server"
#DEPLOY_JAR=/etc/init.d/akka

if [ ! -e "${SRC_LOCATION}/bin" ]; then
  sudo mkdir "${SRC_LOCATION}/bin"
fi

sudo mv "${SRC_LOCATION}${APP}" $DEPLOY_JAR
#sudo ln -s $SRC_LOCATION/akka-playground_2.13-0.1.jar $DEPLOY_JAR
sudo chmod 755 $DEPLOY_JAR
