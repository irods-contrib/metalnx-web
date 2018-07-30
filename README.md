![Metalnx Logo](docs/IMAGES/mlx_logo_blue.png)

## Version: 2.0.1-SNAPSHOT
## Git Tag:
## Date:

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


#### Graceful page showing no access to requested item #46

Add a configurable and defeatable handler for collection browser and info pages that can show a no-access page with basic
data and metadata. This is especially useful in scenarios where an external index has produced a link to an iRODS path. By default,
no access pages are shown. If enabled, the rods admin account is used as a proxy to get a snapshot of basic catalog info about a file and its
metadata. The user can then opt to request access. In the initial implementation this is done via an email sent by the new configurable
email service.

This is configured in the sample etc/irods-ext/metalnx.properties file like so:

```

# Global switch to allow view of a 'basic data and metadata' info view when the user does not have permission to the underlying file.
# this allows a path to be resolved via the rods proxy admin account so that a 'no access' page is displayed with the ability to request access.
# This covers cases where an external search has generated a link to a file the user has no existing permission to see
access.proxy=false

```

#### Add configurable email service    

Add a global configuration in metalnx.properties for setting an SMTP mail service, allowing metalnx to send email messages. This is done initially to
provide a 'request access' for a collection or data object but can be expanded later. This requires the setting of the following new
metalnx.properties settings (reflected in the example etc/irods-ext/metalnx.properties)

```
mail.enable=false
mail.default-encoding=UTF-8
mail.host=some.smpt.org
mail.username=
mail.password=
mail.port=25
mail.smtp.starttls.enable=true
mail.smtp.auth=false
mail.transport.protocol=smtp
mail.debug=true

```

Note that a default email address for rods admin is now configurable in metalnx.properties at:

```
# may be left blank this is the default email for any messages generated from metalnx
irods.admin.email=
```

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


#### NIEHS identified misc theming issues

* #22 fix search text

* #25 search - default to 'contains'
