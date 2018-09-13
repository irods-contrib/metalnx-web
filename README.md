![Metalnx Logo](docs/IMAGES/mlx_logo_blue.png)

## Version: 2.0.0-SNAPSHOT
## Git Tag:
## Date: Oct 27, 2017

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

#### Add properties based global control of features targeted at first towards removing tickets niehs #52

Add a global config to turn on/off certain features via metalnx.properties. This allows sites to globally turn off features such as tickets.

#### Add normal/advanced view niehs #17

Add preferences to toggle between normal/advanced view and made dataGridUser.advancedView a model attribute always available in thymeleaf pages so
that the interface can show or hide features based on normal or power users

#### Make sidebar a fragment #28

Sidebar nav a thymeleaf fragment to reduce redundancy in custom templates

#### File upload when no resource defined can result in NPE #29

While it needs more investigation, the upload processing that does resource searching and building
of metadata to run file-dependent rules on uploads was getting NPE when a resource was not specified
during upload. This now will turn off automatic processing in this case, pending further hardening and clarification
of that functionality. In addition, a new metalinx.properties value that can turn off
this global rules application on upload.

```
metalnx.enable.upload.rules=false

```

This change requires the addition of this property to metalnx.properties,and for unit testing and building
this property should be in settings.xml. See the CONFIGURATION.md and DEVELOPER-README.md for details. The sample
metalnx.properties in /etc/irods-ext in this repo shows a sample configuration.

#### NIEHS 500 Error on empty trash

Incorporated new TrashOperationsAO code from https://github.com/DICE-UNC/jargon/issues/280

This replaces the rule call, and now functions normally for logged in users. There remains a few issues with empty trash as rodsadmin but
that will be addressed at the Jargon or iRODS level.

#### return from search to collections using deep links #34

As a transitional measure, the current favorites, search, and bookmarks functions have a listing that is distinct from the
main collections browser view. In order to support deep linking and a reasonably functioning back button, selecting an item
from any of these search views opens the collections view using the deep link approach in a new tab. This gives a reasonable
experience that can suffice until the collections browser can be refactored to unify all of these searches into the same view.
Maps to NIEHS #70

#### browsing up to zone or home for non-admin user niehs #128

Enhance ability to browse down from root as non-admin user using heuristics and relaxing permission checks for a more intuitive experience

#### URL encoding issues with collection browser links

While still somewhat transitional (still some inconsistancies in how URLs and javascript methods operate) this generally improves support
for files with embedded spaces and special characters. NIES #134

#### Add configurable download limit niehs #173

Add a global configuration setting for a download limit on files for both single file and bundle downloads to metalnx.properties

#### Clicking on the rules icon in the left menu starts a repeating loop of rules page refreshes. #54

Added logic in RuleDeploymentService to create the /zone/.rulecache file if it does not exist in an installation. This is done using the
iRODS admin account specified in metalnx.properties. The absense of the .rulecache file was causing the errors in MetaLnx collection browsing.

#### Replicating files onto multiple storage resources during metalnx upload is not working #53

Replaced rule based replication with direct call to work around a possible iRODS rule engine bug

#### collection paging (page size) not working #52

Temporarily turned off collection paging as the jQuery data tables is exhibiting some sort of bug where the paging chrome is displayed,
but paging and paging size are not active. This will be revisited in a collection listing cleanup and refactoring effort in a follow up release.

#### Metalnx 2.0 src files must be updated with BSD 3-clause license #46

Updated licensing, code to reflect transition of codebase to iRODS Consortium

#### PR #67 from JustKyle addding drag and drop file uploading

Community feature adding drag and drop to file uploading. Thanks to Kyle!

#### switching rodsadmin <-> rodsuser seems to not result in a change to interface

Allow Metalnx DB to update user type based on changes to the iRODS role of a user. This
bug prevented changing from rodsuser to rodsadmin and would not show admin features.

#### Fix Rule error listing microservices

Fix stack traces that resulted from acquiring list of microservices for the dashboard by switching to calling jargon environment services to obtain the microservice list

Quieted stack traces when MSIs not installed by respecting the metalnx.properties

```
populate.msi.enabled=false

```


#### NIEHS identified misc theming issues

* #22 fix search text

* #25 search - default to 'contains'

* #11 Consider removing Jquery data table search filter as confusing next to the planned global search
