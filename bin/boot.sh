#!/bin/bash

eurekaServer=$1
localUrl=$2
hostname=$(hostname -f)
[ $# -eq 0 ] && { echo "Usage: $0 <eureka-server> <local-url>"; \
echo "Example: $0 localhost:8761 http://localhost:18086"; exit 1; }

git clone https://github.com/liuzhengyang/lets-hotfix
cd lets-hotfix
hotfixHome=`pwd`
echo $hotfixHome
./mvnw clean package -pl agent

agentPath=$hotfixHome/agent/target/agent-1.0-SNAPSHOT-jar-with-dependencies.jar

./mvnw spring-boot:run -Dserver.port=18086 -Dagent\
.path=$agentPath -Deureka.client.service-url.defaultZone=http://$eurekaServer/eureka/ -Deureka\
.instance.home-page-url=$localUrl -Deureka.instance.hostname=${hostname} -pl web