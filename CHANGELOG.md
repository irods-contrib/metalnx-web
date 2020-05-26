# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

### Added

#### npm install fails in make #142

Added a Docker build image (Dockerfile.testbuild). This mounts the source directory and provides a command prompt to build the Metalnx war, including providing node support

See comments in Docker file, but essentially cd into the top level directory of the git repo and then, after building,  issue

```

docker run -it -v `pwd`/src/:/usr/src/metalnx metalnx-build /bin/bash

```

This will provide a command prompt and allow maven commands, mainly:

```

mvn package -DskipTests

```


#### Add pluggable search #110

Added the ability to plugin standard search endpoints (see docs/PluggableSearch.md) for configuration details. This allows the provisioning of standard
REST endpoints that provide the ability to plug in custom search services. This is a microservice that can advertise multiple search schema, including search attributes and their meaning. These endpoints can take free text, free text with XXX:term attribute tags, and builder type attribute|operator|value (AND|OR) type queries.

These endpoints can return standard search results that can be highly customizable, or they can return standard virtual collection type listings. This capability will be eventually surfaced as a type of virtual collection.

The entire pluggable search framework is off by default, and can be enabled and configured via metalnx.properties. See the etc/irods-ext/metalnx.properties sample


#### Add test framework for docker-compose #140

Refer to the README.md file in the docker-test-framework for instructions. This adds a docker-compose framework to bootstrap a metalnx database and web container running in a standard configuration. This will eventually become a foundation for automated functional tests (e.g. Selenium tests). The framework links to the Jargon docker test framework which can bootstrap a standard iRODS grid test image.

### Changed

#### bump up max upload for metalnx to 100 GB #143

Updated Docker build to increase file upload sizes
