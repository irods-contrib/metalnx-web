# MetaLnx Configuration and Installation notes

MetaLnx has a bias towards deployment as a Docker image, and will continue to evolve so that it can be clustered
and scaled horizontally using an orchestration tool such as Kubernetes, as
part of a suite of mid-tier components that can work together to create a complete CI environment.

Generally, MetaLnx is configured through a metalnx.properties file, documented below. This file is expected to
be in the /etc/irods-ext directory. Commonly, this is a volume that can be mounted to the Dockerized image
from an arbitrary location.

In addition, theming via assets provided from a configured location works the same way. The metalnx.properties
utilizes the resource pipeline features of Spring MVC, and default to the built-in assets (css, images, etc). These
properties can optionally override the default location within the static web app to favor assets
mounted under the /opt/irods-ext directory.

## metalnx.properties

metalnx.properties is read from /etc/irods-ext/metalnx.properties during deployment. The Docker
image thus expects that file to be mounted as a volume.  See the sample-metalnx.properties file in this
repository for a template for the /etc/irods-ext/metalnx.properties file expected by the application.

## Web app theming and customization

By default, MetaLnx uses the resource support of Spring MVC, e.g. https://docs.spring.io/spring/docs/4.1.9.RELEASE/spring-framework-reference/html/mvc.html#mvc-config-static-resources.

The metalnx.properties file contains default search paths for various static assets that can be used for theming, for example:

```properties

######################################
# resource Configuration
######################################

resource.location.images=/images/,classpath:static/images/
resource.location.fonts=/fonts/,classpath:static/fonts/
resource.location.css=/css/,classpath:static/css/
resource.location.js=/js/,classpath:static/js/
resource.location.i18=classpath:i18n/messages
resource.location.i18-users=classpath:i18n-users/messages


```

Note that by default, MetaLnx is looking at internal static files from the webapp and classpath, thus no
special customization is needed to run the base image. This does allow a site-specific override
of the css, image, js, as well as the i18n message resource bundles. This is set up so that a custom file:/
location may be prepended.

TODO: example of this after testing - MC
