#!/bin/sh -xe

cd /usr/src/metalnx-web/src/metalnx-tools

mvn install flyway:clean flyway:migrate
