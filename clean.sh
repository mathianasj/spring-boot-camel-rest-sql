#! /bin/bash

set -ex

APP_NAME=spring-boot-camel-rest-sql

oc delete all,secret,configmap,pvc,serviceaccount,rolebinding --selector app=$APP_NAME
oc delete secrets --selector app=$APP_NAME

# mvn fabric8:undeploy

