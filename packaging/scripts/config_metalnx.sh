#!/bin/bash

# Global variables
current_time=$(date +"%Y_%m_%d_%H_%M_%S")
conf_tmp_dir=/tmp/metalnx.conf.$current_time
existing_installation=0
keystore_path=$HOME/.metalnx_keystore
keystore_password="M3t4LnX@2o15#"
protocol="http"
port="8080"

# Environment variables
METALNX_ENV_FILE=/opt/emc/.metalnx.env

# iRODS access information
irods_icat_hostname=""
irods_icat_port=""
irods_zone_name=""
irods_username=""
irods_password=""

# Database credentials
enabled_database="mysql"
change_database="n"

# Checking for the java-devel package
stat /usr/bin/jar --version > /dev/null 2>&1
if [ $? -ne 0 ]; then
	echo "Could not locate jar command. Please install the java-devel package using: 'sudo yum install java-devel'."
	exit 1
fi

echo -n "Enter the Tomcat Home directory: "
read tomcat_home

if [ ! -d $tomcat_home/bin ]; then
	echo "This is not a valid Tomcat installation directory. Not 'bin' subdirectory has been found."
	exit 1
else
	echo "Shutting Tomcat down... "
	$tomcat_home/bin/shutdown.sh > /dev/null 2>&1
	echo "Done!"
fi

echo -n "Checking if $tomcat_home is an actual tomcat installation... "
if [ -d $tomcat_home/webapps ]; then
    echo "Ok!"
else
    echo "Could not find the webapps subdirectory on the specified Tomcat home."
    exit 1
fi

echo -n "Looking for the MetaLnx WAR file... "
if [ -f /tmp/emc-tmp/emc-metalnx-web.war ]; then
    echo "Ok!"
else
    echo "Could not find MetaLnx war file. Try removing and re-installing the RPM package."
    exit 1
fi

