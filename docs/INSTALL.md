<font color="#3892CF"> EMC METALNX
===================================

<font color="#3892CF"> Installation Guide
=========================================

<font color="#A6A6A6"> <font size=+2> Revision 1.0 

6/2016 </font>

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
3. [Overview](#metalnx_overview)
3. [Metalnx Installation](#metalnx_installation)
4. [Metalnx Installation Process](#metalnx_installation_process)
5. [Metalnx RMD Installation](#metalnx_RMD_installation)
6. [Metalnx RMD Commands](#metalnx_RMD_commands)
7. [Apache Tomcat Installation](#apache_tomcat_installation)
8. [Install the Metalnx Application](#install_metalnx)
9. [Configure the Metalnx Database](#config_metalnx_database)
10. [Setup Metalnx](#setup_metalnx)
11. [Installing The Metalnx Micro Services](#install_metalnx_microservices)
12. [Using Metalnx Micro Services](#using_metalnx_microservices)
12. [Accessing Metalnx](#accessing_metalnx)

</font>

----------------------------------

<br>
<font color="#0066CC"> <font size=+2> __INTRODUCTION (Read First!)__ </font> <a name="introduction"></a>

<font color="#000000">
Metalnx is a web application designed to work alongside the iRODS (integrated Rule-Oriented Data System) [ [irods.org](http://www.irods.org)]. It provides a graphical UI that can help simplify most administration, collection management, and metadata management tasks removing the need to memorize the long list of icommands.

This installation guide will provide information on how to install the components necessary to run Metalnx along with installation the application. 

At a high level Metalnx is dependent on the following software components being available:

- The Metalnx application, the Metalnx Remote Monitor Daemon (RMD), and the Metalnx iRODS msi (micro service) files all being built and available as either .rpm or .deb files.
- Apache Tomcat (for running EMC Metalnx which is a Java servlet)
- iRODS runtime API
- MySQL or PostgreSQL (we use a database to hold Metalnx operational information)
- Java

__Assumptions__

In this installation guide, to fully install Metalnx, we will:

- Assume that iRODS is installed and that an ICAT server is operational (**Note:** If you need help with installing iRODS please check the documentation for the current release at [docs.irods.org](https://docs.irods.org/) .)
- Assume that the Java runtime and API packages are installed
- Show how to install Tomcat and configure a basic setup (Tomcat version 7 & 8 have been tested.)
- Show how to install Metalnx and configure it work with the iRODS ICAT and use a local MySQL or PostgreSQL RDBMS for holding its information.
- Show how to install the _Metalnx Remote Monitor Daemon (RMD)_ packages on the ICAT and resource servers.  Metalnx uses these packages to monitor and report the active status of the iRODS grid.
- Show how to install the _Metalnx micro services package_ which provides automated metadata extraction for .jpeg, .bam, .cram, .vcf, and some __Illumnia__ manifest files

Metalnx has been tested on the following Linux distributions as indicated:

- CentOS 7 – all functional testing performed.
- CentOS 6 – verified Metalnx will install and start.
- Ubuntu 14 – verified Metalnx will install and start.

Metalnx will work with iRODS 4.1 or later.  It has been tested the most using iRODS 4.1.8.

##### iRODS Background #####
 
iRODS is best described as middleware. It is a software framework that provides capabilities for:

- Storing and managing information (files, objects, etc.) across multiple servers in a data grid using uniform commands across the grid.
- Supporting extensive user defined metadata tagging of objects placed under iRODS management.
- A rules/policy engine which can be adapted via user defined rules and micro services to perform any computer actionable activity on data.
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

*****Key features of Meatalnx include:*****

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
- *Metalnx*- the application.  Metalnx is a Java based application.  The application is provided in an .rpm package or .deb package which requires that Apache Tomcat be installed first.  The application can be installed as a `.war` file manually if so desired.
-	*Metalnx Remote Monitor Daemons (RMD).*  The Metalnx RMD is a small, lightweight daemon which is installed (via .rpm or .deb package) on each iCAT and resource server in the grid.  Metalnx RMD provides, on demand, basic availability information of each server in the iRODS grid which allows Metalnx to report on the overall health of the grid.  
- *Metalnx micro services.*  Metalnx (optional) provides a collection of iRODS micro services which, if installed, will automatically extract and add to the ICAT embedded metadata in files uploaded to iRODS via Metalnx.  The micro services are provided as an .rpm file.  The micro service package must be installed in the micro services directory on each iRODS server in the grid in order for the functionality to work.

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
<li> (NOT SHOWN IN FIGURE)  Metalnx Micro Services.  Metalnx micro services is a collection of iRODS micro services which, if installed on each server in the iRODS grid, will automatically extract metadata from .jpg, .bam, .cram, and .vcf files and add the metadata into the ICAT as part of a file upload from the Metalnx collections interface. The micro service file also contain a tool for extracting all metadata in a <strong> Ilumina </strong> sample sheet provided the sample sheet is setup properly and ingested with the sequencer data into iRODS. 
</li>
</ol>
 
[[Back to: Table of Contents](#TOC)]

----------
<br>
<font color="#0066CC"> <font size=+2> __METALNX INSTALLATION__ </font> <a name="metalnx_installation"></a>

<font color="#000000">

### NOTE: ###

- Ensure that your system meets the minimum requirements before installing Metalnx.
- Command line examples shown in this outline are based on CentOS 7 ([www.centos.org](http://www.centos.org)).

### Installation Process Overview ###

- Verify the minimum system requirements for the ICAT server and iRODS resource servers are met.  This may include installing one or more of Java, Tomcat, PostgreSQL, MySQL, or Python.
-	Verify that iRODS is installed and operational prior to installing Metalnx.
-	Install the appropriate version of the Metalnx remote monitor daemon (RMD) on the ICAT server and each iRODS resource server.
-	Configure RMD on each server if the default configuration does not meet the requirements of your environment.
-	Install the Metalnx application on a server running Tomcat.  (**Note:**  Metalnx requires Java, Python, Tomcat, and either MySQL or PostgreSQL to be installed on the server where Metalnx runs in order to operate.  Also, the iRODS ICAT server must be operational in order for Metalnx configuration to complete successfully.)
-	Configure Metalnx as needed to conform to your iRODS environment.
-	Restart Tomcat in order to engage the configuration changes made in Tomcat.
-	Add the Metalnx micro services to the ICAT server and each iRODS resource server to leverage automated metadata extraction.

### System Requirements ###

Figure 3 shows the relationship between iRODS and Metalnx components.

![alt text] [3]
[3]: IMAGES/Install_figure_3.png "Figure 3 - Relationship between iRODS/Metalnx components"

##### iRODS #####

Metalnx has been tested with iRODS version 4.1 or later. 

For information on how to install iRODS, please see: [http://www.irods.org/download/](http://www.irods.org/download) and [docs.irods.org](https://docs.irods.org/)

##### Python #####

Python 2.6 or later version is required to run the RMD service and must be installed on the ICAT and each iRODS Resource server that will run RMD.

For information on how to install Python, refer to:  [https://www.python.org/](https://www.python.org)

##### Java Devel #####

The Java development environment is required by Metalnx.    The appropriate library can be installed using the yum installation utility (CentOS & Suse) or apt-get (Debian).  For example:

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

The Metalnx interface contains a dashboard that provides real-time information about the machines on the grid. For the UI to retrieve all this information, it uses the RMD (Remote Monitoring Daemon) which should be installed on each server in the iRODS grid.

RMD is a lightweight webserver that accepts limited HTTP requests and responds with JSON data. There are a few pre-defined requests to which RMD is programmed to respond to. It runs as a Linux-service with the server name:  **rmd**.

RMD requires Python (version 2.6 or later) be installed on the ICAT and each iRODS resource server.  Please, note that iRODS should be setup on the server prior to RMD installation.

Metalnx will run without the RMD package. However, RMD is necessary to allow for complete dashboard and server detail page functions in Metalnx. With this package installed disk, memory, and CPU usage data of each server will be available. 

[[Back to: Table of Contents](#TOC)]

<br>
<a name="metalnx_RMD_installation"></a>
##### Metalnx RMD Installation #####

RMD can be built as distribution-specific installation packages using the build instructions. 

Install the RMD package on CentOS as root via the command:

 	# rpm -ivh emc-metalnx-rmd-1.0-1.noarch.rpm

Install the RMD package on a Debian distribution as root via the command:

	 # dpkg -i emc-metalnx-rmd-1.0-1.deb

##### Controlling Metalnx RMD #####

By default, the RMD runs on port 8000. This property is editable in the configuration file of the daemon, located at <span style="font-family: Courier New;">  /etc/rmd/rmd.conf: </span> 

     [daemon]
     ip=0.0.0.0
     port=8000
    
     [irods]
     server_logs_dir=/var/lib/irods/iRODS/server/log
     log_lines_to_show=20

The lines in this file correspond as follows:

- `ip:` The IP address should not be changed. The value <span style="font-family: Courier New'"> 0.0.0.0 </span> is set for the machine to be visible by outside requests.
- `port:` the port on where RMD should listen to requests. This can be changed to meet any firewall or security needs of your environment.
-  `server_logs_dir:` the directory where iRODS server logs are kept.
-  `log_lines_to_show:` the number of lines to get from the end of the iRODS server log to show in the Metalnx UI on the server details page. This is set initially to the last 20 lines

**NOTE:** If you change the port number for RMD in the file <span style="font-family: Courier New;">  /etc/rmd/rmd.conf </span> you must ALSO change the port number that Metalnx knows to communicate with RMD at.  This must be done after Metalnx is installed.  We describe how to do this in the **[Setup Metalnx](#setup_metalnx)** section.

[[Back to: Table of Contents](#TOC)]

<br>
<a name="metalnx_RMD_commands"></a>
##### Metalnx RMD Commands #####

Metalnx RMD responds to the following commands sent to is over the listen port.


<table>
	<tr>
		<td><h4>Request</td><td><h4>Result</td>
	<tr>
		<td><span style="font-family: Courier New;"> / </span></td>
		<td> Returns all the other commands in a single JSON-like object. For development purposes, this call should be avoided due to its long response time. </td>
	<tr>
		<td><span style="font-family: Courier New;"> /cpu </span></td>
		<td> CPU related information. </td>
	<tr>
		<td><span style="font-family: Courier New;">/cpustat</span></td>
		<td> CPU usage statistics. </td>
	<tr>
		<td><span style="font-family: Courier New;">/disk </span></td>
		<td> Disk and partition information of the system. </td>
	<tr>
		<td> <span style="font-family: Courier New;">/irodslogs </span></td>
		<td> The last pre-defined number of lines of the iRODS server log. This number is set in the RMD configuration file. (see Controlling section above) </td>
	<tr>
		<td> <span style="font-family: Courier New;">/irodsstatus </span></td>
		<td> Status of the iRODS process. </td>
	<tr>
		<td><span style="font-family: Courier New;">/memory </span></td>
		<td> Memory-related data. </td>
	<tr>
		<td> <span style="font-family: Courier New;">/mounts </span></td>
		<td> Lists all the file systems mounted on the current machine. </td>
	<tr>
		<td> <span style="font-family: Courier New;">/serverstatus </span></td>
		<td> System-wide status, taking into consideration all the others specific status listed above. </td>
	<tr>
		<td> <span style="font-family: Courier New;">/version </span></td>
		<td> Returns a JSON-like object containing the version and release numbers for the current instance of RMD. </td>
</table>

##### Confirm RMD Acccess #####

Once RMD is installed and configured, a quick test can be done to ensure that RMD is correctly working. 

Open a browser window and access: `http://<IP_OF_THE_RMD_MACHINE>:<PORT>/disk`
It should list all the disk-related information of your machine in JSON format.  For example:

    http://192.168.1.157:8000/disk

##### RMD Troubleshooting #####

If a firewall is set up on the iRODS server, make sure that the port where RMD listens is opened. On IPTables it can be done by adding the following line to the `iptables.conf` file for port 8000 and reloading iptables:

 	-A INPUT -m state --state NEW -m tcp -p tcp --dport 8000 -j ACCEPT

If the RMD process get stuck, remove the PID file located at `/var/run/rmd.pid` and kill the process.

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

**2)** The Tomcat Manager is a web application that can be used interactively (via an HTML GUI) or programmatically (via a URL-based API) to deploy and manage web applications. In order to connect to the Tomcat server remotely, it is necessary to edit parameters in the Linux firewall to allow this. This is best done by modifying the iptable rules in the base firewall configuration.  

Using an editor, such as vi, and as root edit the file `/etc/sysconfig/iptables` .  Comment out the line: 

	-A INPUT -j REJECT --reject-with icmp-host-prohibited 

By adding the character “#” to the first column position.

**3)** Next add the following line below the one commented out: 

	-A INPUT -m state --state NEW -m tcp -p tcp --dport 8080 -j ACCEPT

If you plan to allow https also add the line:

	-A INPUT -m state --state NEW -m tcp -p tcp --dport 8443 -j ACCEPT

**NOTE:** If you will use ONLY SSL encryption on your Web connections to Metalnx (https: ) on the first of these added lines substitute port number 8443 where you see port 8080 above. This operation will open the secure https port for use under Tomcat. There is no need for the second line.

**NOTE:**  If you use a graphical firewall editor make your changes with this tool and do not modify the iptables file directly.  The GUI changes will override manual edits.

<font size=+1> **NOTE:** If your environment has special network security needs please consult your Network Administrator prior to making ipTable changes to ensure the necessary ports are allowed for use on your network and will NOT intefer with other critical network traffic or security policies.
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

**1)** Prior to running the setup script stop the Tomcat sever.  This can be done with the command (run as root):

    # systemctl stop tomcat

**2)** Run the Metalnx setup script, as root:

    # /opt/emc/config_metalnx.sh

This script will request several pieces of information. Details below:

    Enter the Tomcat Home Directory:   

(Provide the full pathname to the Tomcat home directory, likely: `/usr/share/tomcat` )

    Shutting Tomcat down... 
    Done!
    Checking if /usr/share/tomcat is an actual tomcat installation... Ok!
    Looking for the Metalnx WAR file... Ok!
    No existing installation has been found. Creating a new profile...
    Enter the iCAT hostname:

(Fully qualified hostname of the iCAT server,     hostname.domainname)

    Enter the iCAT port:
(port number used to communicate with the iCAT,   usually the port number is 1247)

    Enter the Zone Name:
(The iRODS zone name, the default iRODS zone name is `tempZone`)

    Enter the iRODS administrator username:
(The iRODS administrator username, the default name provide by iRODS is “rods”)

    Enter the iRODS administrator password (it won’t be displayed): 
(The iRODS administrator password, this is the password used to setup the iRODS administrator)

    By default, Metalnx uses MySQL as database.  Would you like to use PostgreSQL instead [y/N] 

My default, Metalnx creates the `database.properties` file structured to interface with a MySQL database.  Selecting `y` to this question will change the settings to use PosgreSQL.  

Following these answers the script will install the application on the Tomcat server and setup the base configuration files. (in this example we answered '`y`'. 

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

##### Adjusting the RDM Port Number #####

If you chose to change the RMD port number in the file <span style="font-family: Courier New;">  /etc/rmd/rmd.conf </span> you must also adjust the port number that Metalnx knows to communicate with the RMD daemon at.  To make this change perform the following steps:

**1)** Stop the tomcat service.

    # systemctl stop tomcat

**2)** Change your directory to the Metalnx configuration parameters directory.  It is located under the tomcat directory tree.  The example below shows the location assuming a standard Tomcat installation on CentOS.  If you manually installed Tomcat adjust the pathnames accordingly.

    # cd /usr/share/tomcat/webapps/emc-metalnx-web/WEB-INF/classes

**3)** Using an editor such as `vi` edit the file `irods.environment.properties` to change the line which reads: 

    rmd.connection.port=8000

to read:

    rmd.connection.port=(xxxx)

where (xxxx) is the port number you changed the RMD access port to in the file <span style="font-family: Courier New;">  /etc/rmd/rmd.conf </span>.

[[Back to: Table of Contents](#TOC)]

-------------- 

<br>
<font color="#0066CC"> <font size=+2> __Installing The Metalnx Micro Services__ </font> <a name="install_metalnx_microservices"></a>

<font color="#000000">

The process for installing the Metalnx micro services differs depending on the systems employed in your iRODS data grid.  Just as it was for the Metalnx Remote Monitor Daemon you must install the micro service package on the iCAT server and each resource server in the iRODS grid.

**Install on CentOS system**

As root, use the following command to install the Metalnx micro services

    # rpm -i emc-metalnx-msi-1.0-1.x86_64.rpm

This step will copy the micro service libraries into the proper plugin directory in the iRODS directory tree. 


**Install on a Debian-based system**

At present, we do not have a deb package version of the Metalnx Micro Services.  To install the files on a Debian-like system it is necessary to do one of:

- Copy the micro service libraries from a system where they are already installed to the Debian system
- Unzip the micro service .rpm file to obtain the libraries and copy them to the Debian system

**Method 1: Copy from an Existing System**

On a system where the micro service package is installed first become the user irods and then go to the iRODS micro services directory.  (Remember as the user irods you will be put into the directory `/var/lib/irods`.)

    $ cd plugins/microservices

In this directory make a tar file containing the micro service libraries:

    $ tar -cvf metalnx_microservices.tar libmsiobjjpeg_extract.so libmsiobjput_mdbam.so libmsiobjput_mdmanifest.so libmsiobjput_mdvcf.so

Copy the resulting .tar file out of the micro services directory and delete it:
 
    cp -v metalnx_microservices.tar /tmp/.
    $ rm -v metalnx_microservices.tar

Now copy the micro services tar file from `/tmp/metalnx_microservices.tar` to the Debian system using whatever technique is suitable for your environment.

Now on the Debian system become the user irods and copy the micro services to the iRODS micro service directory on the Debian system.  Assuming the micro service tar file is copied to the `/tmp` directory on the Debian system the commands will look like:

    $  cd plugins/microservices
    $  tar –xvf /tmp/metalnx_microservices.tar

Following this step, as root, on the Debian system restart the iRODS service in order for the micro services to be registered:

    # systemctl restart irods


**Method 2: Extract from the Micro Service .rpm File**

There are two techniques to accomplish this extraction.  The commands involved will vary based on your environment, but we will outline the steps:

Technique 1:

- Make a copy of the .rpm file  (`emc-metalnx-msi-1.0-1.x86_64.rpm`)
- Use a decompression tool which can crack an .rpm file, such as 7Zip, to open the archive and extract the micro service library files (ending in .so)
- Create an archive of the extracted files into format which can be read on Linux (.tar preferred)
- Copy this extracted archive to the Debian system
- Copy the files in the archive to `/var/lib/irods/plugins/microservices`
- Change the ownership on the copied files to the user irods 
- Change the group ownership on the copied files to the group irods   (`# chown -R irods:irods *`)
- Change the permissions on the copied files to 755 (`chmod -R 755 *`)
- Restart the iRODS service

Technique 2:

The second method can be executed on a Linux server.  However, to work it requires the use of a tool called `rpm2cpio` which may or may not be installed on the Linux system were you will do the extraction. In general you will find `rpm2cpio` installed on CentOS and RHEL systems.  By default it usually is not included on Debian systems.  However, the tool is general available and can often be obtained via the apt-get command:

    # apt-get install rpm2cpio

We outline the steps for extraction assuming we are root, we have made a copy of the .rpm file into the system root directory (/), and that the tool `rpm2cpio` is installed on the system.

First extract the micro services and copy them into the iRODS micro services directory:

    # rpm2cpio emc-metalnx-msi-1.0-1.x86_64.rpm | cpio -idmv

Next change to the iRODS micro services directory and change permissions as necessary:

    # cd /var/lib/irods/plugins/microservices
    # chown irods:irods *
    # chmod 755 *

Finally remove the copy of the micro services .rpm file and restart the iRODS service:

    # rm –v / emc-metalnx-msi-1.0-1.x86_64.rpm
    # service irods restart

[[Back to: Table of Contents](#TOC)]

--------------- 
<br>
<font color="#0066CC"> <font size=+2> __Using Metalnx Micro Services__ </font> <a name="using_metalnx_microservices"></a>
<font color="#000000">

The Metalnx micro services described in this document are not anything unique or special.  They are iRODS micro services and a few iRODS rules that were created to assist Metalnx users with automated extraction of metadata during file ingest into iRODS.

These micro services can be modified, extended, or changed as one sees fit via modification of the rule or micro service source, recompilation, and installation of the modifed rule using the iRODS rule system.  

Details on the iRODS rule engine and rules language can be found in the iRODS documentation at [docs.irods.org](http://docs.irods.org) 

The micro services provided with Metalnx, during a file upload operation (an iRODS "iput" action) will open certain file based on suffix type and use related libraries to extract metadata tags which are then fed to the iRODS ICAT using equivalent calls to the "imeta" operation. This will attach the extracted metadata to the object stored in iRODS.  

The micro services provided will extract meta data from  files ending with `.jpeg`, `.bam`, `.cram`, and `.vcf`.  It should be noted that `.vcf` files do not have a regular metadata format.  The micro service attempts to make educated guesses about which unstructured elements are attributes, values, and units.  Given the irregularity of `.vcf` elements it is possible that tag classifications may not match individual desires.

##### Working with Ilumina Sample Sheets #####

The provided micro services will process Ilumina manifest files (sample sheet files) extracting all the data in the sample sheet and applying all the tags across every object stored in iRODS.  In order for this work specific rules must be observed:

1. The manifest file (`manifest.csv`) must be included with a collection of files to be upload via the Metalnx upload capability.
2. The manifest file, along with the genome sample files in the file structure desired, must be collected into a `.tar` file with the filename structure: `<filename>_SSU.tar`.  For example: `example_project_SSU.tar`.

Using the Metalnx upload feature upload the `.tar` file.  During the upload process the micros service will find the `manifest.csv` file, open it, extract all the sample sheet information into iRODS metadata tags (AVU tage in iRODS parlance), and apply this full set of tags onto each file in the `.tar` file as it is copied from the `.tar` image into the iRODS collection.  Note, the file system structure of the `.tar` file will be maintained as the files are copied into iRODS.

[[Back to: Table of Contents](#TOC)]

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
      <td> </td> <td> 3 </td> <td> Install Java 1.8 or later on the MetaLnx execution server </td>
   <tr>
      <td> </td> <td> 4 </td> <td> Install Apache Tomcat 7, or 8 on the MetaLnx execution server </td>
   <tr>
      <td> </td> <td> 5 </td> <td> Install the package java-devel on the MetaLnx execution server </td>
   <tr>
      <td> </td> <td> 6 </td> <td> Install and/or verify that either PostgreSQL or MySQL is installed, initialized, and operational on the MetaLnx execution server </td>
   <tr>
      <td> </td> <td> 7 </td> <td> Install the MetaLnx RMD package on the iCAT and each iRODS resource server </td>
   <tr>
      <td> </td> <td> 8 </td> <td> Confirm RMD is operational on each iRODS server via remote connection </td>
   <tr>
      <td> </td> <td> 9 </td> <td> Modify Tomcat configuration to allow remote connectivity </td>
   <tr>
      <td> </td> <td> 10 </td> <td> Verify that ports 8080 (and 8443 if appropriate) are opened to receive web traffic. </td>
   <tr>
      <td> </td> <td> 11 </td> <td> Install the MetaLnx application on the MetaLnx execution server </td>
   <tr>
      <td> </td> <td> 12 </td> <td> Configure the MetaLnx database on the MetaLnx execution server </td>
   <tr>
      <td> </td> <td> 13 </td> <td> Setup MetaLnx using the setup script (config_metalnx.sh) </td>
   <tr>
      <td> </td> <td> 14 </td> <td> Install MetaLnx Micro Services on each server in the iRODS grid </td>
	<tr>
      <td> </td> <td> 15 </td> <td> Verify access to the MetaLnx application </td>
   </tr>
</table>

Use & Enjoy MetaLnx!

[[Back to: Table of Contents](#TOC)]

<br>
<font color="#0066CC"> <font size=+2> __Integration With LDAP__ </font> <a name="LDAP"></a>
<font color="#000000">

#####Authentication using LDAP (Lightweight Directory Access Protocol)#####

The diagram below illustrates how MetaLnx syncs user information with LDAP (Lightweight Directory Access Protocol): 

![alt text] [8]
[8]: IMAGES/ldap_sync_diagram.png "Figure 8 - LDAP Syncing With Metalnx"

1.	In the LDAP server, MetaLnx users must be members of a unique group that will be imported into iRODS.
2.	In iRODS, authentication must be set to PAM (Pluggable Authentication Modules). Refer to iRODS documentation for more information. 
3.	MetaLnx must be set to work with PAM authentication.  
4.	New users will be created in iRODS server based on information retrieved from group. 
5.	New users will be created in MetaLnx based on information retrieved from iRODS.

##### Setting PAM Authentication #####

MetaLnx supports PAM authentication, as iRODS does. To setup the PAM authentication on the UI, there are two required steps:

1.	Configure the Tomcat JVM to handle SSL requests. This is required because iRODS needs a SSL connection in order to authenticate users via PAM.
2.	Change the configuration files on your instance of MetaLnx.	

##### Configuring your JVM(Java Virtual Machine) to handle SSL connections#####

For the MetaLnx UI to authenticate users via PAM, it is necessary to include the iRODS server certificate into the JVM keystore of your running Tomcat instance. The `keytool` utility provides the functionality needed to manipulate the keystore.

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

#####Editing Authentication Properties on Metalnx#####

In order to configure MetaLnx to use PAM authentication, edit the file `irods.environment.properties` which can be found on the `webapps/emc-metalnx-web/WEB-INF/classes` directory, under the Tomcat root installation folder. 

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

The parameters below set the authentication scheme used by MetaLnx:

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

#####PAM-LDAP Authentication Tool Belt#####







