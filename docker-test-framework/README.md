# Docker Testing Framework for Metalnx 

This testing framework provides a disposable docker image of a running metalnx, including the metalnx database. This arrangement is meant to use the iRODS configuration found in the [Jargon Docker Testing Framework] (https://github.com/DICE-UNC/jargon/blob/master/DOCKERTEST.md), utilizing the same testing properties and setup facilities across all the services.

# Steps to set up test environment

### build metalnx via the mvn package command (-DskipTests) 

cd into /src where you will find the root pom.xml file, issue the command

```
mvn package -DskipTests
```

This will place the .war file in the proper location for Docker to pick up

### run iRODS

Follow steps in [Jargon Docker Testing Framework] (https://github.com/DICE-UNC/jargon/blob/master/DOCKERTEST.md) to start up an iRODS instance

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

### Start the database

Note: there is still work to be done to properly sequence the start of the database, the database migration, and the start of metalnx. For the time being, the solution is to start each part of the docker group manually following these steps. Here we start the database as a clean image

```
docker-compose up -d -V metalnx-database

```

This will start the database as a daemon

### Run the migration utility. This can be run in the foreground as it runs and terminates

```
docker-compose up database-init
```

You should see messages reflecting a successful migration

```

database-init       | [INFO] Database: jdbc:postgresql://metalnx-database:5432/irods-ext (PostgreSQL 12.2)
database-init       | [INFO] Creating schema "irods-ext" ...
database-init       | [INFO] Creating Metadata table: "irods-ext"."schema_version"
database-init       | [INFO] Current version of schema "irods-ext": null
database-init       | [INFO] Migrating schema "irods-ext" to version 1 - Base version
database-init       | [INFO] Migrating schema "irods-ext" to version 1.0.1 - BasicAdvance version
database-init       | [INFO] Migrating schema "irods-ext" to version 1.0.2 - MetadataUnitPref version
database-init       | [INFO] Migrating schema "irods-ext" to version 1.0.3 - RemoveGroup
database-init       | [INFO] Migrating schema "irods-ext" to version 1.0.4 - RemoveUser UserGroupConstraints
database-init       | [INFO] Successfully applied 5 migrations to schema "irods-ext" (execution time 00:00.377s).
database-init       | [INFO] ------------------------------------------------------------------------
database-init       | [INFO] BUILD SUCCESS
database-init       | [INFO] ------------------------------------------------------------------------
database-init       | [INFO] Total time:  3.576 s
database-init       | [INFO] Finished at: 2020-04-15T15:17:24Z
database-init       | [INFO] ------------------------------------------------------------------------
database-init exited with code 0

```

### Start metalnx

```
docker-compose up -d metalnx

```

You should now be able to access metalnx at http://localhost:8080/metalnx