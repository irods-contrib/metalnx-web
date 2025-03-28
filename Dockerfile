FROM tomcat:9.0.99-jdk21-temurin-noble
LABEL organization="iRODS Consortium"
LABEL description="Metalnx iRODS Browser"
RUN apt update 
RUN apt-get install wget
RUN apt-get install less
RUN apt-get install nano

COPY packaging/docker/runit.sh /
COPY packaging/docker/metalnx.war /usr/local/tomcat/webapps/
COPY packaging/docker/server.xml /conf/server.xml

CMD ["/runit.sh"]
