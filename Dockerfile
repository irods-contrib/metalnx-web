FROM tomcat:jre8-alpine
LABEL organization="iRODS Consortium"
LABEL description="Metalnx iRODS Browser"

RUN wget -qO- https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/5.2.4/flyway-commandline-5.2.4-linux-x64.tar.gz | tar xvz \
 && ln -s `pwd`/flyway-5.2.4/flyway /usr/local/bin

COPY src/metalnx-tools/src/main/resources/migrations /migrations

COPY packaging/docker/runit.sh /
COPY packaging/docker/metalnx.war /usr/local/tomcat/webapps/

CMD ["/runit.sh"]



# build: docker build -t myimages/metalnx:latest .

# run:  docker run -d --rm -p 8080:8080 -v /etc/irods-ext:/etc/irods-ext  -v /home/mcc/webdavcert:/tmp/cert --add-host irods420.irodslocal:172.16.250.101 diceunc/metalnx:latest
