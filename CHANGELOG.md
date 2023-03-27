# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

### Added

- [#317] Build no longer pollutes personal .m2 directory.
- [#191] Added support for hiding metadata based on a set of prefixes.

### Changed

- [#247,#337,#343] Bumped version of Jargon for bug fixes.
- [#294] Create IRODSFileSystem and IRODSAccessObjectFactory only once.
- [#332] Set createParentCollections to true when calling createCollection
- [#191] Made it so that rodsadmins can always see metadata.
- [#321] Bumped Jargon version.
- [#255] User is now asked to confirm changes to permissions.
