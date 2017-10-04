This post provides you a guideline on how to package Metalnx into `RPM` and `DEB` packages.

**If you have already installed Metalnx following the [Getting Started](https://github.com/Metalnx/metalnx-web/wiki/Getting-Started) notes, this step is not required.**

# Compilation
Before start packaging anything, make sure you have the Metalnx `war` available. For more information about compiling the Metalnx Web project, check out the [Build Metalnx](https://github.com/Metalnx/metalnx-web/wiki/Build-Metalnx) post in this Wiki.

# Create `rpm` and `deb` packages

Return to the root of the Metalnx repository you build Metalnx from (`<your repository directory>/metalnx-web`), you should see a folder called `packaging`. This folder contains all scripts and configuration files necessary to create both `rpm` and `deb` packages.

Metalnx provides a script for `rpm` package creation and another script for `deb` package creation. Both scripts take two input parameters:

1. Version number - specifies which version of Metalnx the RPM package refers, currently it should be 1.1.1 or higher. 
2. Build number - It specifies the number of the current build. If you are building Metalnx for the first time, it should be 1.  You can increment this second (pass) number on each build.

## `rpm`

Under `metalnx-web/packaging/rpm` is the file `emc-metalnx-webapp.spec` file which tells rpm-build how to build the Metalnx RPM package and a file list for all the binaries that get included in the package. 

To create the package use the script `create_rpm_package.sh` located at `metalnx-web/packaging/scripts`. This script was uses a environment variable, WORKSPACE, to track where the top level directory for the build is.  This variable must be set prior to using this script.  An example on how to create a RPM package using this script is:

    $ WORKSPACE=$(pwd) ./packaging/scripts/create_rpm_package.sh 1.0 1

When the script is finished you will have an `rpm` packaged named `emc-metalnx-webapp-X.X.X-X.noarch.rpm`  ready for deployment.  This will will be located at `~/rpmbuild/noarch`. 

## `deb`

Under `metalnx-web/packaging/deb/DEBIAN` is a `control` file. This file is used to create the Metalnx DEB package. The DEB creation script is `create_deb_package.sh` located at `metalnx-web/packaging/scripts`. 

This script was uses a environment variable, `WORKSPACE`, necessary to track where the top level directory for the build is.  This variable must be set prior to using this script. An example on how to create a `deb` package using this script is:

    $ WORKSPACE=$(pwd) ./packaging/scripts/create_rpm_package.sh <version-number> <build-number>

After the script is done, you will have a file named `emc-metalnx-webapp-X.X.X-X.deb`  ready for deployment located at `/tmp/emc-tmp/`. 