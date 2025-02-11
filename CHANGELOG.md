# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project **only** adheres to the following _(as defined at [Semantic Versioning](https://semver.org/spec/v2.0.0.html))_:

> Given a version number MAJOR.MINOR.PATCH, increment the:
> 
> - MAJOR version when you make incompatible API changes
> - MINOR version when you add functionality in a backward compatible manner
> - PATCH version when you make backward compatible bug fixes

## [3.0.0] - 2025-02-11

This major release removes the need for the PostgreSQL database, improves compatibility with iRODS 4.3, and updates several dependencies.

The removal of the PostgreSQL database is particularly important as it allows Metalnx to better reflect the state of the iRODS zone.

### Changed

- Update dependencies - spring, node, dom4j, etc (#307).
- Expose 10-level iRODS permission model for iRODS 4.3 and later (#342).
- Update Tomcat Docker image to v9.0.98 (#359).
- Server no longer requires default resource to be configured (#373).
- Update log4j from v1 to v2 (#374).

### Removed

- Remove dependency on PostgreSQL database (#214, #327).
- Remove preferences form and related code (#370).

### Fixed

- Fix CSV download of metadata query results (#355).
- Do not allow updates to groups while adding new user (#369).

### Added

- Allow link to sidebar public collection link to be hidden (#189).

## [2.6.1] - 2023-03-27

### Changed

- [#247,#337,#343] Bumped version of Jargon for bug fixes.
- [#294] Create IRODSFileSystem and IRODSAccessObjectFactory only once.
- [#332] Set createParentCollections to true when calling createCollection
- [#191] Made it so that rodsadmins can always see metadata.
- [#321] Bumped Jargon version.
- [#255] User is now asked to confirm changes to permissions.

### Added

- [#317] Build no longer pollutes personal .m2 directory.
- [#191] Added support for hiding metadata based on a set of prefixes.
