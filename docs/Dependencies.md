This post lists all dependencies for the Metalnx Web application.

# Web Application

### Java
Java 1.8 or higher

### iRODS
iRODS 4.1.8+ or 4.2.0+

### Python
Python 2.7

### Web Server
Tomcat 7 or higher

    $ sudo yum -y install tomcat
    $ sudo apt-get -y install tomcat7

# Manual Build
This section describes the dependencies *only necessary* to *build* the Metalnx Web project. **If you are only installing Metalnx via `rpm` or `deb` packages, you can skip this section**.

If you want to build the Metalnx Web project manually, first ensure that you installed all dependencies listed in the section above before you proceed.

Metalnx depends on Maven 3.1 or higher. Before starting, you should have already installed Maven in your environment. Maven is a Apache project available at [https://maven.apache.org/download.cgi](https://maven.apacke.org/download.cgi). This website provides download links and you can also find a Maven overview and how to integrate the tool in your system.  

You can typically install Maven via a package manager like `yum` or `apt-get`.  For example:
```
# yum install maven
```

# Docker Container

* [Docker 1.9.1](https://www.docker.com/)
* Metalnx source code (specifically the `packaging/docker` folder), (*This is needed if you are building the container locally*)
* Metalnx `war` file already built (refer to **Metalnx Build Guide**), (*This is needed if you are building the container locally*)
* Access to the Internet. The container building process needs to get files from the CentOS central repository in order to get the base system running.
