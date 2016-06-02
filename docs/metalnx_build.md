# How to build Metalnx

This tutorial guides you through the Metalnx compilation pipeline. It explains how you should procedure if you want to build Metalnx from scratch.

## Version
1.0
 
## Depedencies
 
Metalnx depends on Java 1.8 or higher and Maven 3.1 or higher. Before starting, you should have already installed Maven in your environment. Maven is a Apache project available on https://maven.apache.org/download.cgi or through your package manager. This website provides download links and you can also find a Maven overview and how to integrate the tool in your system. 

> We strongly recommend both Java and Maven intallation via package manager, if available.
 
 ### Java JDK 1.8
 
 Make sure you have the Java-devel 1.8 package installed on your environment before proceeding. This package contains the java compiler required by Metalnx in order to build the project.
 
 > Again, we strongly recommend the java-devel installation via package manager, if available.
 
 ## Metalnx Repository
 
Also, make sure you have the Metalnx source code checked out in your environment. The Metalnx directory should be as follows:

* src
* packaging
* README
* License

The *src* folder is where the Metalnx application code is located. *Src* contains all files that will be compiled and, after the compilation, it will also contains the Metalnx web app. 

> The README and License files contains general and legal information about Metalnx, respectivelly. 
> Packaging is the folder where RPM and DEB configuration scripts live. They are used to create the Metalnx RPM and DEB packages. 

## Metalnx Compilation Pipeline

Assuming you have successfully installed Maven (you can run a *mvn --version* to double check that), it is time to actually build Metalnx. As a Java, Web-based application, Metalnx will be available as a [war] file at the end of the compilation process. Browse to the Metalnx *src* folder (*metalnx-web/src*). Listing the *src* folder content you should see:

* emc-metalnx-core
* emc-metalnx-services
* emc-metalnx-shared
* emc-metalnx-ui-admin
* emc-metalnx-ui-user
* emc-metalnx-web
* pom.xml

> Note that Maven will work only if you are under the src directory (with the files listed above). Maven requires the **pom.xml** file to start the building process. If you are able to see this file, you are on the right place to build Metalnx.

Now that you are under the *src* folder, run the following command:

```sh
$ mvn generate-sources package -Dmaven.test.skip=true
```

### Understanding the *mvn* command:

Maven is based around the central concept of a [build lifecycle]. The *clean* phase cleans the project's working directory, *generate-sources* generates source code for inclusion in compilation, *package* takes the compiled code and package it in its distributable format, in this case the format is WAR.

Any other package that Metalnx requires will be downloaded from the [Maven repository]. After downloading all dependencies, Maven will build all Metalnx subprojects in the following order: *emc-metalnx-core*, *emc-metalnx-services*, *emc-metalnx-shared*, *emc-metalnx-ui-admin*, *emc-metalnx-ui-user* and *emc-metalnx-web*.

### Finding the WAR file

When the build is completed, you will get a "BUILD SUCCESS" message. Now, the Metalnx WAR file exists and it is ready to be deployed. The WAR file is named *emc-metalnx-web.war* and it can be found under *metalnx-web/emc-metalnx-web/target*.

### Deploying the Metalnx WAR file

With the *emc-metalnx-web.war* file in hand, it is easy to deploy it in a [Tomcat] server. There are few different ways to deploy WAR files in a Tomcat instance that you can check on [Deploy a new application from a local path]. In this tutorial, we are going to deploy Metalnx via Tomcat Manager App (available on *http://{your-tomcat-host}:{your-tomcat-port}/manager/html*).

![Tomcat Manager app]

Once on this page, you can select the WAR file to deploy (under the *Deploy*) section, find the *emc-metalnx-web.war* file and click deploy. This operation may take a few minutes to complete. 

## Building the RPM and DEB packages

Going back to the root of the Metalnx project directory, you will see a folder called *packaging*. This folder contains all scripts and configuration files necessary to create both RPM and DEB packages.

Metalnx provides a script for RPM package creation and another script for DEB package creation. Both scripts take two input parameters. The first parameter specifies which version of Metalnx the RPM package refers, currently it should be 1.0. The second parameter specifies the number of the current build. If you are building Metalnx for the first time, it should be 1.

### RPM

Under *metalnx-web/packaging/rpm* you can check the *emc-metalnx-webapp.spec* file. This file contains a description instructions on how to build the Metalnx RPM package and a file list for all the binaries that get installed. The script used to create the rpm package is called *create_rpm_package.sh* and it can be found at *metalnx-web/packaging/scripts*. An example on how to create a RPM package using this script is:

```sh
$ ./packaging/scripts/create_rpm_package.sh 1.0 1
```

After the script is done, you will have a *emc-metalnx-webapp-1.0-1.noarch.rpm* file ready to be installed.

### DEB

Under *metalnx-web/packaging/deb/DEBIAN* you can check the *control* file. This file is used to create the Metalnx DEB package. The DEB creation script is called *create_deb_package.sh* and it can be found at *metalnx-web/packaging/scripts*. An example on how to create a DEB package using this script is:

```sh
$ ./packaging/scripts/create_rpm_package.sh 1.0 1
```

After the script is done, you will have a *emc-metalnx-webapp-1.0-1.deb* file ready to be installed.

   [war]: <https://docs.oracle.com/cd/E19316-01/820-3748/aduvz/index.html>
   [build lifecycle]: <https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html>
   [Maven repository]: <http://mvnrepository.com/>
   [Tomcat]: <http://tomcat.apache.org/>
   [Deploy WAR Remotely]: <https://tomcat.apache.org/tomcat-8.0-doc/manager-howto.html#Deploy_A_New_Application_Archive_(WAR)_Remotely>
   [Deploy a new application from a local path]: <https://tomcat.apache.org/tomcat-8.0-doc/manager-howto.html#Deploy_A_New_Application_from_a_Local_Path>
   [Tomcat Manager app]: <https://assets.digitalocean.com/articles/tomcat8_1604/manager.png>
