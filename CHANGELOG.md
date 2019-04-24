# Change Log

The format is based on [Keep a Changelog](http://keepachangelog.com/).

## [Unreleased]
### Changed
- `JSONAuto` renamed to `JSONDeserializer`
- `JSONAutoTest` renamed to `JSONDeserializerTest`

### Added
- `JSONAuto`: new version (pass-through to `JSONDeserializer`)

## [0.2] - 2019-04-23
### Changed
- `JSONAutoTest`: added more tests (derived classes)
- `JSONAuto`: added code to check for `@JSONName` annotation
- `JSONAutoTest`: added tests for `@JSONName` annotation

### Added
- `JSONName`: added annotation


## [0.1] - 2019-04-22
### Added
- `JSONAuto`: `parse` and `deserialize` functions
- `JSONAutoTest`
