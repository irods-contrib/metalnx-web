<font color="#3892CF"> EMC METALNX
===================================

<font color="#3892CF"> Build a Docker Container
=========================================

<font color="#A6A6A6"> <font size=+2> Revision 1.0 

6/2016 </font>

----------------------------------

<font color="#000000">

Copyright © 2015-16 EMC Corporation.

This software is provided under the Software license provided in the <a href="LICENSE"> LICENSE </a> file.

The information in this file is provided “as is.” EMC Corporation makes no representations or warranties of any kind with respect to the information in this publication, and specifically disclaims implied warranties of merchantability or fitness for a particular purpose. 

<font color="#000000"> 

<br>
## Introduction </font> <a name="introduction"></a>


This document walks you through the creation of a Metalnx Docker Container. There are two ways of using the Metalnx docker container:

* You can either run the already built container hosted on [DockerHub][dockerhub-metalnx] or
* you can build MetaLnx container on your environment and run it locally

<br>
## Requirements <a name="requirements"></a>

* [Docker 1.9.1][docker]
* Metalnx source code (specifically the `packaging/docker` folder), (*This is needed if you are building the container locally*)
* Metalnx WAR file already built (refer to **Metalnx Build Guide**), (*This is needed if you are building the container locally*)
* Access to the Internet. The container building process needs to get files from the CentOS central repository in order to get the base system running.

## Running the docker service
For all operations listed below, you need to make sure the Docker service is installed and running on your machine.

To check the status of the Docker Service, execute:

    $ systemctl status docker

If the Docker service is not running, start it:

    $ systemctl start docker

## iRODS grid information
In order to allow Metalnx to access and manipulate your data grid, you have to gather some information about your environment before running the container. Please make sure you have these items ready:

* iRODS iCAT hostname
* iRODS Zone name
* iRODS port
* iRODS admin username (MUST be a **rodsadmin** user)
* iRODS admin password

These items must be passed to the container in order to allow Metalnx to contact your data grid and execute commands on it through the *Jargon API*. 

## Running Metalnx container from DockerHub
The Metalnx container is hosted on DockerHub under the **henriquenogueira** account. You can launch your container using the following command:


    docker run -d \
    -p 8080:8080 \
    -e IRODS_HOST="<IRODS_HOST_NAME>" \
    -e IRODS_PORT=<IRODS_PORT> \
    -e IRODS_ZONE="<IRODS_ZONE_NAME>" \
    -e IRODS_USER="<IRODS_ADMIN_USERNAME>" \
    -e IRODS_PASS="<IRODS_ADMIN_PASSWORD>" \
    henriquenogueira/metalnx

## Building and Running the Container Locally
In order to build the Metalnx container, you need to **cd** to your `packaging/docker` directory:

    $ cd <Metalnx_source_code_root>/packaging/docker

Finally, execute the build

    $ docker build -t metalnx .  # Notice the '.' at the end of the command

The '.' character at the end of the commands tells Docker where the **Dockerfile** is. Then, you need to execute **run** on the docker container specifying the following paramenters

    docker run -d \
    -p 8080:8080 \
    -e IRODS_HOST="<IRODS_HOST_NAME>" \
    -e IRODS_PORT=<IRODS_PORT> \
    -e IRODS_ZONE="<IRODS_ZONE_NAME>" \
    -e IRODS_USER="<IRODS_ADMIN_USERNAME>" \
    -e IRODS_PASS="<IRODS_ADMIN_PASSWORD>" \
    metalnx

Keep in mind that the **-p** parameter maps a port from the host machine to the container. You can replace the host port with any port you want, but the container port must be *8080*, unless you customize the image (refer to the next section for instruction on how to do this).

## Modifying MetaLnx container
The **Dockerfile** file is a script that describes the steps for how to build the container. You can adapt this file to your needs (change base operating system, modifying database manager, etc). If you need to execute any command on  container startup, please modify the `start_metalnx.sh` script under your `<Metalnx_source_root>/packaging/docker` directory.

## References
* [Docker][docker]
* [DockerHub][dockerhub]
* [Dockerfile specification][dockerfile]

[docker]: https://www.docker.com/
[dockerhub]: https://hub.docker.com/
[dockerfile]: https://docs.docker.com/engine/reference/builder/
[dockerhub-metalnx]: https://hub.docker.com/r/henriquenogueira/metalnx/