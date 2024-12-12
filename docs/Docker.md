This document walks you through the creation of a Metalnx Docker Container. There are two ways of using the Metalnx docker container:

* You can either run the already built container hosted on [DockerHub][dockerhub-metalnx] or
* you can build Metalnx container on your environment and run it locally

Before going any further, make sure your system meets the [dependencies](https://github.com/Metalnx/metalnx-web/wiki/Dependencies#docker-container) listed for Docker.

*The Metalnx Docker Container is just another way to deploy Metalnx in your infrastructure. If you do not want to use Docker or you are not familiar with it, take a look at the [Getting Started](https://github.com/Metalnx/metalnx-web/wiki/Getting-Started) post to check how to install Metalnx in another way.*

## Docker Service
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
The Metalnx container is hosted on DockerHub under the **metalnx** account. You can launch your container using the following command:

    docker run -d \
    -p 8080:8080 \
    -e IRODS_HOST="<IRODS_HOST_NAME>" \
    -e IRODS_PORT=<IRODS_PORT> \
    -e IRODS_ZONE="<IRODS_ZONE_NAME>" \
    -e IRODS_USER="<IRODS_ADMIN_USERNAME>" \
    -e IRODS_PASS="<IRODS_ADMIN_PASSWORD>" \
    metalnx/metalnx-web:1.1.0-latest
    
### Configuring a self-signed SSL cert for iRODS transport

If you have a self-signed certificate for iRODS SSL, it needs to be known by the JVM running Metalnx. This can be accomplished by adding the ssl public key as a file called server.crt mounted as a volume that appears under /tmp/cert in docker, 

```
-v /home/hostvolumehere:/tmp/cert
```
    
*The command line above assumes your Docker container in running in an environment which supports DNS.  If you are running in an environment without DNS see the **Troubleshooting** section below for instructions on how to add custom hostnames to this command.*

You can check all Metalnx containers at [DockerHub](https://hub.docker.com/r/metalnx/metalnx-web/tags/).

## Building and Running the Container Locally
In order to build the Metalnx container, you need to **cd** to your `packaging/docker` directory:

    $ cd /path/to/metalnx/repo 

Finally, execute the build

    $ docker build -t metalnx .  # Notice the '.' at the end of the command

The `.` character at the end of the commands tells Docker where the **Dockerfile** is. Then, you need to execute **run** on the docker container specifying the following paramenters

    $ docker run -d \
    -p 8080:8080 \
    -e IRODS_HOST="<IRODS_HOST_NAME>" \
    -e IRODS_PORT=<IRODS_PORT> \
    -e IRODS_ZONE="<IRODS_ZONE_NAME>" \
    -e IRODS_USER="<IRODS_ADMIN_USERNAME>" \
    -e IRODS_PASS="<IRODS_ADMIN_PASSWORD>" \
    metalnx
    
*The command line above assumes your Docker container in running in an environment which supports DNS.  If you are running in an environment without DNS see the **Troubleshooting** section below for instructions on how to add custom hostnames to this command.*

Keep in mind that the **-p** parameter maps a port from the host machine to the container. You can replace the host port with any port you want, but the container port must be *8080*, unless you customize the image (refer to the next section for instruction on how to do this).

## Modifying Metalnx container
The **Dockerfile** file is a script that describes the steps for how to build the container. You can adapt this file to your needs (change base operating system, modifying database manager, etc). If you need to execute any command on  container startup, please modify the `start_metalnx.sh` script under your `<Metalnx_source_root>/packaging/docker` directory.

## Troubleshooting

### Running custom hostnames without a DNS server on your network
If you don't have a DNS server installed and configured in your infrastructure and would like to use hostname to reference your grid's machines, then you will have to use the `--add-host` option alongside the `docker run` command. It is necessary because the docker image creation does not allow modifications to the `/etc/hosts` file of the container OS, so any host:ip mapping should be done on command line.

Example:

    $ docker run -d \
    -p 8080:8080 \
    -e IRODS_HOST="<IRODS_HOST_NAME>" \
    -e IRODS_PORT=<IRODS_PORT> \
    -e IRODS_ZONE="<IRODS_ZONE_NAME>" \
    -e IRODS_USER="<IRODS_ADMIN_USERNAME>" \
    -e IRODS_PASS="<IRODS_ADMIN_PASSWORD>" \
    --add-host=icat:192.168.1.123 \
    metalnx/metalnx-web:1.0-latest

If you are running multiple iRODS grid servers, you must add one `--add-host` mapping per mechine, as follows:

    $ docker run -d \
    -p 8080:8080 \
    -e IRODS_HOST="<IRODS_HOST_NAME>" \
    -e IRODS_PORT=<IRODS_PORT> \
    -e IRODS_ZONE="<IRODS_ZONE_NAME>" \
    -e IRODS_USER="<IRODS_ADMIN_USERNAME>" \
    -e IRODS_PASS="<IRODS_ADMIN_PASSWORD>" \
    --add-host=icat:192.168.1.123 \
    --add-host=resource1:192.168.1.124 \
    --add-host=resource2:192.168.1.125 \
    metalnx/metalnx-web:1.0-latest

## References
* [Docker][docker]
* [DockerHub][dockerhub]
* [Dockerfile specification][dockerfile]

[docker]: https://www.docker.com/
[dockerhub]: https://hub.docker.com/
[dockerfile]: https://docs.docker.com/engine/reference/builder/
[dockerhub-metalnx]: https://hub.docker.com/r/metalnx/metalnx-web/
