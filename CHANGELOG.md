# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0] - XXXX
### Added

#### Add SSL cert management support #2

Add ability to import iRODS SSL self-signed cert into jvm keystore when using SSL transport

#### Investigate formalized schema deployment #5

Adding formalized database schema setup and migration tools using flywaydb. See the metalnx-tools
subproject and its README for a maven based database setup and migration tool.

#### setting of jargon props (ssl negotiation) via etc properties #10

Added ability to configure SSL negotiation and other properties in metalnx.properties and have them propogate to the settable jargon properties. This may
eventually be pulled out to a stand alone utilities package for use across mid tier components.

#### Add configurable based resource pipeline #13

Add facilities to allow site-specific customization of the browser (css,logo, resource bundles, etc). See CONFIGURATION.md for instructions. This change does require /etc/irods-ext files to be put into place for the resource locations. This may be refactored at a later time, but provides a first clean separation between customizations based on css, messages, images, etc. Some clean up needs to be done to fully internationalize text, etc. This is being addressed in other issues.

#### Add properties based global control of features targeted at first towards removing tickets niehs #52

Add a global config to turn on/off certain features via metalnx.properties. This allows sites to globally turn off features such as tickets.

#### Add normal/advanced view niehs #17

Add preferences to toggle between normal/advanced view and made dataGridUser.advancedView a model attribute always available in thymeleaf pages so
that the interface can show or hide features based on normal or power users

#### Add configurable download limit niehs #173

Add a global configuration setting for a download limit on files for both single file and bundle downloads to metalnx.properties

#### Metalnx 2.0 src files must be updated with BSD 3-clause license #46

Updated licensing, code to reflect transition of codebase to iRODS Consortium

#### PR #67 from JustKyle addding drag and drop file uploading

Community feature adding drag and drop to file uploading. Thanks to Kyle!

#### switching rodsadmin <-> rodsuser seems to not result in a change to interface

Allow Metalnx DB to update user type based on changes to the iRODS role of a user. This
bug prevented changing from rodsuser to rodsadmin and would not show admin features.

#### add 'copyable' path for collection and data object #72

Add clipboard functionality to paths on the info views to allow easy copy to clipboard on the colleciton and data object info pages

#### Add inherit flag and ability to update to coll info view

Added inheritance flag to the info view for collections and allow modification

#### Add mapping to friendly names for auth types #88

Add ability to change login options to site-specific. This means instead of showing "PAM" as a Login
option it can say "Example Corp. Account", etc. The property is described in the etc/irods-ext sample in this subproject

```

#############################
# misc ui configuration niceties
#############################
# allow translation of iRODS auth types to user friendly names in login
# in the form irodstype:displaytype|
metalnx.authtype.mappings=PAM:NIH Login|STANDARD:iRODS Auth

```

### Changed

#### Obtain necessary props from irods-ext file #7

Switched to the existing standardized /etc/irods-ext method of defining properties picked up by spring for interpolation in
bean configuration. This is especially useful for docker deployments as the /etc/irods-ext can be a volume mount for docker

#### Adjust props,etc so that unit tests are clean before proceding #14

Normalizing the test setup, properties generation scheme to align with cloud browser, rest, jargon, etc. This will allow
easier setup in iRODS CI, etc. Simplified the spring config propertis references to look at the metalnx.properties in /etc/irods-ext for production in issue #10 and #7, and this change utilizes build of test.metalnx.properties and testing.properties from the pom
based on settings.xml as in jargon and other libs.

See the DEVELOPER-README.md doc for details on how to set up and run tests.

#### metalnx specific queries only operate vs. postgres #15

Isolated specific queries and reference client hints to determine iRODS catalog type, this uses a factory arrangement to obtain a source for SQL queries. This is now scaffolded with unit tests of existing specific queries. MySql semantics are being added...work in progress

Integrated changes from issue 15 in consortium codebase, mapping to issue #43 in NIEHS.

#### Update selenium test unify testing framework #45

Incrementally going through the Selenium tests to unify with the Jargon testing framework and to reactivate ignored selenium tests.  See the CONFIGURATION.md file for information on Selenium test setup, which is run from the src/metalnx directory. That directoy also includes a test-scripts folder with the required maven settings.xml updates.
The current Selenium tests have been refactored to start with basic health checks while the page functions stabilize.

#### Fix 500 errors clicking on zone or home when no permissions

Updated Jargon and controller code to gracefully handle no permission errors with a helpful message and a return to the previous directory view

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

#### Clicking on the rules icon in the left menu starts a repeating loop of rules page refreshes. #54

Added logic in RuleDeploymentService to create the /zone/.rulecache file if it does not exist in an installation. This is done using the
iRODS admin account specified in metalnx.properties. The absense of the .rulecache file was causing the errors in MetaLnx collection browsing.

#### Replicating files onto multiple storage resources during metalnx upload is not working #53

Replaced rule based replication with direct call to work around a possible iRODS rule engine bug

#### Fix Rule error listing microservices

Fix stack traces that resulted from acquiring list of microservices for the dashboard by switching to calling jargon environment services to obtain the microservice list

Quieted stack traces when MSIs not installed by respecting the metalnx.properties

```
populate.msi.enabled=false

```

#### Add override of login over metalnx.properties to allow standard or PAM

Add a dropdown that can select an alternative authentication method (currently standard or pam auth). This defaults to the pre-configured auth method in metalnx.properties.

#### 'remembering' login/exception as a last path causes display of error when login succeeded

Overhaul of handling of session/login timeout behavior. This is complicated by the mixing of 'thymeleaf' server rendered pages mixed with AJAX operations and caused inconsistent behavior where sometimes the operation before the timeout would be remembered and sometimes it would not. In addition, a login exception would cause repeated redisplay of the login screen even when login had succeeded. The javascript 'ajax()' method was overloaded by wrapping the success handling code to look for the login page and record the current location as a ajaxOrigPath parameter That could be used to reposition the browser after a successful login. This was a pretty significant reworking of the timeout handling.

#### invalid coll browse when spaces in name #205 (NIEHS)

Better handling of special chars and spaces in file names when using the summary pop up right hand panel in the collection browser.

#### dashboard slow to load #84

Made the dashboard view optional, with the plan in later releases to revisit the performance monitor to
make it asynchronous and caching. This updates the metalnx.properties file to add a new optional

```

# show dashboard (off by default due to performance issues)
metalnx.enable.dashboard=false

```

#### change web context #87

The original web context 'emc-metalnx-web' and the deployed war file name has been updated to simply 'metalnx'. In later versions,
this will be site-configurable, but for now there is some legacy hard coded path information.

#### metadata query page functionality not working #96

Metadata paging and page size changes were not working. Fixed code to respond properly to paging and to resize and limit results per user input on the results table.

### Removed

#### ticket tests failing when run together, possible iRODS issue #16

Did some cleanup and temporarily put aside several ticket tests for further assessment at the iRODS and Jargon layer. There maybe some
remaining issues with tickets, see https://github.com/DICE-UNC/jargon/issues/266 so this will be revisited at that
layer as soon as possible. These changes allow a clean unit test baseline

#### Clean out non-internationalized items in views #19

Remove remaining hard coded text in templates and convert to resource bundle refs mapped to niehs issue #44

#### collection paging (page size) not working #52

Temporarily turned off collection paging as the jQuery data tables is exhibiting some sort of bug where the paging chrome is displayed,
but paging and paging size are not active. This will be revisited in a collection listing cleanup and refactoring effort in a follow up release.
