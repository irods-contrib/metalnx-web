# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

### Added


### Changed

#### #183 Temporarily remove non-functional drag and drop to the upload dialog, to be addressed in collection browser re-facing as a single page app

#### #195 downloading CSV of metadata search results fails w/ SYS_MALLOC_ERR 

Added unit tests and interim patch to csv download of metadata query results (set at 5000 result limit currently), made CSV results delimited by comma, as opposed to semi-colon