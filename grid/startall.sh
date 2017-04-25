#!/bin/bash

#DIR=$(PWD)
#APP_JARS=$DIR/../cluster-management-domain/target/cluster-management-domain-1.0.0.jar


gfsh <<!
start locator --name=locator1 --port=10334 --properties-file=config/locator.properties --load-cluster-configuration-from-dir=true --initial-heap=256m --max-heap=256m

start server --name=server1 --server-port=0 --properties-file=config/gemfire.properties --initial-heap=1g --max-heap=1g
#start server --name=server2 --server-port=0 --properties-file=config/gemfire.properties --initial-heap=1g --max-heap=1g

# deploy the functions
undeploy --jar=cluster-management-server-1.0.0.jar
deploy --jar=../cluster-management-server/target/cluster-management-server-1.0.0.jar

list members;
list regions;
exit;
!
