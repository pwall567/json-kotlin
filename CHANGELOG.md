# Change Log

The format is based on [Keep a Changelog](http://keepachangelog.com/).

## [Unreleased]
### Changed
- `JSONAuto`: added `JSONConfig` to functions
- `JSONDeserializer`: added `JSONConfig` to functions

## [0.5] - 2019-05-21
### Changed
- `JSONSerializer`: added serialization of `Pair` and `Triple`
- `JSONSerializerTest`: corresponding tests
- `JSONDeserializer`: added deserialization of `Pair` and `Triple`
- `JSONDeserializerTest`: corresponding tests
- `JSONDeserializer`: added checking of types marked nullable
- `JSONDeserializerTest`: corresponding tests
- `JSONDeserializer`: added use of custom deserialization from `JSONConfig`
- `JSONDeserializerTest`: corresponding tests
- `JSONSerializer`: added use of custom serialization from `JSONConfig`
- `JSONSerializerTest`: corresponding tests

### Added
- `JSONConfig`: new configuration class

## [0.4] - 2019-05-14
### Changed
- `JSONDeserializerTest`: modified to use `KType` values
- `JSONSerializer`: optimisation of data classes
- `JSONAuto`: added `stringify()`

## [0.3] - 2019-04-26
### Changed
- `JSONAuto` renamed to `JSONDeserializer`
- `JSONAutoTest` renamed to `JSONDeserializerTest`

### Added
- `JSONAuto`: new version (pass-through to `JSONDeserializer`)
- `DummyClasses` (test): split out from `JSONDeserializerTest`
- `JSONSerializer`
- `JSONSerializerTest`
- `JSONIgnore` annotation class

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
