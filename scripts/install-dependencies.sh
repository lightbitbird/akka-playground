#!/bin/bash

set -e


#if [ ! -e "/usr/bin/java" ]; then
#  wget https://d3pxv6yz143wms.cloudfront.net/11.0.3.7.1/java-11-amazon-corretto-devel-11.0.3.7-1.x86_64.rpm
#  sudo yum localinstall java-11-amazon-corretto-devel-11.0.3.7-1.x86_64.rpm
#fi

#wget https://downloads.lightbend.com/scala/2.13.1/scala-2.13.1.tgz
#tar xvzf ./scala-2.13.1.tgz
#sudo mv scala-2.13.1 /usr/local/scala
#sudo chown -R root:root /usr/loca/scala

SRC_LOCATION=/var/local/akka-playground/
APP=akka-playground-assembly-0.1.jar
DEPLOY_JAR="${SRC_LOCATION}/bin/http-server"

if [ -e "${SRC_LOCATION}${APP}" ]; then
  mv ${SRC_LOCATION}${APP} "${SRC_LOCATION}${APP}.backup"
fi

if [ -e $DEPLOY_JAR ]; then
  rm -rf $DEPLOY_JAR
fi
