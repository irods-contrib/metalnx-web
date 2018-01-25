# MetaLnx Configuration and Installation notes

MetaLnx has a bias towards deployment as a Docker image, and will continue to evolve so that it can be clustered
and scaled horizontally using an orchestration tool such as Kubernetes, as
part of a suite of mid-tier components that can work together to create a complete CI environment.

### /etc/irods-ext

This project contains a sample /etc/irods-ext directory with a metalnx.properties and a metalnxConfig directory.
These two files must be mounted as a volume at /etc/irods-ext, or be present in the same directory when running
in a web container directly on the host OS.

Generally, MetaLnx is configured through a metalnx.properties file, documented below. This controls optthe ional behaviors
and customizations with the exception of theming (custom images, banners, css, messages). The metalnx.properties
in the provided /etc/irods-ext directory can serve as a template.

In addition, theming via assets is controlled by the required metalnxConfig.xml file. This file must be in /etc/irods-ext or
you will receive invalid resource messages. The provided file will cause the default theme and messages to be utilized. customizations
of the resource pipeline are done via standard SpringMVC.  See the 'Web app theming and customization section'.

## metalnx.properties

metalnx.properties is read from /etc/irods-ext/metalnx.properties during deployment. The Docker
image thus expects that file to be mounted as a volume.  See the metalnx.properties file in this
repository for a template for the /etc/irods-ext/metalnx.properties file expected by the application.

See the DEVELOPER-README.md and the README.md in the src/metalnx-tools for information on configuring the metalnx database.

## Web app theming and customization

By default, MetaLnx uses the resource support of Spring MVC, e.g. https://docs.spring.io/spring/docs/4.1.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-config-static-resources.

The provided /etc/irods-ext/metalnxConfig.xml can be used as provided to load the default baseline theming, this file must be present.

Note that by default, MetaLnx is looking at internal static files from the webapp and classpath, thus no
special customization is needed to run the base image. This does allow a site-specific override
of the css, image, js, as well as the i18n message resource bundles.

In order to customize the themes, it is up to the discretion of the deployer to provide a custom metalnxConfig.xml. The recommended practice is to point to a mounted /opt/irods-ext/metalnx mounted volume or host machine directory with the metalnxConfig based on
/etc/irods-ext/customMetalnxConfig.xml (the file must be renamed to metalnxConfig.xml).  


## Running Docker with these configs


An example developer run of the docker image with these configs...


```
#!/bin/bash
docker run -i -t \
-p 8080:8080 \
-v /Users/conwaymc/Documents/docker/ml_dfc/etc/irods-ext:/etc/irods-ext \
-v /Users/conwaymc/Documents/docker/ml_dfc/opt/irods-ext:/opt/irods-ext \
diceunc/metalnx:dev


```
