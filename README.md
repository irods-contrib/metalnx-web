![Metalnx Logo](docs/IMAGES/mlx_logo_blue.png)

## Version: 4.2.1.0-SNAPSHOT
## Git Tag: niehs/issue2
## Date: Oct 27, 2017


#### This is a fork of the open source Metalnx browser as a candidate basis for Cloud Browser II. This is meant to develop as a generalized tool with hooks, plugins, and theming to allow use in a broader community. Please join the project if interested!


Metalnx is a web application designed to work alongside the [iRODS - Integrated Rule-Oriented Data System](http://www.irods.org). It provides a graphical UI that can help simplify most administration,
collection management, and metadata management tasks removing the need to memorize the long list of icommands.

### Install Metalnx

Note that this fork adds a schema configuration and migration tool to set up and migrate the database over versions. This
actually is a start of a more formalized irods-ext database which will evolve to support an implementation of virtual collections,
metadata templates, and the like. See the README.md in the metalnx-tools subproject for details on setting up and migrating the database schema.

Check out [Getting-Started](https://github.com/Metalnx/metalnx-web/wiki/Getting-Started) for installation instructions.

### Documentation

Check out the [Metalnx Wiki](https://github.com/Metalnx/metalnx-web/wiki) for further information.

### License

Copyright © 2015-2017, Dell EMC.

This software is provided under the Software license provided in the <a href="LICENSE.md"> LICENSE </a> file.

### Changes

#### Add SSL cert management support #2

Add ability to import iRODS SSL self-signed cert into jvm keystore when using SSL transport

#### Investigate formalized schema deployment #5

Adding formalized database schema setup and migration tools using flywaydb. See the metalnx-tools
subproject and its README for a maven based database setup and migration tool.

#### setting of jargon props (ssl negotiation) via etc properties #10

Added ability to configure SSL negotiation and other properties in metalnx.properties and have them propogate to the settable jargon properties. This may
eventually be pulled out to a stand alone utilities package for use across mid tier components.

#### Obtain necessary props from irods-ext file #7

Switched to the existing standardized /etc/irods-ext method of defining properties picked up by spring for interpolation in
bean configuration. This is especially useful for docker deployments as the /etc/irods-ext can be a volume mount for docker

#### ticket tests failing when run together, possible iRODS issue #16

Did some cleanup and temporarily put aside several ticket tests for further assessment at the iRODS and Jargon layer. There maybe some
remaining issues with tickets, see https://github.com/DICE-UNC/jargon/issues/266 so this will be revisited at that
layer as soon as possible. These changes allow a clean unit test baseline

#### Adjust props,etc so that unit tests are clean before proceding #14

Normalizing the test setup, properties generation scheme to align with cloud browser, rest, jargon, etc. This will allow
easier setup in iRODS CI, etc. Simplified the spring config propertis references to look at the metalnx.properties in /etc/irods-ext for production in issue #10 and #7, and this change utilizes build of test.metalnx.properties and testing.properties from the pom
based on settings.xml as in jargon and other libs.

See the DEVELOPER-README.md doc for details on how to set up and run tests.

#### Add configurable based resource pipeline #13

Add facilities to allow site-specific customization of the browser (css,logo, resource bundles, etc). See CONFIGURATION.md for instructions. This change does require /etc/irods-ext files to be put into place for the resource locations. This may be refactored at a later time, but provides a first clean separation between customizations based on css, messages, images, etc. Some clean up needs to be done to fully internationalize text, etc. This is being addressed in other issues.

#### metalnx specific queries only operate vs. postgres #15

Isolated specific queries and reference client hints to determine iRODS catalog type, this uses a factory arrangement to obtain a source for SQL queries. This is now scaffolded with unit tests of existing specific queries. MySql semantics are being added...work in progress

Integrated changes from issue 15 in consortium codebase, mapping to issue #43 in NIEHS.

#### Clean out non-internationalized items in views #19

Remove remaining hard coded text in templates and convert to resource bundle refs mapped to niehs issue #44

#### Update selenium test unify testing framework #45

Incrementally going through the Selenium tests to unify with the Jargon testing framework and to reactivate ignored selenium tests.  See the CONFIGURATION.md file for information on Selenium test setup, which is run from the src/emc-metalnx-web directory. That directoy also includes a test-scripts folder with the required maven settings.xml updates.
The current Selenium tests have been refactored to start with basic health checks while the page functions stabilize.

#### Fix 500 errors clicking on zone or home when no permissions

Updated Jargon and controller code to gracefully handle no permission errors with a helpful message and a return to the previous directory view

#### NIEHS identified misc theming issues 

* #22 fix search text
