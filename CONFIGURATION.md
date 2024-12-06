# Metalnx Deployment Configuration

Metalnx has a bias towards deployment as a Docker image, and will continue to evolve so that it can be clustered
and scaled horizontally using an orchestration tool such as Kubernetes, as
part of a suite of mid-tier components that can work together to create a complete CI environment.

## /etc/irods-ext

This project contains a sample `/etc/irods-ext` directory with a `metalnx.properties` and `metalnxConfig.xml` examples.
These two files must be mounted as a volume at `/etc/irods-ext`, or be present in the same directory when running
in a web container directly on the host OS.

Generally, Metalnx is configured through a `metalnx.properties` file, documented below. This controls the optional behaviors
and customizations with the exception of theming (custom images, banners, css, messages). 

In addition, theming via assets is controlled by the required `metalnxConfig.xml` file. This file must be in `/etc/irods-ext` or
you will receive invalid resource messages. The provided file will cause the default theme and messages to be utilized.

### App configuration (metalnx.properties)

`metalnx.properties` is read from `/etc/irods-ext/metalnx.properties` during deployment within the container. This file controls the optional behaviors and customizations. This file must be present for Metalnx to run.

### App theming (metalnxConfig.xml)

By default, Metalnx uses the resource support of [Spring MVC](https://docs.spring.io/spring/docs/4.1.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-config-static-resources).

The provided `/etc/irods-ext/metalnxConfig.xml` can be used to load the default baseline theming. This file must be present for Metalnx to run.  It controls the theming of the deployed application, including custom images, banners, css, javascript, and any internationalization of the user messages.

## Running Docker with these configs

A local directory with these two files must be volume mounted when running via Docker.

An example developer run of the docker image with custom configuration:

```
docker run -it \
-p 8080:8080 \
-v `pwd`/mylocal-irods-ext:/etc/irods-ext \
mylocal/metalnx:dev
```
