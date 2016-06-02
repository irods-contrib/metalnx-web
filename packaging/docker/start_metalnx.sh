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



#!/bin/bash

DB_NAME=metalnx
DB_USER=metalnx
DB_PASS=metalnx

PG_CONFDIR="/var/lib/pgsql/data"

__create_user() {
  #Grant rights
  usermod -G wheel postgres

  # Check to see if we have pre-defined credentials to use
if [ -n "${DB_USER}" ]; then
  if [ -z "${DB_PASS}" ]; then
    echo ""
    echo "WARNING: "
    echo "No password specified for \"${DB_USER}\". Generating one"
    echo ""
    DB_PASS=$(pwgen -c -n -1 12)
    echo "Password for \"${DB_USER}\" created as: \"${DB_PASS}\""
  fi
    echo "Creating user \"${DB_USER}\"..."
    echo "CREATE ROLE ${DB_USER} with CREATEROLE login superuser PASSWORD '${DB_PASS}';" |
      sudo -u postgres -H postgres --single \
       -c config_file=${PG_CONFDIR}/postgresql.conf -D ${PG_CONFDIR}
  
fi

if [ -n "${DB_NAME}" ]; then
  echo "Creating database \"${DB_NAME}\"..."
  echo "CREATE DATABASE ${DB_NAME};" | \
    sudo -u postgres -H postgres --single \
     -c config_file=${PG_CONFDIR}/postgresql.conf -D ${PG_CONFDIR}

  if [ -n "${DB_USER}" ]; then
    echo "Granting access to database \"${DB_NAME}\" for user \"${DB_USER}\"..."
    echo "GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} to ${DB_USER};" |
      sudo -u postgres -H postgres --single \
      -c config_file=${PG_CONFDIR}/postgresql.conf -D ${PG_CONFDIR}
  fi
fi
}

__setup_irods() {
	env_file=/usr/share/tomcat/webapps/emc-metalnx-web/WEB-INF/classes/irods.environment.properties
	sed -ir "s|irods.host=.*$|irods.host=$IRODS_HOST|" $env_file
	sed -ir "s|irods.port=.*$|irods.port=$IRODS_PORT|" $env_file
	sed -ir "s|irods.zoneName=.*$|irods.zoneName=$IRODS_ZONE|" $env_file
	sed -ir "s|jobs.irods.username=.*$|jobs.irods.username=$IRODS_USER|" $env_file
	sed -ir "s|jobs.irods.password=.*$|jobs.irods.password=$IRODS_PASS|" $env_file
}

__run_supervisor() {
  supervisord -n
}

# Call all functions
__create_user
__setup_irods
__run_supervisor
