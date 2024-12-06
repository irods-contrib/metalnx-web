# Roadmap

This document is designed to provide a high level overview of upcoming work on Metalnx.

The details of this work will be reflected in the particular issues and milestones within this repository.

## History

Metalnx 1.4 was open-sourced and contributed to the iRODS Consortium by Dell EMC in the Spring of 2017.

Read more at [History.md](./docs/History.md).

## Future

The iRODS Consortium is working to develop the [Zone Management Tool](https://github.com/irods/irods_client_zone_management_tool) (ZMT) as the administrative interface to an iRODS Zone.

Metalnx 3.0 will be refocused to provide best-in-class
 - Browse,
 - Search, and
 - Metadata capabilities.

The search capabilities of Metalnx will be configured to provide one or the other or both of:

 - Basic search will query only the iRODS Catalog through the General Query.  Initially, this will be more limited than the current Metalnx interface for reasons related to the Specific Query mentioned below.

 - Advanced search will utilize an external indexing service, initially focused on Elasticsearch, populated by the [iRODS Indexing Plugin](https://github.com/irods/irods_capability_indexing).

Along with some refactoring of existing features, the following functionality will be removed:

 - Removal of the dashboard

   This has been turned off by default since Metalnx 2.0.0.  It has external dependencies and does not scale well with many servers.  This will be better handled by ZMT.
   
 - Removal of user/group/resource management
 
   This should be completely handled by ZMT.

 - Removal of rodsadmin requirement

   Originally required for inserting specific queries into the iRODS Catalog, running them, and then removing them, this requirement can be removed if the Specific Queries themselves are made unnecessary.  GenQuery replacements will be provided where possible (GenQuery is limited with regards to boolean 'OR' searches, etc.).  As GenQuery itself gets smarter in the iRODS Server (using a new flex/bison parser), any missing Metalnx functionality can be re-included.

This work will provide a more maintainable Metalnx codebase and a clearer separation of concerns between an administrative GUI (ZMT) and user GUI (Metalnx).
