![Metalnx Logo](docs/IMAGES/mlx_logo_blue.png)

Metalnx is a web application designed to work alongside [iRODS](http://www.irods.org). It is a graphical user interface and serves as a client that authenticates to an existing iRODS Zone.


## Deploying Packaged Metalnx

The preferred method of deployment is via Docker.  You can deploy Metalnx directly from Docker Hub.

First, [create and configure a database for Metalnx's use (this is for caching and other local information)](src/metalnx-tools/README.md).

```
cd src/metalnx-tools
mvn flyway:migrate
```

[Configuration](CONFIGURATION.md) of the default server can change many things about how Metalnx looks and behaves.
 - SSL to iRODS
 - SSL to User (in the browser)
 - Configuration of Zone information, and features to display
 - Theming with custom CSS/Logo

Run an existing container with zone-specific configuration:
```
docker run -d -v `pwd`/mylocal-irods-ext:/etc/irods-ext irods-contrib/metalnx:latest .
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
docker run -d -v `pwd`/mylocal-irods-ext:/etc/irods-ext myimages/metalnx:latest
```

### More information

More documentation can be found in the [Docs](docs) directory.

### Copyright and License

Copyright © 2018-2019 University of North Carolina at Chapel Hill; 2015-2017, Dell EMC.

This software is provided under the [BSD-3 License](LICENSE.md).

### Changes

Refer to the [CHANGELOG.md](CHANGELOG file) for the latest changes