# Checking is there are MetaLnx installations
if [ -d $tomcat_home/webapps/emc-metalnx-web/WEB-INF/classes ]; then
	existing_installation=1
	echo "Found an existing MetaLnx Web installation."
	
	echo "TOMCAT_HOME=$tomcat_home" > $METALNX_ENV_FILE
	
	echo "All the configuration files will be automatically restored."
	mkdir -p $conf_tmp_dir
	mv $tomcat_home/webapps/emc-metalnx-web/WEB-INF/classes/*.properties $conf_tmp_dir/
else
	echo "No existing installation has been found. Creating a new profile..."

	echo -n "Enter the iCAT hostname: "
	read irods_icat_hostname
	
	echo -n "Enter the iCAT port: "
	read irods_icat_port
	
	echo -n "Enter the Zone Name: "
	read irods_zone_name
	
	echo -n "Enter the iRODS administrator username: "
	read irods_username
	
	echo -n "Enter the iRODS administrator password (it won't be displayed): "
	read -s irods_password
	echo
	echo
	
	echo -n "By default, MetaLnx uses MySQL as database. Would you like to use PostgreSQL instead? [y/N] "
	read change_database
fi

# Changing default database
if [ "$change_database" == "y" ];
then
	enabled_database="postgresql"
fi

echo "Using $enabled_database."

# Removing old metalnx instances
echo -n "Removing old instances of MetaLnx Web application..."
rm -rf $tomcat_home/webapps/emc-metalnx-web*
if [ $? -eq 0 ]; then
    echo "Done!"
else
    echo "Could not remove old instances of metalnx."
    exit 1
fi

echo -n "Moving the WAR package to the correct directory... "
mkdir -p $tomcat_home/webapps/emc-metalnx-web
cp /tmp/emc-tmp/emc-metalnx-web.war $tomcat_home/webapps/emc-metalnx-web/
if [ $? -eq 0 ]; then
    echo "Done!"
else
    echo "Could not move WAR file to $tomcat_home/webapps."
    exit 1
fi

# Extracting the WAR file
cd $tomcat_home/webapps/emc-metalnx-web
jar xvf $tomcat_home/webapps/emc-metalnx-web/emc-metalnx-web.war > /dev/null 2>&1

# Moving the WAR file back to the webapps root.
mv $tomcat_home/webapps/emc-metalnx-web/emc-metalnx-web.war $tomcat_home/webapps/emc-metalnx-web.war

# Restoring or creating configuration files
if [ $existing_installation -eq 1 ]; then
	echo -n "Restoring configuration files... "
	mv $conf_tmp_dir/*.properties $tomcat_home/webapps/emc-metalnx-web/WEB-INF/classes/
	if [ $? -eq 1 ]; then
		echo "Could not move configuration files from $conf_tmp_dir back to the MetaLnx webapp folder."
	else
		echo "Done."
	fi
else
	echo -n "Creating iRODS environment configuration file..."
	sed -i "s|irods.host=.*|irods.host=$irods_icat_hostname|" $tomcat_home/webapps/emc-metalnx-web/WEB-INF/classes/irods.environment.properties
	sed -i "s|irods.port=.*|irods.port=$irods_icat_port|" $tomcat_home/webapps/emc-metalnx-web/WEB-INF/classes/irods.environment.properties
	sed -i "s|irods.zoneName=.*|irods.zoneName=$irods_zone_name|" $tomcat_home/webapps/emc-metalnx-web/WEB-INF/classes/irods.environment.properties
	sed -i "s|jobs.irods.username=.*|jobs.irods.username=$irods_username|" $tomcat_home/webapps/emc-metalnx-web/WEB-INF/classes/irods.environment.properties
	sed -i "s|jobs.irods.password=.*|jobs.irods.password=$irods_password|" $tomcat_home/webapps/emc-metalnx-web/WEB-INF/classes/irods.environment.properties
	echo "Done!"
	
	# Setting database configuration
	echo -n "Setting database configuration file..." 
	cp -f $tomcat_home/webapps/emc-metalnx-web/WEB-INF/classes/$enabled_database.properties $tomcat_home/webapps/emc-metalnx-web/WEB-INF/classes/database.properties
	echo "Done!"
	echo
fi

if [ $existing_installation -eq 0 ]; then
	enable_https_tomcat=""
	# Making sure the user inputted a valid option
	while [ "$enable_https_tomcat" != "y" ] && [ "$enable_https_tomcat" != "n" ];
	do
		echo -n "Do you want to enable HTTPS on Tomcat and Metalnx? [Y/n]: "
		read enable_https_tomcat
		
		if [ "$enable_https_tomcat" == "" ];
		then
			enable_https_tomcat="y"
		fi
		
		# Making sure the response is lower-cased
		enable_https_tomcat=$(echo "$enable_https_tomcat" | tr '[:upper:]' '[:lower:]')
	done

	if [ "$enable_https_tomcat" == "y" ];
	then
		# Extracting the WAR file
		cd $tomcat_home/webapps/emc-metalnx-web
		jar xvf $tomcat_home/webapps/emc-metalnx-web/emc-metalnx-web.war > /dev/null 2>&1
	
		TOMCAT_CONF_FILE=$tomcat_home/conf/server.xml
		WEB_XML_FILE=$tomcat_home/webapps/emc-metalnx-web/WEB-INF/web.xml
		
		# Change None to CONFIDENTIAL on web.xml tomcat configuration file
		echo -n "Setting web.xml security mode to CONFIDENTIAL... "
		sed -i "s|>NONE<|>CONFIDENTIAL<|" $WEB_XML_FILE
		if [ $? -ne 0 ];
		then
			echo "Error. Could not modify web.xml file."
			exit 1
		fi
		echo "Done!"
		
		echo -n "Creating self-signed certificate... "
		keytool -genkey –keysize 2048 -noprompt -alias metalnx-tomcat -dname "CN=MetaLnx Tester, OU=home, O=home, L=Campinas, ST=SP, C=BR" -keyalg RSA -keystore $keystore_path -storepass "$keystore_password" -keypass "$keystore_password"
		if [ $? -ne 0 ];
		then
			echo "Error. Could not create self-signed certificate."
			exit 1
		fi
		echo "Done!"
		
		CONNECTOR_SPEC="<Connector port=\"8443\" accceptCount=\"100\" protocol=\"org.apache.coyote.http11.Http11Protocol\" disableUploadTiemout=\"true\" enableLookups=\"true\" keystoreFile=\"$keystore_path\" maxThreads=\"150\" SSLEnabled=\"true\" scheme=\"https\" secure=\"true\" keystorePass=\"$keystore_password\" clientAuth=\"false\" sslProtocol=\"TLS\" ciphers=\"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_256_CBC_SHA256,TLS_RSA_WITH_AES_256_CBC_SHA\" />"
		
		cp $tomcat_home/conf/server.xml $tomcat_home/conf/server.xml.bkp.$(date +%s)
		sed -i "s|<Connector.*port=\"8443\".*|-->$CONNECTOR_SPEC<\!--|g" $tomcat_home/conf/server.xml
		port="8443"
		protocol="https"
	fi
fi

echo -n "Starting Tomcat ... "
$tomcat_home/bin/startup.sh > /dev/null 2>&1
if [ $? -ne 0 ]; then
	echo "Failed. Tomcat could not be started. Please do it manually."
fi

echo -n "Removing temporary structures... "
rm -rf $conf_tmp_dir
rm -rf /tmp/emc-tmp
echo "Done!"

echo
echo "All set. Make sure your Tomcat server is restarted and point your browser to $protocol://$(hostname):$port/emc-metalnx-web/login/."

exit 0
