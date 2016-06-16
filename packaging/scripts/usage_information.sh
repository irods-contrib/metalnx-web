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

# Global variables
current_time=$(date +"%Y_%m_%d_%H_%M_%S")

# Environment variables
METALNX_ENV_FILE=/opt/emc/.metalnx.env
. $METALNX_ENV_FILE

# Checking for the tar package
TAR_BIN=$(which tar > /dev/null 2>&1)
if [ $? -ne 0 ];
then
	echo "ERROR: Could not locate tar tool. Make sure it is installed and try again."
	exit 1
fi

print_help() {
	echo "$0 is used to manage usage information on EMC MetaLnx WebApp."
	echo "The tomcat installation used will be the one configured with MetaLnx."
	echo "Usage: $0 <command>"
	echo "Command:"
	echo -e "\tenable\t\t enables Java Session ID on the access logs (*Tomcat will be restarted)"
	echo -e "\tdisable\t\t disables Java Session ID on the access logs (*Tomcat will be restarted)"
	echo -e "\texport\t\t creates a tar with the access logs to be sent to EMC"
	echo
	echo -e "\t*Active Tomcat connections will be reset."
}

restart_tomcat() {
	echo -n "Restarting your Tomcat instance... "
	$TOMCAT_HOME/bin/shutdown.sh > /dev/null 2>&1
	$TOMCAT_HOME/bin/startup.sh > /dev/null 2>&1
	echo "Done!"
}

if [ -z $1 ];
then
	print_help
	exit 2
else
	echo "Using tomcat instance on $TOMCAT_HOME"
	SERVER_CONF=$TOMCAT_HOME/conf/server.xml
	
	if [ "$1" == "enable" ];
	then
		echo -n "Enabling SESSION ID on Tomcat access logs... "
		sed -i "s|%u %t|%u %S %t|g" $SERVER_CONF
		if [ $? -ne 0 ];
		then
			echo "ERROR: Could not enable usage information on Tomcat."
			exit 1
		fi
		
		echo "Done!"
		restart_tomcat
		exit 0
	elif [ "$1" == "disable" ];
	then
		echo -n "Disabling SESSION ID on Tomcat access logs... "
		sed -i "s|%u %S %t|%u %t|g" $SERVER_CONF
		if [ $? -ne 0 ];
		then
			echo "ERROR: Could not disable usage information on Tomcat."
			exit 1
		fi
		
		echo "Done!"
		restart_tomcat
		exit 0
	elif [ "$1" == "export" ];
	then
		LOG_PREFIX=$(cat $SERVER_CONF | grep -oP 'prefix="\K([\._a-zA-Z0-9]+)')
		TMP_FOLDER=/tmp/metalnx/usage_information
		
		echo "Gathering logs with format $TOMCAT_HOME/logs/$LOG_PREFIX.*... "
		
		mkdir -p $TMP_FOLDER
		cp $TOMCAT_HOME/logs/$LOG_PREFIX* $TMP_FOLDER > /dev/null 2>&1
		cd $TMP_FOLDER
		tar -czvf access_logs_$current_time.tar.gz . > /dev/null 2>&1
		mv $TMP_FOLDER/access_logs_$current_time.tar.gz /opt/emc
		rm -rf $TMP_FOLDER
		
		echo "The access log archive is located on /opt/emc/access_logs_$current_time.tar.gz"
	else
		print_help
	fi
fi