![Metalnx Logo](docs/IMAGES/mlx_logo_blue.png)

## Version: 2.2.0-SNAPSHOT
## Git Tag:
## Date:

Metalnx is a web application designed to work alongside [iRODS](http://www.irods.org). It is a graphical user interface and serves as a client that authenticates to an existing iRODS Zone.

![Metalnx 2.0.0 Screenshot](docs/IMAGES/metalnx2.0.0.png)


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

### Prepare the application

[Configuration](CONFIGURATION.md) of the default application can change many things about how Metalnx looks and behaves.
 - Configuration of Zone information, and features to display
 - Theming with custom CSS/Logo

Create a copy of the default `etc/irods-ext` directory and update `metalnx.properties` and `metalnxConfig.xml`, and then run a container with the new configuration, probably with `--add-host` information due to Docker:
```
docker run -d \
  -p 8080:8080 \
  -v `pwd`/mylocal-irods-ext:/etc/irods-ext \
  --add-host hostcomputer:172.17.0.1 \
  --name metalnx \
  irods/metalnx:latest
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

## Building Metalnx

From source, the package will 'just build':
```
make
```

This will result in a new Docker image named `myimages/metalnx:latest` on your machine.

## Building Metalnx Docker image by using multistage approach

```
docker build --rm --no-cache -t myimages/metalnx:latest -f Dockerfile.multistage .
```

This will result in a new Docker image named `myimages/metalnx:latest` on your machine.

## Deploying Built Metalnx

If you're deploying your own image (built just above):

```
docker run -d \
  -p 8080:8080 \
  -v `pwd`/mylocal-irods-ext:/etc/irods-ext \
  --add-host hostcomputer:172.17.0.1 \
  --name metalnx \
  myimages/metalnx:latest
```

### More information

More documentation can be found in the [Docs](docs) directory.

### Copyright and License

Copyright © 2018-2020 University of North Carolina at Chapel Hill; 2015-2017, Dell EMC.

This software is provided under the [BSD-3 License](LICENSE.md).
