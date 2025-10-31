FROM tomcat:9.0.111-jdk21-temurin-noble

LABEL organization="iRODS Consortium"
LABEL description="Metalnx iRODS Browser"

RUN apt update && \
    apt-get install -y \
        less \
        nano \
        wget

COPY packaging/docker/runit.sh /
COPY packaging/docker/metalnx.war /usr/local/tomcat/webapps/
COPY packaging/docker/server.xml /conf/server.xml

CMD ["/runit.sh"]
