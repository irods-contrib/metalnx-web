FROM tomcat:jre8-alpine
LABEL organization="iRODS Consortium"
LABEL description="Metalnx iRODS Browser"

RUN wget -qO- https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/5.2.4/flyway-commandline-5.2.4-linux-x64.tar.gz | tar xvz \
 && ln -s `pwd`/flyway-5.2.4/flyway /usr/local/bin

COPY src/metalnx-tools/src/main/resources/migrations /migrations

COPY packaging/docker/runit.sh /
COPY packaging/docker/metalnx.war /usr/local/tomcat/webapps/

CMD ["/runit.sh"]
