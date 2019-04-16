![Metalnx Logo](docs/IMAGES/mlx_logo_blue.png)

## Version: 2.1.0-SNAPSHOT
## Git Tag:
## Date:

## Deploying Packaged Metalnx

The preferred method of deployment is via Docker.  You can deploy Metalnx directly from Docker Hub.

### Prepare the database

First, create and configure a database for Metalnx's use (this is for caching and other local information).

```
$ (sudo) su - postgres
postgres$ psql
psql> CREATE USER metalnx WITH PASSWORD 'changeme';
psql> CREATE DATABASE "IRODS-EXT";
psql> GRANT ALL PRIVILEGES ON DATABASE "IRODS-EXT" TO metalnx;
```

Then, create and update the database connection information in `flyway.conf`:
```
cp flyway.conf.template flyway.conf
```

Then, run the flyway database migration tool:

```
docker run --rm \
  -v `pwd`/src/metalnx-tools/src/main/resources/migrations:/flyway/sql \
  -v `pwd`:/flyway/conf \
  boxfuse/flyway migrate
```

You will probably need to add `--add-host` to the docker command so that the flyway container can see the database server.
If the database is running directly on the host machine, then the host IP may be the Docker bridge network IP:
```
  --add-host hostcomputer:172.17.0.1 \
```

If you're having trouble with the flyway container, you can run the `.sql` files in [src/metalnx-tools/src/main/resources/migrations](src/metalnx-tools/src/main/resources/migrations) directly.  Make sure to run them in order (first `V1__Base_version.sql`, then each numbered version).

Additional maven-based migration information can be found in [src/metalnx-tools](src/metalnx-tools/README.md).

### Prepare the application

[Configuration](CONFIGURATION.md) of the default application can change many things about how Metalnx looks and behaves.
 - Configuration of Zone information, and features to display
 - Theming with custom CSS/Logo

Create a copy of the default `etc/irods-ext` directory, update `metalnx.properties` and `metalnxConfig.xml`, and then run a container with the new configuration, probably with `--add-host` information due to Docker:
```
docker run -d \
  -p 8080:8080 \
  -v `pwd`/mylocal-irods-ext:/etc/irods-ext \
  --add-host hostcomputer:172.17.0.1 \
  irods-contrib/metalnx:latest .
```

To map a local directory with SSL certificates (self-signed or from a CA), the container will look in `/tmp/cert`:
```
 -v `pwd`/mylocal-certs:/tmp/cert \
```

The login screen should appear when requested in a web browser:

```
http://x.x.x.x:8080/metalnx
```




Or...

## Build Prerequisites

Metalnx is a Java 8 application and runs as a `.war` file within an Apache Tomcat instance.

- Java 8
- Maven 3.1+

## Building Metalnx

From source, the package will 'just build':
```
cd src
mvn package -Dmaven.test.skip=true
```

The new `.war` file can be found at `packaging/docker/metalnx.war`, awaiting deployment.

## Packaging Built Metalnx

The preferred method of deployment is via Docker.  Navigate to the `docker` directory to build the Docker image.

```
cd ../packaging/docker
docker build -t myimages/metalnx:latest .
```

## Deploying Built Metalnx

If you're deploying your own image (built just above):

```
docker run -d \
  -p 8080:8080 \
  -v `pwd`/mylocal-irods-ext:/etc/irods-ext \
  --add-host hostcomputer:172.17.0.1 \
  myimages/metalnx:latest
```

### More information

More documentation can be found in the [Docs](docs) directory.

### Copyright and License

Copyright © 2018-2019 University of North Carolina at Chapel Hill; 2015-2017, Dell EMC.

This software is provided under the [BSD-3 License](LICENSE.md).

### Changes

Refer to the [CHANGELOG file](CHANGELOG.md) for the latest changes
