#!/bin/sh
#	Copyright (c) 2015-2016, EMC Corporation
#
#	Licensed under the Apache License, Version 2.0 (the "License");
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

#=-=-=-=-=-=-=-=-=-=-=-=-=-
# iRODS connection params
IRODS_HOST="<irods-host>"
IRODS_PORT=<irods-port>
IRODS_ZONE="<irods-zone>"
IRODS_USER="<irods-user>"    # must be a rods admin
IRODS_PASS="<irods-password>"

#=-=-=-=-=-=-=-=-=-=-=-=-=-
# Web Server connection params
LISTEN_PORT=8080
HOSTNAME=$(hostname)

#=-=-=-=-=-=-=-=-=-=-=-=-=-
# Finding docker on the host machine
DOCKER_PATH=$(which docker)
if [ "$?" -ne "0" ]; then
	echo "[ERROR] Could not find docker installed on your environment."
	echo "[ERROR] Please make sure it is installed and in your PATH."
	exit 1
fi

DOCKER_VERSION=$($DOCKER_PATH version)

echo
echo "==========================================================="
echo "=            MetaLnx Web Application Container            ="
echo "==========================================================="
echo "   [Docker Version]                                       "
echo "$DOCKER_VERSION                                           "
echo 
echo "   [Connection parameters]"
echo "   Host     $IRODS_HOST                                   "
echo "   Port     $IRODS_PORT                                   "
echo "   Zone     $IRODS_ZONE                                   "
echo "   User     $IRODS_USER                                   "
echo "==========================================================="
echo

#=-=-=-=-=-=-=-=-=-=-=-=-=-
# Running container
$DOCKER_PATH run -d \
	-p $LISTEN_PORT:8080 \
	-e IRODS_HOST=$IRODS_HOST \
	-e IRODS_PORT=$IRODS_PORT \
	-e IRODS_ZONE=$IRODS_ZONE \
	-e IRODS_USER=$IRODS_USER \
	-e IRODS_PASS=$IRODS_PASS \
	arthurguerra/metalnx
	
echo "   [Container specification]                               "
echo "   URL: http://$HOSTNAME:$LISTEN_PORT/emc-metalnx-web/login"
echo "==========================================================="
echo