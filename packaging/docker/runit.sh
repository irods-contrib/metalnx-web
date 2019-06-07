#!/bin/sh -xe

if [ -f /tmp/cert/server.crt ];
then
   echo "Cert will be imported"
   set +e
   keytool -delete -noprompt -alias mycert -keystore /usr/lib/jvm/default-jvm/jre/lib/security/cacerts -storepass changeit
   set -e
   keytool -import -trustcacerts -keystore /usr/lib/jvm/default-jvm/jre/lib/security/cacerts -storepass changeit -noprompt -alias mycert -file /tmp/cert/server.crt
else
   echo "No cert to import"
fi

echo "running catalina"
catalina.sh run > /tmp/catalina.out
