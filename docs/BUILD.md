<font color="#3892CF"> EMC METALNX WEB BUILD GUIDE
===================================

<font color="#3892CF"> Build From Source Instructions
=========================================

<font color="#A6A6A6"> <font size=+2> Revision 1.0

6/2016 </font>

----------------------------------

<font color="#000000">
Copyright © 2015-16 EMC Corporation.

This software is provided under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

The information in this file is provided “as is.” EMC Corporation makes no representations or warranties of any kind with respect to the information in this publication, and specifically disclaims implied warranties of merchantability or fitness for a particular purpose. 

-------------------------------- 

<br>
<font color="#0066CC"> <font size=+2> __INTRODUCTION__ </font>

<font color="#000000"> <a name="Introduction"></a>

This tutorial guides you through the Metalnx Web compilation pipeline. It explains how to build Metalnx Web from scratch.

## Version
1.0
 
## Depedencies
 
Metalnx depends on JAVA 1.8 or higher and Maven 3.1 or higher. Before starting, you should have already installed Maven in your environment. Maven is a Apache project available on [https://maven.apache.org/download.cgi](https://maven.apacke.org/download.cgi). This website provides download links and you can also find a Maven overview and how to integrate the tool in your system.  You can typically install Maven via a package manager like `yum` or `apt-get`.  For example:

    yum install maven
 
If you desire to build the `rpm` or `deb` package versions of Metalnx you will need the approriate build tools.   For `.rpm` packages that tool is `rpm-build`.

    yum install rpmdevtools rpm rpm-devel rpm-build

For `.deb` files the tool is `dpkg-dev`

    apt-get install dpkg-dev

##Metalnx Repository

Ensure you have the Metalnx git repository in your environment. The Metalnx repository should be as follows:

* src
* packaging
* README
* LICENSE

The `src` folder is where the Metalnx application code is located. `Src` contains all files that will be compiled and, after the compilation, it will also contains the Metalnx web application. 

> The `README` and `LICENSE` files contains general and legal information about Metalnx, respectively.
>  
> Packaging is the folder where RPM and DEB configuration scripts live. They are used to create the Metalnx `.rpm` and `deb` packages. 

## Metalnx Compilation Pipeline

Once you have successfully installed Maven (use the `mvn --version` command to  check that), you can build Metalnx. As Metalnx is a Java, Web-based application, it will be available as a [war](https://docs.oracle.com/cd/E19316-01/820-3748/aduvz/index.html) file at the end of the compilation process. 

Browse to the Metalnx `src` folder in the repository you cloned from the git repository. Listing the `src` folder content you should see:

    emc-metalnx-core
    emc-metalnx-services
    emc-metalnx-shared
    emc-metalnx-ui-admin
    emc-metalnx-ui-user
    emc-metalnx-web
    pom.xml

Under the source folder run the following command for a quickstart:

    $ mvn clean generate-sources package -Ppreprod -Dmaven.test.skip=true 

### Understanding the *mvn* command:

Maven is based around the central concept of a [build lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html). The *clean* phase cleans the project's working directory, *generate-sources* generates source code for inclusion in compilation, *package* takes the compiled code and package it in its distribution format, in this case the format is `.war`.

You do not have to worry about downloading any dependencies. Maven will take care of that for you. Any other package that Metalnx requires will be download from the [Maven repository](http://mvnrepository.com/). After downloading all dependencies, Maven will build all Metalnx subprojects in the following order: *emc-metalnx-core*, *emc-metalnx-services*, *emc-metalnx-shared*, *emc-metalnx-ui-admin*, *emc-metalnx-ui-user* and *emc-metalnx-web*.

### Finding the WAR file

When the build is completed, you will get a "BUILD SUCCESS" message. Now, the Metalnx WAR file exists and it is ready to be deployed. The WAR file is named `emc-metalnx-web.war` and it can be found under `metalnx-web/emc-metalnx-web/target`.

### Deploying the Metalnx WAR file

With the `emc-metalnx-web.war` file in hands, it is easy to deploy it in on an Apache Tomcat server. There are few different ways to deploy WAR files in a Tomcat instance. In this tutorial, we will deploy Metalnx through the Tomcat Manager App (available at `http://{your-tomcat-host}:{your-tomcat-port}/manager/html`).

![Tomcat Manager app]

Once on this application manager page, you can select the `war` file to deploy (under the *Deploy*) section, find the `emc-metalnx-web.war` from above on your file system, select it, and click deploy. 

**NOTE:** This operation may take a few minutes to complete. 

## Building the RPM and DEB packages

Return to the root of the Metalnx repository you build Metalnx from (`<your repository directory>/metalnx-web`), you will see a folder called `packaging`. This folder contains all scripts and configuration files necessary to create both RPM and DEB packages.

Metalnx provides a script for RPM package creation and another script for DEB package creation. Both scripts take two input parameters. The first parameter specifies which version of Metalnx the RPM package refers, currently it should be 1.0. The second parameter specifies the number of the current build. If you are building Metalnx for the first time, it should be 1.  You can increment this second (pass) number with each build you make for tracking purposes.

### RPM

Under `metalnx-web/packaging/rpm` is the file `emc-metalnx-webapp.spec` file which tells rpm-build how to build the Metalnx RPM package and a file list for all the binaries that get included in the package. 

To create the package use the script `create_rpm_package.sh` located at `metalnx-web/packaging/scripts`. This script was uses a environment variable, WORKSPACE, to track where the top level directory for the build is.  This variable must be set prior to using this script.  An example on how to create a RPM package using this script is:

    $ WORKSPACE=$(pwd) ./packaging/scripts/create_rpm_package.sh 1.0 1


When the script is finished you will have an `.rpm` packaged named `emc-metalnx-webapp-1.0-1.noarch.rpm`  ready for deployment.  This will will be located at `~/rpmbuild/noarch`. 

### DEB

Under `metalnx-web/packaging/deb/DEBIAN` is a `control` file. This file is used to create the Metalnx DEB package. The DEB creation script is `create_deb_package.sh` located at `metalnx-web/packaging/scripts`. This script was uses a environment variable, WORKSPACE, to track where the top level directory for the build is.  This variable must be set prior to using this script.  An example on how to create a DEB package using this script is:

    $ WORKSPACE=$(pwd) ./packaging/scripts/create_rpm_package.sh 1.0 1

After the script is done, you will have a file named `emc-metalnx-webapp-1.0-1.deb`  ready for deployment.  This will will be located at `/tmp/emc-tmp/emc-metalnx-webapp-1.0-1.deb`. 

[Tomcat Manager app]: <https://assets.digitalocean.com/articles/tomcat8_1604/manager.png>