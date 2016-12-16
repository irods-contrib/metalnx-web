EMC METALNX WEB - INSTALL GUIDE
===============================

----------------------------------

<font color="#000000">
Copyright © 2015-16 EMC Corporation.

This software is provided under the Software license provided in the <a href="LICENSE"> LICENSE </a> file.

The information in this file is provided “as is.” EMC Corporation makes no representations or warranties of any kind with respect to the information in this publication, and specifically disclaims implied warranties of merchantability or fitness for a particular purpose. 

-------------------------------- 

<font color="#0066CC"> <font size=+2> __TABLE OF CONTENTS__ </font>

<font color="#000000"> <a name="TOC"></a>

<font size=+1> 

1. [Introduction](#introduction)
2. [Overview](#metalnx_overview)
3. [Metalnx Packages](#metalnx_packages)
4. [Metalnx Web Installation](#metalnx_installation)
5. [Metalnx Web Installation Process](#metalnx_installation_process)
6. [Apache Tomcat Installation](#apache_tomcat_installation)
7. [Install the Metalnx Web Application](#install_metalnx)
8. [Configure the Metalnx Database](#config_metalnx_database)
9. [Setup Metalnx](#setup_metalnx)
10. [Accessing Metalnx](#accessing_metalnx)
11. [Metalnx Install Checklist](#metalnx_checklist)
12. [Integration With LDAP](#LDAP)

</font>

----------------------------------

<br>
<font color="#0066CC"> <font size=+2> __INTRODUCTION (Read First!)__ </font> <a name="introduction"></a>

<font color="#000000">


Metalnx is a web application designed to work alongside the [iRODS (integrated Rule-Oriented Data System)](http://www.irods.org). It provides a graphical UI that can help simplify most administration, collection management, and metadata management tasks removing the need to memorize the long list of icommands.

This installation guide will provide information on how to install the components necessary to run Metalnx along with installation the application. 

### Dependencies <a id="dependencies"></a>

- Java 1.8 or higher
- iRODS 4.1.8, 4.1.9 or 4.1.10
- MySQL 5.6 or higher
- PostgreSQL 9.3 or higher
- Tomcat 7 or higher

At a high level Metalnx is dependent on the following software components being available:

- The Metalnx application, the Metalnx Remote Monitor Daemon (RMD), and the Metalnx iRODS MSI (microservice) files all being built and available as either `.rpm` or `.deb` files.
- Apache Tomcat (for running EMC Metalnx which is a Java servlet)
- iRODS runtime API
- MySQL or PostgreSQL (we use a database to hold Metalnx operational information)
- Java

__Assumptions__

In this installation guide, to fully install Metalnx, we will:

- Assume that iRODS is installed and that an ICAT server is operational (**Note:** If you need help with installing iRODS please check the documentation for the current release at [docs.irods.org](https://docs.irods.org/))
- Assume that the Java runtime and API packages are installed
- Show how to install Tomcat and configure a basic setup (Tomcat version 7 & 8 have been tested.)
- Show how to install Metalnx and configure it work with the iRODS ICAT and use a local MySQL or PostgreSQL RDBMS for holding its information

Metalnx has been tested on the following Linux distributions as indicated:

- CentOS 7 – all functional testing performed.
- CentOS 6 – verified Metalnx will install and start.
- Ubuntu 14 – verified Metalnx will install and start.

Metalnx will work with iRODS 4.1.8, 4.1.9 or 4.1.10.

##### iRODS Background #####
 
iRODS is best described as middleware. It is a software framework that provides capabilities for:

- Storing and managing information (files, objects, etc.) across multiple servers in a data grid using uniform commands across the grid.
- Supporting extensive user defined metadata tagging of objects placed under iRODS management.
- A rules/policy engine which can be adapted via user defined rules and microservices to perform any computer actionable activity on data.
- Federation of independent iRODS data grids allowing sharing of resources between grids.

An iRODS data grid consists of three elements:

- The iRODS Catalogue (called ICAT). The ICAT is kept in a database housed on the grid primary server called the ICAT server. There is one ICAT server per grid.
- One or more iRODS resource servers which communicate between each other and the ICAT server allowing storage resources connected to these resources servers to be added into the data grid.
- One or more storage resources which provide the physical storage for file/objects placed in iRODS data management control.

The diagram below (Figure 1) illustrates an iRODS environment:

![alt text] [1]
[1]: IMAGES/Install_figure_1.png "Figure 1 - An Example iRODS Grid"

The elements are:

1. iRODS ICAT server
2. iRODS Resource servers 
3. iRODS Storage Resources (This can be a combination of direct attach, SAN, NAS, Object stores (with supporting iRODS resource plug-in), EMC Isilon, and/or EMC ECS.)
4. Federated iRODS Grids (other grids)

The storage resources in the grid can be combined to provide collections making curation of data uniform. Each iRODS user is presented with a machine independent logical view of the information (collections) and tools (icommands) for working with the data. iRODS has an extensive collection of APIs which allow for the creation and customization of software integrated with iRODS.

The iRODS Consortium [www.irods.org](http://www.irods.org) provides a short technical overview of these capabilities which can be found at:

[http://irods.org/wp-content/uploads/2012/04/iRODS-Overview-November-2014.pdf](http://irods.org/wp-content/uploads/2012/04/iRODS-Overview-November-2014.pdf)

[[Back to: Table of Contents](#TOC)]

<br>
<font color="#0066CC"> <font size=+2> __Metalnx Overview__ </font></font> <a name="metalnx_overview"></a>

<font color="#000000">

Metalnx is a web application designed to work alongside iRODS (Integrated Rule-Oriented Data System).  It provides an intuitive graphical interface that supports iRODS administrative actions, collection management, and metadata management without requiring the iRODS administrator or user to memorize individual icommands. The application allows administrators to monitor system health, manage users, storage resources, and content.  It allows users to manage content and metadata associated with content.

*****Key features of Metalnx include:*****

For **rodsadmin** users:

- *Dashboard* - Displays information on the system health.
- *User Management*  - Add, edit and remove users from iRODS. 
- *Group Management* - Create, edit and remove iRODS groups.
- *User Profile Management* – Create user profiles that contain pre-defined groups and permission settings that can be applied to any iRODS users as needed.  
- *Resource Management* – Add, remove, and view information about storage resources in the iRODS grid.   

For **rodsuser** users:

- *Collection Management* - Add, edit, upload, download, copy, move, and delete objects / collections as needed.
- *Metadata Extraction* - Automatically extracts and  embedded metadata for .jpeg, .bam, .cram, & .vcf files into the ICAT.
- *Metadata Management* – Add, edit, & remove metadata tags in via the collection management page.
- *Metadata Templates* - Define templates with multiple metadata tags and default values.  Apply the template across one or many files from the Collection Management page. 
- *Sample Sheet Extraction* - Metalnx can extract metadata from an Illumina sample sheet if organized properly and included as one of the files in a bulk ingest of files.

*****Metalnx Components:***** 

- Metalnx relies on Apache Tomcat to provide the necessary Java servlet environment to run the application.
- *Metalnx Web* - the application.  Metalnx is a Java based application.  The application is provided in an .rpm package or .deb package which requires that Apache Tomcat be installed first.  The application can be installed as a `.war` file manually if so desired.
- *[Metalnx Remote Monitor Daemons (RMD)][RMD_github_repo]*  The Metalnx RMD is a small, lightweight daemon which is installed (via .rpm or .deb package) on each iCAT and resource server in the grid.  Metalnx RMD provides, on demand, basic availability information of each server in the iRODS grid which allows Metalnx to report on the overall health of the grid.  
- *[Metalnx microservices][MSI_github_repo]*  Metalnx (optional) provides a collection of iRODS microservices which, if installed, will automatically extract and add to the ICAT embedded metadata in files uploaded to iRODS via Metalnx.  The microservices are provided as an .rpm file.  The microservice package must be installed in the microservices directory on each iRODS server in the grid in order for the functionality to work.

Figure 2 (below) illustrates an iRODS grid with Metalnx deployed:

![alt text] [2]
[2]: IMAGES/Install_figure_2.png "Figure 2 - An Example iRODS Grid with Metalnx Installed"

In figure 2 items 1-4 are the same as in Figure 1 above.  In addition:

<ol start=5>
<li> The server running Apache Tomcat and Metalnx.  Metalnx does NOT require a separate server.  It can be run on the ICAT, resource server, or any virtual machine running Linux on the local area network, where you can install and run Apache Tomcat. We show Metalnx running on a separate server to add clarity to the diagram. </li> 
<br>
The Metalnx RDMBS.  Metalnx requires its own small database.  The database manager must be either MySQL or PostgreSQL.  The database does NOT have to be on unique, attached storage.  It can be on any server in the local area network, including the ICAT database server if desired.  Again we show the data storage as local direct attach for clarity in the drawing.
<br>
<br>
<li> Metalnx Remote Monitor Daemons (RMD).   Metalnx Remote Monitor Daemons are installed on the ICAT server and each iRODS resource server.  The RMD is a small daemon which runs as the user iRODS and listens for a request on a port of the customer’s choosing via a configuration file (port 8000 is the default).  When a Metalnx user views the dashboard page it issues update requests to the RMD daemons in the grid which will report memory, disk, and iRODS application status via JSON packets back to Metalnx.  The Metalnx application parses the information to build the dashboard and drill down pages.  (<strong> Note: </strong> Metalnx RMD is not required for the application to work, but without the Dashboard page will have incomplete information and show each iRODS server without RMD to be in a <em> Warning </em> state.) </li>
<br>
<li> (NOT SHOWN IN FIGURE)  Metalnx Micro Services.  Metalnx microservices is a collection of iRODS microservices which, if installed on each server in the iRODS grid, will automatically extract metadata from .jpg, .bam, .cram, and .vcf files and add the metadata into the ICAT as part of a file upload from the Metalnx collections interface. The microservice file also contain a tool for extracting all metadata in a <strong> Ilumina </strong> sample sheet provided the sample sheet is setup properly and ingested with the sequencer data into iRODS. 
</li>
</ol>
 
[[Back to: Table of Contents](#TOC)]

----------
<br>
<font color="#0066CC"> <font size=+2> __Metalnx Packages__ </font></font> <a name="metalnx_packages"></a>

### JFrog Bintray ###

You do not need to build Metalnx from scratch unless you want to. We already provide all packages needed to run Metalnx in your environment. The packages are available on our [Bintray repository](https://bintray.com/metalnx).

You can download each package from the following links:

- Metalnx Web: [RPM](https://bintray.com/metalnx/rpm/emc-metalnx-web#files) and [DEB](https://bintray.com/metalnx/deb/emc-metalnx-web#files/emc-metalnx-web/1.0/pool/main/e/emc-metalnx-web)
- Metalnx MSI: [RPM](https://bintray.com/metalnx/rpm/emc-metalnx-msi#files) and [DEB](https://bintray.com/metalnx/deb/emc-metalnx-msi#files)
- Metalnx RMD: [RPM](https://bintray.com/metalnx/rpm/emc-metalnx-rmd#files) and [DEB](https://bintray.com/metalnx/deb/emc-metalnx-rmd#files)

### Docker ###

Metalnx also has a Docker image that is ready for you to deploy in your environment. For more information about this image, please check [Metalnx Docker](DOCKER.md).

[[Back to: Table of Contents](#TOC)]

----------
<br>
<font color="#0066CC"> <font size=+2> __METALNX WEB INSTALLATION__ </font> <a name="metalnx_installation"></a>

<font color="#000000">

### NOTE: ###

- Ensure that your system meets the minimum requirements before installing Metalnx.
- Command line examples shown in this outline are based on CentOS 7 ([www.centos.org](http://www.centos.org)).

### Installation Process Overview ###

- Verify the minimum system requirements for the ICAT server and iRODS resource servers are met.  This may include installing one or more of Java, Tomcat, PostgreSQL, MySQL, or Python.
-	Verify that iRODS is installed and operational prior to installing Metalnx.
-	Install the appropriate version of the Metalnx remote monitor daemon (RMD) on the ICAT server and each iRODS resource server. For further information on how to install [RMD][RMD_github_repo], refer to [RMD Installation Guide][RMD_installation_guide].
-	Install the Metalnx application on a server running Tomcat.  (**Note:**  Metalnx requires Java, Python, Tomcat, and either MySQL or PostgreSQL to be installed on the server where Metalnx runs in order to operate.  Also, the iRODS ICAT server must be operational in order for Metalnx configuration to complete successfully.)
-	Configure Metalnx as needed to conform to your iRODS environment.
-	Restart Tomcat in order to engage the configuration changes made in Tomcat.
-	Add the Metalnx microservices to the ICAT server and each iRODS resource server to leverage automated metadata extraction. For further information on how to install the Metalnx microservices, refer to the [Metalnx MSI Installation Guide][MSI_installation_guide].

### System Requirements ###

Figure 3 shows the relationship between iRODS and Metalnx components.

![alt text] [3]
[3]: IMAGES/Install_figure_3.png "Figure 3 - Relationship between iRODS/Metalnx components"

##### iRODS #####

Metalnx has been tested with iRODS version 4.1 or later. 

For information on how to install iRODS, please see the [iRODS website](http://www.irods.org/download) and the [iRODS documentation](https://docs.irods.org/).

##### Java Devel #####

The Java development environment is required by Metalnx. The appropriate library can be installed using the yum installation utility (CentOS & Suse) or apt-get (Debian).  For example:

	yum install java-devel
 
##### Apache Tomcat #####

As Metalnx is a Web application, a Web server is required in order to run Metalnx. The Web server used by Metalnx is Apache Tomcat (version 7 or later).

For information about Apache Tomcat, refer to:  [http://tomcat.apache.org/](http://tomcat.apache.org)

Tomcat must be installed prior to setup of Metalnx.  However, there are configuration changes which must be made to Tomcat after installation in order for it to work well with Metalnx.  We outline those steps in the installation process section.

##### Metalnx Database #####

Metalnx requires either a MySQL or PostgreSQL database in order to operate.  By default Metalnx is setup to run with a MySQL database.  PostgreSQL can be used, but will require some manual configuration changes be made after installation. 

The database can be setup either locally (same server where Metalnx runs) or on another host.  The Metalnx RDBMS can be same one used for the iCAT.  However, we recommend you use a database instance on the system where Metalnx will be run to lower overall system overhead and network traffic.

##### Browser Support #####

Metalnx works with: 

- Google Chrome 40.X or later
- Mozilla Firefox 18.X or later
- Safari 7.X or later 
- Internet Explorer 11.X or later
- Microsoft Edge

[[Back to: Table of Contents](#TOC)]

------------- 

<br>
<font color="#0066CC"> <font size=+2> __METALNX INSTALLATION PROCESS__ </font> <a name="metalnx_installation_process"></a>

<font color="#000000">

##### Metalnx RMD Background #####

The Metalnx interface contains a dashboard that provides real-time information about the machines on the grid. For the UI to retrieve all this information, it uses the [RMD (Remote Monitoring Daemon)][RMD_github_repo] which should be installed on each server in the iRODS grid.

Metalnx will run without the RMD package. However, RMD is necessary to allow for complete dashboard and server detail page functions in Metalnx. With this package installed disk, memory, and CPU usage data of each server will be available. 

[[Back to: Table of Contents](#TOC)]

-----------------

<br>
<font color="#0066CC"> <font size=+2> __Apache Tomcat Installation__ </font> <a name="apache_tomcat_installation"></a>

<font color="#000000">

##### Installing Tomcat #####

This section contains information on how to install and setup Apache Tomcat ( *version 7 in our examples* ), the Web server required by Metalnx.

On a linux machine, install Tomcat and related utilities using a package manager:

Redhat-like systems:

	yum install tomcat

Debian-like systems:

	apt-get install tomcat

For information on how to setup Tomcat using a `.zip` file, refer to: [http://tomcat.apache.org/](http://tomcat.apache.org/). 


##### Setup Tomcat for Remote Connections #####

Follow the steps below to allow remote connections to the Tomcat server:

**1)** Verify that you have installed the following packages: 

  * Tomcat

Package installation can be verified with the `rpm` command on CentOS systems and `dpkg` on Debian based systems.  For example, on CentOS 7 the following command will list all tomcat packages:

    # rpm –qa |grep tomcat

You can read through the resulting list to identify packages.  On Debian the command:

    # dpkg –l |grep tomcat

Will perform the same function.

**NOTE:** Depending on the repository structure you may find that you also need to install the package `tomcat-admin-webapps` in order to obtain the Tomcat administrative GUI.

**2)** The Tomcat Manager is a web application that can be used interactively (via an HTML GUI) or programmatically (via a URL-based API) to deploy and manage web applications. In order to connect to the Tomcat server remotely, it is necessary to edit parameters in the Linux firewall to allow this. This is best done by modifying the firewall configuration.  

#### Iptables ####

Using an editor, such as vi, and as root edit the file `/etc/sysconfig/iptables` .  Comment out the line: 

	-A INPUT -j REJECT --reject-with icmp-host-prohibited 

By adding the character “#” to the first column position.

**3)** Next add the following line below the one commented out: 

	-A INPUT -m state --state NEW -m tcp -p tcp --dport 8080 -j ACCEPT

If you plan to allow https also add the line:

	-A INPUT -m state --state NEW -m tcp -p tcp --dport 8443 -j ACCEPT

**NOTE:** If you will use ONLY SSL encryption on your Web connections to Metalnx (https: ) on the first of these added lines substitute port number 8443 where you see port 8080 above. This operation will open the secure https port for use under Tomcat. There is no need for the second line.

**NOTE:**  If you use a graphical firewall editor make your changes with this tool and do not modify the `iptables` file directly.  The GUI changes will override manual edits.

#### Firewall-cmd (CentOS 7) ####

If the `firewall-cmd` command is available on your command line interface, the configuration changes can be achieved by typing the following commands in your terminal:
    
    firewall-cmd --zone=public --add-port=8080/tcp --permanent          # Tomcat configuration
    firewall-cmd --zone=public --add-port=1247/tcp --permanent          # iRODS configuration
    firewall-cmd --zone=public --add-port=1248/tcp --permanent          # iRODS configuration
    firewall-cmd --zone=public --permanent --add-port=20000-20199/tcp   # iRODS configuration
    firewall-cmd --zone=public --permanent --add-port=20000-20199/udp   # iRODS configuration
    firewall-cmd --zone=public --add-port=5432/tcp --permanent          # For PostgreSQL
    firewall-cmd --zone=public --add-port=3306/tcp --permanent          # For MySQL
    firewall-cmd --zone=public --add-service=http --permanent           # Web server (HTTPS disabled, refer to the configuration section)
    firewall-cmd --zone=public --add-service=https --permanent          # Web server
    firewall-cmd –reload

<font size=+1> **NOTE:** If your environment has special network security needs please consult your Network Administrator prior to making firewall changes to ensure the necessary ports are allowed for use on your network and will NOT intefer with other critical network traffic or security policies.
</font>

**4)** In the Tomcat configuration directory (likely in `/usr/share/tomcat/conf`) and add the following lines to the file `tomcat-users.xml` between the tags `<tomcat-users> </tomcat-users>`. This will create a manager user in Tomcat to support remote application management. **NOTE: Take care not to add these lines in sections that are commented out.**

    <role rolename="manager"/>
	<user username="your_username_here" password="your_password_here" 	roles="manager"/>

    <role rolename="admin-gui"/>
    <user username="admin" password="metalnx" roles="manager-gui,admin-gui"/>


Pick any username / password combination you will remember.

Open access for remote connections over the web. In the same directory open the file `server.xml`. The easiest method is to add the following line between the lines: `<Connector port = “8080” protocol=”HTTP/1.1”` and `connectionTimeout=”20000”`:

    address="0.0.0.0"

This line will open access to any system which connects to Tomcat via port 8080.

<font size=+1> **NOTE:** Please consult with your Network Administrator prior to this step if your organization has security policies related to network access.  This line opens Tomcat to any and all connection requests.  It is possible to restrict access to Tomcat.  Please consult the Tomcat documentation for information on how to implement security. </font>

**5)** Restart iptables and tomcat services using the commands:

	systemctl restart iptables
	systemctl restart tomcat

**6)** Connect to the Tomcat machine IP address in your browser via the command

    localhost:8080/manager/html

You will be prompted to provide the username and password added to the `tomcat-users.xm`l file to gain access. The Tomcat Applicaiton Manager screen will be displayed.

![alt text] [4]
[4]: IMAGES/Install_figure_4_tomcat_app_mgr.png "Figure 4 - Tomcat Web Application Manager"

From this page you can enable / disable Metalnx or deploy it from a .war file if this is the version you built. (NOTE:  We recommend using an .rpm or .deb file - setup is easier.)

<font color="#0066CC"> <font size=+2> __Install the Metalnx Application__ </font> <a name="install_metalnx"></a>

<font color="#000000">

**1)** Verify that the Tomcat web server is running using the systemctl command

    systemctl status tomcat

**2)** As root, install the Metalnx application from either the rpm or deb package built.  The packages will have names similar to:

`enc-metalnx-webapp-1.0-1.noarch.rpm`	(CentOS / RHEL like systems)
`emc-metalnx-webapp-1.0-1.noarch.deb`	(Debian like systems)

On CentOS install the Metalnx application package, as root, using the command:

    # rpm –ivh emc-metalnx-webapp-1.0-1.noarch.rpm

On a Debian-like system install the Metalnx application, as root, using the command:

    # dpkg –i emc-metalnx-webapp-1.0-1.noarch.deb

[[Back to: Table of Contents](#TOC)]

-------------- 

<br>
<font color="#0066CC"> <font size=+2> __Configure the Metalnx Database__ </font> <a name="config_metalnx_database"></a>

<font color="#000000">

**NOTE: The Metalnx Database MUST be setup prior to starting Metalnx (the Setup Metalnx step).**

The command to setup the database will vary between whether you will use MySQL or PostgreSQL as the Metalnx database. The steps are logically similar.

**Configure using PostgreSQL**

**1)** Become the user postgres using the command:

    # su – postgres

**2)** Start the utility psql using the command:

    # psql

**3)** Create the database user metalnx for communication with the database.  (You can use a different database user name and password, but doing so will require manual edits to the Metalnx configuration files.) 

    Postgres=# CREATE USER metalnx WITH PASSWORD 'metalnx';

**NOTE:** You can use any database password that you can remember.  The username, `metalnx`, however is fixed.

**4)** Create the database metalnx for metalnx to use.   (The choice of database name `metalnx` is fixed.)

    Postgres=# CREATE DATABASE "metalnx";

**5)** Grant the user metalnx accress rights on the Metalnx database

    Postgres=# GRANT ALL PRIVILEGES ON DATABASE "metalnx" TO metalnx;

**6)** Exit the psql utility with the command:

    Postgres=# \q

Some extra configuration may be needed for Metalnx to be able to authenticate correctly against Postgres.

Open the Postgres HBA configuration file:

	# vim /var/lib/pgsql/data/pg_hba.conf
	
Find the lines that look like this, near the bottom of the file:

	host    all             all             127.0.0.1/32            ident
	host    all             all             ::1/128                 ident

Replace *ident* with *md5* or *trust*:

	host    all             all             127.0.0.1/32            md5
	host    all             all             ::1/128                 md5

Then, start and enable Postgres:

	# systemctl start postgresql
	# systemctl enable postgresql

**Configure using MySQL**

**1)** Become the user mysql root user using the command:

    # mysql – u root -p

**NOTE:** You will have to provide the MySQL root database password established when MySQL was first setup.

**2)** Create the database user metalnx for communication with the database.  (The choice of database name `metalnx` is fixed.) 

    mysql> CREATE USER ‘metalnx’@’<HOSTNAME>’ IDENTIFIED BY ‘metalnx’;

**3** Create the database metalnx for metalnx to use.   (You can choose to use a different username name, but doing so will require manual edits to the Metalnx configuration files.)

    mysql> CREATE DATABASE metalnx;

**4)** Grant the user metalnx accress rights on the Metalnx database

    mysql> GRANT ALL PRIVILEGES ON * . * TO ‘metalnx’@’<HOSTNAME>’;

**5)** Now reload the privileges so they take effect:

    mysql> FLUSH PRIVILIGES:

**6)** Exit the mysql utility with the command:

    mysql> quit;
 
[[Back to: Table of Contents](#TOC)]

-------------- 

<br>
<font color="#0066CC"> <font size=+2> __Setup Metalnx__ </font> <a name="setup_metalnx"></a>

<font color="#000000">

The Metalnx installation package comes with a setup script.  The script setup the Metalnx environment on a CentOS server using MySQL as the Metalnx database (default) or using PostgreSQL (a script option).   The script will help to setup the Metalnx in other environments, but additional manual configuration changes may be needed.

Once the RPM package is installed, run the Metalnx setup script, as root:

    # python /<metalnx-script-dir>/setup_metalnx.py

In case `setup_metalnx.py` cannot find the `pyscopg2` or `MySQL` modules, please run the following commands to install the required dependencies according to your database (MySQL or Postgres) and your OS distribuition:

	# apt-get install python-psycopg2    # Ubuntu - PostgreSQL
	# apt-get install python-mysqldb     # Ubuntu - MySQL
	# yum install python-psycopg2	     # CentOS - PostgreSQL
	# yum install MySQL-python           # CentOS - MySQL

The script is organized in steps, so you can easily identify what is changed during its execution. The script will request several pieces of information and it will make sure all dependencies required by Metalnx are met. Details below:

     Executing config_java_devel (1/13)   

The first step checks if the packakge java_devel exists in your system. If it does, the script goes to step two:

      Executing config_tomcat_home (2/13)   

It will ask for the tomcat home directory. Provide the full pathname to the Tomcat home directory, by default it is `/usr/share/tomcat` ). The steps 3 to 6 are automatic, there is no input requried. You can follow what is happenning by reading the output messages.

      Executing config_tomcat_shutdown (3/13)   # Shutdown the tomcat service
      Executing config_metalnx_package (4/13)   # Checks if any Metalnx package already exists in your environment
      Executing config_existing_setup (5/13)    # Saves current installed Metalnx configuration, if any
      Executing config_war_file (6/13)          # Installs the Metalnx WAR file into Tomcat

The step 7 will ask you all the Metalnx Database configuration parameters, as follows:

     Executing config_database (7/13)                                      # Configures database access
     Enter the Metalnx Database type (mysql, postgresql) [mysql]: mysql    # Metalnx database type. By default, it is MySQL
     Enter Metalnx Database Host [localhost]:                              # Database hostname. By default, it is localhost.
     Enter Metalnx Database Port [3306]:                                   # Database port. The default port is 3306 for MySQL and 5432 for PostgreSQL
     Enter Metalnx Database Name [metalnx]:                                # Database name. By default, it is "metalnx".
     Enter Metalnx Database User [metalnx]:                                # Database user. By default, it is "metalnx"
     Enter Metalnx Database Password (it will not be displayed):           # Database password. We do not provide any default value for that.     
       
     
After the Metalnx database configuration, the script tests whether or not the credentials are valid by connecting to the database. If it is successful, the following message is shown:

    * Testing database connection...
    * Database connection successful.

Now, you will be configuring the iRODS parameters to allow Metalnx connect to the grid.

    Enter iRODS Host [localhost]:                                             # iRODS machine's hostname. By default, it is localhost
    Enter iRODS Port [1247]:                                                  # port number used to communicate with the iCAT. By default, it is 1247
    Enter iRODS Zone [tempZone]:                                              # iRODS Zone Name. By default, it is tempZone
    Enter Authentication Schema (STANDARD, GSI, PAM, KERBEROS) [STANDARD]:    # iRODS authentication mechanism. By default, it is STANDARD
    Enter iRODS Admin User [rods]:                                            # iRODS Admin Username. By default, it is rods.
    Enter iRODS Admin Password (it will not be displayed):                    # iRODS Admin Passoword. We do not provide any default value for that.
    
The iRODS credentials are tested to make sure Metalnx is able to connect to the grid. If the connection is successful, the following message is shown:

    * Testing iRODS connection...
    * iRODS connection successful.

After the iRODS configuration step, the script will check if it necessary to restore any previous configuration files.

    Executing config_restore_conf (9/13)
 
The next step is related to HTTPS configuration. 
 
    Executing config_set_https (10/13)
 
The 11th step will ask you to confirm if the params you set for both iRODS and the Metalnx database are correct.

    Executing config_confirm_props (11/13)
        - Confirm configuration parameters

    Metalnx Database Parameters:
        * db.type = mysql
        * db.db_name = metalnx
        * db.port = 3306
        * db.host = localhsost
        * db.username = metalnx

    iRODS Paramaters:
        * irods.port = 1247
        * irods.auth.scheme = STANDARD
        * irods.zoneName = tempZone
        * jobs.irods.username = rods
        * irods.host = icat.localdomain
    
    Do you accept these configuration paramaters? (yes, no) [yes]:

By saying no, the execution of the script is aborted. By saying yes, the script will create the configuration files necessary for the Metalnx Web application to run.

    * Creating Database properties file...
    * Database properties file created.
    * Creating iRODS properties file...
    * iRODS properties file created.

The following steps are the two last steps for configuring Metalnx.

    [*] Executing config_tomcat_startup (12/13)
       - Starting tomcat back again
    Redirecting to /bin/systemctl start  tomcat.service

    [*] Executing config_displays_summary (13/13)
       - Metalnx configuration finished

If everything is successfull, the script will show you where to access Metalnx:

    Metalnx configuration finished successfully!
    You can access your Metalnx instance at:
        http://<hostname>:8080/emc-metalnx-web/login/
    
    For further information and help, refer to:
        https://github.com/Metalnx/metalnx-web

By default, Metalnx creates the `database.properties` file structured to interface with a MySQL database.  Selecting `y` to this question will change the settings to use PostgreSQL.  

Following these answers the script will install the application on the Tomcat server and setup the base configuration files. (in this example we answered '`y`'). 

    Using postgresql.
    Removing old instances of Metalnx Web application...Done!
    Moving the WAR package to the correct directory... Done!
    Creating iRODS environment configuration file...Done!
    Setting database configuration file...Done!


Follow these actions the following question will be asked.

    Do you want to enable HTTPS on Tomcat and Metalnx? [Y/n]:  

If you respond “n” Tomcat and Metalnx will be setup to respond to http protocol on port 8080. If you press “Y” or ‘y’ it will configure Tomcat to use SSL encryption (https) for its connections on port 8443.

**Note:** If you answer “Y” (the default) Tomcat and Metalnx will be setup to respond only using the https protocol on port 8443.  

If you encounter a message in the script that reads:

    Starting Tomcat ... Failed. Tomcat could not be started. Please do it manually.

This message means that the installation script could not restart Tomcat on its own.  You will need to take this action manually using the systemctl command (see below).   This is a known issue.

     # systemctl start tomcat

<br>
**Modify the Metalnx Configuration for PostgreSQL** 

If you used PostgreSQL as the database for Metalnx the setup script above should automatically convert the MySQL settings to PosgreSQL settings in in the `database.properties` file.  However, if for some reason you need to manually edit the database properties configuration file.  The steps for this are below.

**1)** Stop the tomcat service.

    # systemctl stop tomcat

**2)** Change your directory to the Metalnx configuration parameters directory.  It is located under the tomcat directory tree.  The example below shows the location assuming a standard Tomcat installation on CentOS.  If you manually installed Tomcat adjust the pathnames accordingly.

    # cd /usr/share/tomcat/webapps/emc-metalnx-web/WEB-INF/classes

Using an editor, such as vi, and as root edit the file database.properties .  Make the following changes:

Change the line which reads: 

`db.driverClassName=com.mysql.jdbc.Driver`

to read:

`db.driverClassName= org.postgresql.Driver`

Change the line which reads: 

`db.url=jdbc:mysql://localhost:3306/metalnx`

to read:

`db.url= jdbc:postgresql://localhost:5432/metalnx`

(**NOTE:**  adjust the hostname, postgreSQL port number, and Metalnx database name as appropriate.)

Change the line which reads: 

`hibernate.dialect=org.hibernate.dialect.MySQL5Dialect`

to read:

`hibernate.dialect=org.hibernate.dialect. PostgreSQLDialect`

**3)** Restart the tomcat service:

    # systemctl start tomcat

[[Back to: Table of Contents](#TOC)]

-------------- 

<br>
<font color="#0066CC"> <font size=+2> __Accessing Metalnx__ </font> <a name="accessing_metalnx"></a>
<font color="#000000">


After following all these instructions, the system is ready to run Metalnx. 

Open a browser and enter on the web browser address line:

Using http protocol:

    http://<IP_ADDRESS_OF_TOMCAT_SERVER>:8080/emc-metalnx-web/login/ 

Using https protocol:

    https://<IP_ADDRESS_OF_TOMCAT_SERVER>:8443/emc-metalnx-web/login/

For example, if your metalnx sever was named `metalnx1`:

    http://metalnx1:8080/emc-metalnx-web/login/

<br>
If the connection is successful you should reach the login screen shown below.

![alt text] [6]
[6]: IMAGES/Install_figure_6_metalnx_welcome.png "Figure 6 - Metalnx Login Screen"

Log in with the default iRODS admin username and password setup when iRODS was installed.  Typically this will be username: **rods** and the password **rods**. If successful the Metalnx dashboard page will be displayed as below:

![alt text] [7]
[7]: IMAGES/Install_figure_7_metalnx_dashboard.png "Figure 7 - Metalnx Dashboard"

[[Back to: Table of Contents](#TOC)]

-------------- 

<br>
**Modifing properties files directly**

*NOTICE: If you already run the script and you are able to access the Metalnx UI, this section can be skipped. This is troubleshooting section in case an Authentication Error happens in the UI.*

Metalnx uses configuration files to keep the correct parameters for your environment. Although it is strongly recommended that you use the Metalnx setup script for any configuration changes, you can directly modify these files to set up the parameters you prefer.

There are 7 configuration files in total and they are located under `<your-tomcat-directory>/webapps/emc-metalnx-web/WEB-INF/classes`.

* **database.properties**: database connection and authentication parameters
* **irods.environment.properties**: iRODS connection and authentication parameters
* **security.properties**: security parameters (tells Metalnx which credentials are encoded)
* **log4j.properties**: set the log levels for all frameworks used by Metalnx
* **msi.properties**: enable/disable the illumina and photo-metadata-extraction microservices
* **mysql.properties**: basically has the same structure as database.properties, but specific for MySQL databases
* **postgresql.properties**: basically has the same structure as database.properties, but specific for PostgreSQL databases
                               
If an Authentication Error is shown on the UI, it is likely that there is a misconfiguration in database.properties, irods.environment.properties or security.properties.

First of all, make sure the credentials shown in all these files are actually correct, the Metalnx database exists and the user in *database.properties* file (`db.username`) has rights to modify it (Refer to: [Configure the Metalnx Database](#config_metalnx_database)) and the iRODS credentials belong to a rodsadmin user.

Second, make sure there are no spaces between configuration parameters and their values.

	db.driverClassName = com.mysql.jdbc.Driver   # WRONG
	db.driverClassName= com.mysql.jdbc.Driver    # WRONG
	db.driverClassName =com.mysql.jdbc.Driver    # WRONG
	db.driverClassName=com.mysql.jdbc.Driver     # CORRECT
                
Finally, modify the **security.properties** file. For security reasons, during the installation process, some configuration parameters (passwords) are encoded. This file tells Metalnx which parameters were encoded and need to be decoded when the application starts. 

However, those parameters are encoded only if the Metalnx setup script is used. Since we are modifing *properties* files directly, they will be plain text. Then, Metalnx needs to know that it is no longer necessary to decode them. We do so by either removing *db.password,jobs.irods.password* or commenting the entire line out:

	encoded.properties=                                         # CORRECT
	# encoded.properties=db.password,jobs.irods.password        # CORRECT


[[Back to: Table of Contents](#TOC)]


<br>
<font color="#0066CC"> <font size=+2> __Metalnx Install Checklist__ </font> <a name="metalnx_checklist"></a>
<font color="#000000">

<table>
   <tr>
      <td> Complete </td><td> Step </td><td> Description </td>
   <tr>
      <td> </td> <td> 1 </td> <td> Verify 4.1x is installed and operational </td>
   <tr>
      <td> </td> <td> 2 </td> <td> Install Python 2.6 or later on the iCAT and resource servers </td>
   <tr>
      <td> </td> <td> 3 </td> <td> Install Java 1.8 or later on the Metalnx execution server </td>
   <tr>
      <td> </td> <td> 4 </td> <td> Install Apache Tomcat 7 or 8 on the Metalnx execution server </td>
   <tr>
      <td> </td> <td> 5 </td> <td> Install the package java-devel on the Metalnx execution server </td>
   <tr>
      <td> </td> <td> 6 </td> <td> Install and/or verify that either PostgreSQL or MySQL is installed, initialized, and operational on the Metalnx execution server </td>
   <tr>
      <td> </td> <td> 7 </td> <td> Install the <a href="https://github.com/sgworth/metalnx-rmd/">Metalnx RMD package</a> on the iCAT and each iRODS resource server </td>
   <tr>
      <td> </td> <td> 8 </td> <td> Confirm RMD is operational on each iRODS server via remote connection </td>
   <tr>
      <td> </td> <td> 9 </td> <td> Modify Tomcat configuration to allow remote connectivity </td>
   <tr>
      <td> </td> <td> 10 </td> <td> Verify that ports 8080 (and 8443 if appropriate) are opened to receive web traffic. </td>
   <tr>
      <td> </td> <td> 11 </td> <td> Install the Metalnx application on the Metalnx execution server </td>
   <tr>
      <td> </td> <td> 12 </td> <td> Configure the Metalnx database on the Metalnx execution server </td>
   <tr>
      <td> </td> <td> 13 </td> <td> Setup Metalnx using the setup script (config_metalnx.sh) </td>
   <tr>
      <td> </td> <td> 14 </td> <td> Install <a href="https://github.com/sgworth/metalnx-msi/">Metalnx Microservices</a> on each server in the iRODS grid </td>
	<tr>
      <td> </td> <td> 15 </td> <td> Verify access to the Metalnx application </td>
   </tr>
</table>

Use & Enjoy Metalnx!

[[Back to: Table of Contents](#TOC)]

<br>
<font color="#0066CC"> <font size=+2> __Integration with LDAP__ </font> <a name="LDAP"></a>
<font color="#000000">

##### Authentication using LDAP (Lightweight Directory Access Protocol) #####

The diagram below illustrates how Metalnx syncs user information with LDAP (Lightweight Directory Access Protocol): 

![alt text] [8]
[8]: IMAGES/ldap_sync_diagram.png "Figure 8 - LDAP Syncing With Metalnx"

1.	In the LDAP server, Metalnx users must be members of a unique group that will be imported into iRODS.
2.	In iRODS, authentication must be set to PAM (Pluggable Authentication Modules). Refer to iRODS documentation for more information. 
3.	Metalnx must be set to work with PAM authentication.  
4.	New users will be created in iRODS server based on information retrieved from group. 
5.	New users will be created in Metalnx based on information retrieved from iRODS.

##### Setting PAM Authentication #####

Metalnx supports PAM authentication, as iRODS does. To setup the PAM authentication on the UI, there are two required steps:

1.	Configure the Tomcat JVM to handle SSL requests. This is required because iRODS needs a SSL connection in order to authenticate users via PAM.
2.	Change the configuration files on your instance of Metalnx.	

##### Configuring your JVM(Java Virtual Machine) to handle SSL connections #####

For the Metalnx UI to authenticate users via PAM, it is necessary to include the iRODS server certificate into the JVM keystore of your running Tomcat instance. The `keytool` utility provides the functionality needed to manipulate the keystore.

The first thing you is to put the iRODS server certificate into the machine running the Tomcat instance. Then, you'll need to locate your JRE environment files, that is, generally, located underneath `/usr/lib/jvm/`.

Inside the `jre/lib/security` directory there is a file called `cacerts`. This is the store file for the trusted certification authorities. We need to insert the iRODS server certificate into this store. For example:

    $ sudo keytool -import -keystore <path_to_your_cacerts_file> -file <path_to_your_certificate_file>

	Enter keystore password:  <type_your_password_here>
	Owner: CN=localhost, OU=EMC, O=EMC, L=CH, ST=North Carolina, C=US Issuer: CN=localhost, OU=COMPANY, O=EMC, L=CH, ST=North Carolina, C=US Serial number: c584501847c209bb Valid from: Mon Mar 10 12:24:19 GMT-05:00 2014 until: Tue Mar 10 12:24:19 GMT-05:00 2015 Certificate fingerprints: MD5: F8:82:D7:1B:F2:2F:21:CE:53:4A:C9:5B:76:BA:9E:08 SHA1: 29:F3:95:36:4C:69:76:FD:8B:CF:C4:5C:15:79:AE:83:1F:27:57:B6 SHA256: 88:8C:95:49:41:27:60:01:A1:75:A2:AB:CD:6A:85:01:E8:9F:61:B6:27:43:3D:E2:5C:C5:57:71:90:A6:E8:19 Signature algorithm name: SHA1withRSA Version: 3

	Extensions:
    #1: ObjectId: 2.5.29.35 Criticality=false AuthorityKeyIdentifier [ KeyIdentifier [ 0000: 8D 02 6D 2E 54 1A 80 BB 0C 7E 6A CE E2 82 0A B8 ..m.T.....j..... 0010: 70 35 C1 9F p5.. ] ]

	#2: ObjectId: 2.5.29.19 Criticality=false BasicConstraints:[ CA:true PathLen:2147483647 ]

	#3: ObjectId: 2.5.29.14 Criticality=false SubjectKeyIdentifier [ KeyIdentifier [ 0000: 8D 02 6D 2E 54 1A 80 BB 0C 7E 6A CE E2 82 0A B8 ..m.T.....j..... 0010: 70 35 C1 9F p5.. ] ]

	Trust this certificate? [no]: yes 
 
	Certificate was added to keystore

Notice that it asks for a password to view or modify the `cacerts` file. On the first access to this file, use the password `changeit`.

Once the certificate is added to keystore, the JVM will be ready to handle SSL connections with the iRODS server.

##### Editing Authentication Properties on Metalnx #####

In order to configure Metalnx to use PAM authentication, edit the file `irods.environment.properties` which can be found on the `webapps/emc-metalnx-web/WEB-INF/classes` directory, under the Tomcat root installation folder. 

    # iRODS parameters
	irods.host=<hostname>
	irods.port=1247
	irods.zoneName=<zone_name>
	irods.auth.scheme=STANDARD

	# Jobs parameters
	jobs.irods.username=rods
	jobs.irods.password=<password>
	jobs.irods.auth.scheme=STANDARD
	runSyncJobs=true

The parameters below set the authentication scheme used by Metalnx:

	irods.auth.scheme
	jobs.irods.auth.scheme
 
The following values are allowed for those properties: 

* The STANDARD method will authenticate the user against the iCAT database, using the password that is stored there. 
* The PAM scheme will authenticate the user against the PAM configuration on the iRODS server machine. It's possible to set the synchronization jobs to authenticate against the iCAT and UI authentication against the PAM settings, since these parameters work independently. The STANDARD authentication scheme does not required the SSL setup on your JVM.

An example of configuration is:

    # iRODS parameters
	irods.host=myicat.local.localdomain
	irods.port=1247
	irods.zoneName=tempZone
	irods.auth.scheme=PAM

	# Jobs parameters
	jobs.irods.username=jobs_user
	jobs.irods.password=j0BsP4sSW0rd!
	jobs.irods.auth.scheme=STANDARD
	runSyncJobs=true

**NOTE:** Always check the new line format in your properties files. Avoid Windows-editors to eliminate new line characters being inserted to prevent errors on Linux environments. 

[RMD_github_repo]: https://github.com/sgworth/metalnx-rmd/
[RMD_installation_guide]: https://github.com/sgworth/metalnx-rmd/blob/master/docs/INSTALL.md
[MSI_github_repo]: https://github.com/sgworth/metalnx-msi/
[MSI_installation_guide]: https://github.com/sgworth/metalnx-msi/blob/master/docs/INSTALL.md
