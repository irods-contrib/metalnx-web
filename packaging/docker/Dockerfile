#Copyright (c) 2015-2016, EMC Corporation
#
#Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.


#############################
# EMC MetaLnx Web container #
#############################
FROM centos:centos7
MAINTAINER Henrique Nogueira <henrique.nogueira@emc.com>

RUN yum -y update \
	&& yum -y install sudo epel-release

# Temporary fix for EMC network firewall
RUN sed -i "s|mirrorlist|#mirrorlist|" /etc/yum.repos.d/epel.repo \
	&& sed -i "s|#baseurl|baseurl|" /etc/yum.repos.d/epel.repo \
	&& sed -i '16 i\failovermethod=priority' /etc/yum.repos.d/CentOS-Base.repo \
	&& sed -i "s|http://download.fedoraproject.org/pub/|http://mirrors.mit.edu/|" /etc/yum.repos.d/epel.repo \
	&& yum -y install \
		postgresql-server \
		postgresql \
		postgresql-contrib \
		supervisor \
		tomcat \
		tomcat-servlet-3.0-api \
		java-1.8.0-openjdk-devel \
		wget 

ADD ./postgresql-setup /usr/bin/postgresql-setup
ADD ./supervisord.conf /etc/supervisord.conf
ADD ./start_metalnx.sh /start_metalnx.sh

#Sudo requires a tty. fix that.
RUN sed -i 's/.*requiretty$/#Defaults requiretty/' /etc/sudoers \
	&& chmod +x /usr/bin/postgresql-setup; sync \
	&& chmod +x /start_metalnx.sh; sync \
	&& usr/bin/postgresql-setup initdb

ADD ./postgresql.conf /var/lib/pgsql/data/postgresql.conf

RUN chown -v postgres.postgres /var/lib/pgsql/data/postgresql.conf \
	&& sed -i "s|ident|trust|g" /var/lib/pgsql/data/pg_hba.conf 

RUN mkdir /usr/share/tomcat/webapps/emc-metalnx-web \
	&& wget https://tpa-eld1102/job/metalnx-web-rev1/ws/src/emc-metalnx-web/target/emc-metalnx-web.war --no-check-certificate -O /usr/share/tomcat/webapps/emc-metalnx-web/emc-metalnx-web.war \
	&& cd /usr/share/tomcat/webapps/emc-metalnx-web; jar xvf emc-metalnx-web.war; rm -f emc-metalnx-web.war \
	&& cd /usr/share/tomcat/webapps/emc-metalnx-web/WEB-INF/classes/; rm database.properties; mv postgresql.properties database.properties


EXPOSE 8080

CMD ["/bin/bash", "/start_metalnx.sh"]
