FROM tomcat:jre8-alpine
LABEL organization="iRODS"
LABEL maintainer="michael.c.conway@gmail.com"
LABEL description="iRODS Metalnx Browser"
ADD runit.sh /

ADD emc-metalnx-web.war /usr/local/tomcat/webapps/
CMD ["/runit.sh"]



# build: docker build -t diceunc/metalnx:latest .

#  -v /home/mcc/webdavcert:/tmp/cert

# run:  docker run -d --rm -p 8080:8080 -v /etc/irods-ext:/etc/irods-ext  -v /home/mcc/webdavcert:/tmp/cert --add-host irods420.irodslocal:172.16.250.101 diceunc/metalnx:latest
