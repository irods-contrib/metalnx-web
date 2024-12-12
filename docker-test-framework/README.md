# Docker Testing Framework for Metalnx 

This testing framework provides a disposable docker image of a running metalnx. This arrangement is meant to use the iRODS configuration found in the [Jargon Docker Testing Framework] (https://github.com/DICE-UNC/jargon/blob/master/DOCKERTEST.md), utilizing the same testing properties and setup facilities across all the services.

# Steps to set up test environment

### build metalnx via the mvn package command (-DskipTests) 

cd into /src where you will find the root pom.xml file, issue the command

```
mvn package -DskipTests
```

This will place the .war file in the proper location for Docker to pick up

### run iRODS

Follow steps in [Jargon Docker Testing Framework] (https://github.com/DICE-UNC/jargon/blob/master/docker-test-framework/DOCKERTEST.md) to start up an iRODS instance

You should be able to issue a docker ps command and see these services running

```
LMBP-02010755:docker-test-framework conwaymc$ docker ps
CONTAINER ID        IMAGE                                  COMMAND                  CREATED             STATUS              PORTS                              NAMES
77f0c2c2fe20        metalnx                                "/runit.sh"              About an hour ago   Up 15 minutes       0.0.0.0:8080->8080/tcp             metalnx
2d94c58fd077        4-2_irods-catalog-consumer-resource1   "./start_consumer.sh"    20 hours ago        Up 20 hours         1247-1248/tcp                      irods-catalog-consumer-resource1
072086ac44e1        4-2_irods-catalog-provider             "./start_provider.sh"    20 hours ago        Up 20 hours         0.0.0.0:1247->1247/tcp, 1248/tcp   irods-catalog-provider
c7e102bd061d        4-2_maven                              "/usr/local/bin/mvn-…"   20 hours ago        Up 20 hours                                            maven
ALMBP-02010755:docker-test-framework conwaymc$ 


```

### Set an environment variable to point to the required etc config files 

in this subdirectory is an etc/irods-ext directory, set the variable to point to this local location and mount in the docker image. Open a terminal to the docker-test-framework subdirectory and issue:

```

export METALNX_ETC_DIR=`pwd`/etc/irods-ext


```

If you have variations you may copy or edit properties in place and reposition the environment variable

### run docker-compose build from the docker-test-framework directory

from the docker-test-framework directory, run the docker-compose build command so that the proper docker image is created, This snippet
shows positioning of a terminal session to the proper location

```

ALMBP-02010755:docker-test-framework conwaymc$ pwd
/Users/conwaymc/Documents/workspace-niehs-rel/metalnx-web/docker-test-framework
ALMBP-02010755:docker-test-framework conwaymc$ ls
Dockerfile.databaseinit README.md               database.env            docker-compose.yml      etc                     initdb.sh               settings.xml
ALMBP-02010755:docker-test-framework conwaymc$ 


```

From this location, build the necessary docker images

```
docker-compose build

```

### Start metalnx

```
docker-compose up -d metalnx

```

You should now be able to access metalnx at http://localhost:8080/metalnx
