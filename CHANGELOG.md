# Change Log

The format is based on [Keep a Changelog](http://keepachangelog.com/).

## [3.3] - 2020-04-18
### Added
- `JSONSerializerFunctions`: common functionality for `JSONSerializer`, `JSONStringify` etc. (split out from
`JSONSerializer`)
### Changed
- `JSONDeserializer`: added check for `@JSONIgnore`
### Removed
- `TestAnnotatedClasses.kt`, `TestSealedClasses.kt`: moved to `json-kotlin-test-classes`

## [3.2] - 2020-04-13
### Changed
- `JSONConfig`, `JSONAuto`, `JSONFun`, `JSONSerializer`: accommodate JSONStringify
- `JSONTypeRef`: added `nullable` parameter
### Added
- `JSONStringify`: stringify without intermediate JSONValue tree

## [3.1] - 2020-04-05
### Changed
- `JSONConfig`, `JSONSerializer`: added `includeNulls` switch
- `JSONConfig`, `JSONSerializer`: added annotations to specify that null properties are to be included
- `JSONConfig`, `JSONDeserializer`: added `allowExtra` switch
- `JSONConfig`, `JSONDeserializer`: added annotations to specify that extra properties are to be ignored
### Added
- `JSONIncludeIfNull`: Annotation class
- `JSONIncludeAllProperties`: Annotation class
- `JSONAllowExtra`: Annotation class

## [3.0] - 2020-01-27
### Added
- `JSONTypeRef`: implementation of the [TypeReference](https://gafter.blogspot.com/2006/12/super-type-tokens.html)
pattern
### Changed
- `JSONAuto`, `JSONDeserializer`: changed to use `JSONTypeRef`
- `JSONFun`: improved `toKType`
- `JSONConfig`: added switches to select handling of `BigInteger` and `BigDecimal`
- Several: switched to version 3.0 of `jsonutil` - improved handling of decimal fractions (`JSONDecimal`)

## [2.1] - 2019-12-11
### Changed
- `JSONFun`: added new `targetKType` that takes nested `KType`
- `JSONFun`: added `CharSequence.parseListJSON()`, `CharSequence.parseSetJSON()` and `CharSequence.parseMapJSON()`

### Added
- `JSONDelegate`: delegate class to assist in deserializing parameterized classes.

## [2.0] - 2019-11-17
### Changed
- `JSONConfig`: Sealed class discriminator property may now be specified
- `JSONConfig`: Modifying functions that used to return `this` for chaining no longer do so (breaking change)


## [1.2] - 2019-11-11
### Changed
- `JSONSerializer`: Added serialization of sealed classes
- `JSONDeserializer`: Added deserialization of sealed classes
- Tests: switched to use of external repository (to avoid compilation order issues)

## [1.1] - 2019-11-03
### Changed
- Updated version to 1.0
- `JSONConfig`: improved selection of custom serialization and deserialization functions
- `JSONConfig`: added shortcut  custom serialization / deserialization functions using `toString()` and constructor
  taking `String` parameter
- `JSONDeserializer`: changed to use new custom serialization and deserialization functions
- `JSONSerializer`: changed to use new custom serialization and deserialization functions
- Several: added more KDoc

### Added
- `JSONJava`: to access Kotlin serialization and deserialization from Java

## [1.0] - 2019-10-08
### Changed
- Updated version to 1.0
- `JSONDeserializer`: improved deserialization, including defaults if types not specified

## [0.13] - 2019-09-22
### Changed
- `JSONConfig`: added `charset`

## [0.12] - 2019-09-19
### Changed
- `JSONFun`: changed name of `targetJSON` function to `targetKType`
- `JSONFun`: added `toKType` extension function
- `JSONDeserializer`: added function to take Java `Type`
- `JSONAuto`: added function to take Java `Type`

## [0.11] - 2019-09-17
### Changed
- `JSONDeserializer`: added shared function to access type parameters
- `JSONAuto`: added `targetJSON` function

## [0.10] - 2019-09-11
### Changed
- `JSONConfig`: added more KDoc
- `JSONConfig`: added custom name and ignore annotation specification
- `JSONConfig`: added `defaultConfig` (and added use of default to other functions)
- `JSONConfig`: added combine functions
- `JSONDeserializer`: changed to use custom name and ignore annotations
- `JSONFun`: changed `asJSON` to `asJSONValue`; changed `toJSON` to `isJSON`
- `JSONFun`: added coverage of data types `Char`, `Short`, `Byte`
- `JSONFun`: added added more extensions functions
- `JSONFun`: added KDoc
- `JSONSerializer`: changed to use custom name and ignore annotations
- `JSONDeserializer`: improved enum handling

## [0.9] - 2019-07-17
### Changed
- `JSONAuto`: added optional `JSONConfig` to all methods
- `JSONAuto`: Changed `parse` to take `CharSequence` instead of `String`
- `JSONConfig`: fixed typo in method names
- `JSONDeserializer`: added deserialization of `java.util.BitSet`
- `JSONDeserializer`: switched to use `JSONInt` `typealias`
- `JSONFun`: renamed `toJSON` to `isJSON`
- `JSONFun`: added `Any?.asJSON()`
- `JSONFun`: added `CharSequence.parseJSON()`
- `JSONFun`: added `Any?.stringifyJSON()`
- `JSONSerializer`: added serialization of `java.math.BigDecimal`, `java.math.BigInteger`, `java.net.URI`,
  `java.net.URL`
- `JSONSerializer`: fixed typo - `invokeSetter` should have been `invokeGetter`
- `JSONSerializer`: switched to use `JSONInt` `typealias`
- `pom.xml`: switched to use of parent POM
- `pom.xml`: added link to `jsonutil` JavaDoc

## [0.8] - 2019-07-08
### Changed
- `JSONConfig`: changed to use system default buffer size
- `JSONSerializer`: added serialization of `java.sql.Date`, `java.sql.Time`, `java.sql.Timestamp`
- `JSONDeserializer`: added deserialization of `java.sql.Date`, `java.sql.Time`, `java.sql.Timestamp`
- `README.md`: documentation added (more needed)

### Added
- `JSONFun`: JSON helper functions
- `JSONFunTest`: tests for JSON helper functions

## [0.7] - 2019-07-06
### Changed
- `JSONDeserializer`: added deserialization of Kotlin `object`s (serialization already worked)
- `JSONSerializer`: improved serialization of Java objects
- `JSONDeserializer`: improved deserialization of Java objects
- `JSONSerializer`: added serialization of `java.time.Duration`, `java.time.Period`
- `JSONDeserializer`: added deserialization of `java.time.Duration`, `java.time.Period`
- `JSONConfig`: added `readBufferSize`

## [0.6] - 2019-05-23
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
