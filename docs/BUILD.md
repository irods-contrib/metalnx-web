<font color="#3892CF"> EMC METALNX BUILD GUIDE
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

This tutorial guides you through the Metalnx compilation pipeline. It explains how to build Metalnx from scratch.

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

Once you have successfully installed Maven (use the `mvn --version` command to  check that), you can build Metalnx. As Metalnx is a Java, Web-based application, it will be available as a [war](*war) file at the end of the compilation process. 

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

Maven is based around the central concept of a [build lifecycle](#build_lifecycle). The *clean* phase cleans the project's working directory, *generate-sources* generates source code for inclusion in compilation, *package* takes the compiled code and package it in its distribution format, in this case the format is `.war`.

You do not have to worry about downloading any dependencies. Maven will take care of that for you. Any other package that Metalnx requires will be download from the [Maven repository]. After downloading all dependencies, Maven will build all Metalnx subprojects in the following order: *emc-metalnx-core*, *emc-metalnx-services*, *emc-metalnx-shared*, *emc-metalnx-ui-admin*, *emc-metalnx-ui-user* and *emc-metalnx-web*.

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

## Building RMD RPM and DEB packages

Before building RMD packages, make sure you have the metalnx-rmd source code downloaded in your environment and **cd** to its source tree root. You will see a folder called `packaging`. This folder contains all scripts and configuration files necessary to create both RPM and DEB packages.

Metalnx RMD provides a script for RPM package creation and another script for DEB package creation. Both scripts take two input parameters. The first parameter specifies which version of Metalnx the RPM package refers, currently it should be 1.0. The second parameter specifies the number of the current build. If you are building Metalnx for the first time, it should be 1.  You can increment this second (pass) number with each build you make for tracking purposes.

### RPM

Under `metalnx-rmd/packaging/rpm` is the file `emc-metalnx-rmd.spec` file which tells rpm-build how to build the Metalnx RMD RPM package and a file list for all the binaries that get included in the package. 

To create the package use the script `create_rpm_package.sh` located at `metalnx-rmd/packaging/scripts`. This script was uses a environment variable, WORKSPACE, to track where the top level directory for the build is.  This variable must be set prior to using this script.  An example on how to create a RPM package using this script is:

    $ WORKSPACE=$(pwd) ./packaging/scripts/create_rpm_package.sh 1.0 1

When the script is finished you will have an `.rpm` packaged named `emc-metalnx-rmd-1.0-1.noarch.rpm`  ready for deployment.  This will will be located at `~/rpmbuild/noarch`. 

### DEB

Under `metalnx-rmd/packaging/deb/DEBIAN` is a `control` file. This file is used to create the Metalnx RMD DEB package. The DEB creation script is `create_deb_package.sh` located at `metalnx-rmd/packaging/scripts`. This script was uses a environment variable, WORKSPACE, to track where the top level directory for the build is.  This variable must be set prior to using this script.  An example on how to create a DEB package using this script is:

    $ WORKSPACE=$(pwd) ./packaging/scripts/create_rpm_package.sh 1.0 1

After the script is done, you will have a file named `emc-metalnx-rmd-1.0-1.deb`  ready for deployment.  This will will be located at `/tmp/emc-tmp/emc-metalnx-rmd-1.0-1.deb`. 

### Troubleshooting

If you are facing problems in executing either the `create_rpm_package.sh` or the `create_deb_package.sh` scripts, make sure your current Linux user has execution permissions on these files.

The execution permission on these files can be set as follows:

    $ chmod u+x ./packaging/rpm/create_rpm_package.sh

or

    $ chmod u+x ./packaging/deb/create_deb_package.sh

<br>
## Building Metalnx MSI RPM and DEB packages

The Metalnx MSI package is an optional package that provides a set of iRODS micro services allowing the data grid to automatically extract metadata from certain kinds of files. It relies on the irods-dev library provided by  [iRODS][irods-dev-download].

This package also provides automatic metadata extraction for genetic research files (BAM, SAM, CRAM, VCF and Illumina project manifest). In order to allow iRODS and Metalnx to understand these files formats, you must install additional libraries:
    * [HTSlib][htslib-download]
	* [samtools][samtools-download]

### Installing the irods-dev Package

In order to install the `irods-dev` package, you must satisfy its dependencies. The only package you'll need to install `irods-dev` is `openssl-dev` (on DEB systems) or `openssl-devel` (on RPM systems). Once this dependency is satisfied, you can install `irods-dev` with the command:

    $ rpm -ivh irods-dev-4.1.8-centos7-x86_64.rpm         # On RPM-based system
	
or

    $ dpkg -i irods-dev-4.1.8-ubuntu14-x86_64.deb         # On DEB-based systems

### Installing HTSlib

On DEB-based or RPM-based systems, you will need the basic development tools in order to download, compile and install HTSlib (C++ compiler,   make):

    $ wget https://github.com/samtools/htslib/releases/download/1.3.1/htslib-1.3.1.tar.bz2    # Getting sources from GitHub
    $ tar -xvf htslib-1.3.1.tar.bz2															  # Extracting tarball
    $ cd htslib-1.3.1																		  # Entering sources directory
    $ ./configure																			  # Configuring compilation pipeline
    $ make																					  # Compiling sources
    $ sudo make install																		  # Installing compiled binary and headers

### Installing samtools

On DEB-based or RPM-based systems, you will need the basic development tools in order to download, compile and install samtools (C++ compiler, make):

    $ wget https://github.com/samtools/samtools/releases/download/1.3.1/samtools-1.3.1.tar.bz2    # Getting sources from GitHub
    $ tar -xvf samtools-1.3.1.tar.bz2															  # Extracting tarball
    $ cd samtools-1.3.1																			  # Entering sources directory
    $ ./configure																				  # Configuring compilation pipeline
    $ make																						  # Compiling source code
    $ sudo make install																			  # Installing compiled binary
    $ sudo cp *.h /usr/local/include/															  # Installing headers

### Building Metalnx MSI RPM and DEB package

Once the previous requirements are satisfied, you can build the MSI package. The format of the package will depend on which platform you are executing these commands on. That happens because the Metalnx MSI packing system is based on EPM (Enterprise Package Manager), which is part of the iRODS compilation pipeline. The EPM automatically manages the package creation depending on which platform it is running on.

The first step is make sure you have the `metalnx-msi` sources downloaded and a terminal session opened on the source tree root:

    $ cd metalnx-msi
	
Then you have to make sure your Linux user has execution permission on the `build.sh` script:

    $ chmod u+x packaging/build.sh
	
The last step is to execute the `build.sh` script. 

**NOTE:** This command must be executed from the source tree root, otherwise it will NOT work.":

    $ ./packaging/build.sh

Once completed, your resulting package can be found at `<metalnx_msi_root>/linux-X.XX-<arch>/RPMS/<arch>metalnx-msi-plugins-1.0.0.<arch>.<rpm/deb>`.

## References

   [war]: <a name=war></a> <https://docs.oracle.com/cd/E19316-01/820-3748/aduvz/index.html>

   [build lifecycle]: <a name=build_lifecycle></a> <https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html>

   [Maven repository]: <http://mvnrepository.com/>
   [Tomcat]: <http://tomcat.apache.org/>
   [Deploy WAR Remotely]: <https://tomcat.apache.org/tomcat-8.0-doc/manager-howto.html#Deploy_A_New_Application_Archive_(WAR)_Remotely>
   [Deploy a new application from a local path]: <https://tomcat.apache.org/tomcat-8.0-doc/manager-howto.html#Deploy_A_New_Application_from_a_Local_Path>
   [Tomcat Manager app]: <https://assets.digitalocean.com/articles/tomcat8_1604/manager.png>
   [irods-dev-download]: http://irods.org/download/
   [htslib-download]: https://github.com/samtools/htslib/releases/download/1.3.1/htslib-1.3.1.tar.bz2
   [samtools-download]: https://github.com/samtools/samtools/releases/download/1.3.1/samtools-1.3.1.tar.bz2
