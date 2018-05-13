#!/usr/bin/env bash

cp /tomcat-lib/*.jar /usr/local/tomcat/lib/

#wait for couchbase
sleep 15

catalina.sh run