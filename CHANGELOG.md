# Change Log

The format is based on [Keep a Changelog](http://keepachangelog.com/).

## [4.4] - 2021-09-16
### Changed
- `JSONDeserializer`: improved type determination for parameterised types with upper bound
- `pom.xml`: bumped dependency versions

## [4.3] - 2021-05-04
### Changed
- several: switched to use `typeOf()`
- `pom.xml`: added compiler switch
- `JSONDeserializer`, `JSONDeserializerFunctions`: add validation of UUID (workaround for Java bug)

## [4.2] - 2021-04-19
### Changed
- `pom.xml`: updated dependency versions
- `JSONFun.kt`: removed `value` extension variables (no longer necessary)

## [4.1] - 2021-02-28
### Changed
- `JSONDeserializer`: added duplicate check on deserialization of `Set`

## [4.0] - 2021-01-10
### Added
- `JSONKotlinException`: New exception class
### Changed
- several: switched to use `JSONKotlinException`
- `JSONDeserializer`: make use of pointer on error messages
- `JSONDeserializer`: improved error messages on deserializing objects

## [3.16] - 2021-01-07
### Changed
- `JSONFun.kt`: added `value` extension variables for `JSONValue` primitive types

## [3.15] - 2021-01-03
### Added
- `USERGUIDE.md`
- `CUSTOM.md`
### Changed
- `pom.xml`: updated dependency versions
- `JSONDeserializer`: simplified `deserializeAny`
- `JSONSerializer`, `JSONDeserializer`, `JSONStringify`: added support for Java Streams
- `JSONConfig`: bug fix in `combineAll`

## [3.14] - 2020-11-30
### Changed
- `JSONFun`: additional extension functions on `JSONValue`
- `JSONConfig`: bug fix on `fromJSON`
- `JSONDeserializer`: minor bug fix

## [3.13] - 2020-11-25
### Changed
- `JSONAuto`: added more helper functions

## [3.12] - 2020-11-25
### Changed
- `JSONDeserializer`: modified to allow use by `YAMLSequence` and `YAMLMapping`
- `README.md`: added badges
### Added
- `travis.yml`

## [3.11] - 2020-09-16
### Changed
- `pom.xml`: updated to Kotlin 1.4.0

## [3.10.1] - 2020-08-25
### Changed
- `pom.xml`: fixed generation of sources jar
- `JSONJava`, `JSONJavaTest`: moved to kotlin directory (to allow sources jar generation)

## [3.10] - 2020-08-25
### Changed
- `JSONDeserializer`: further improvements to handling of parameterized types

## [3.9] - 2020-08-20
### Changed
- `JSONConfig`: changed `toJSON` and `fromJSON` to use `JSONTypeRef`

## [3.8] - 2020-07-12
### Changed
- `JSONDeserializer`: improved handling of parameterized types

## [3.7] - 2020-07-07
### Changed
- `JSONDeserializerFunctions`: added `hasSingleParameter` and `findParameterName` (moved from `JSONDeserializer`)
- `JSONDeserializer`: improved deserialization of objects with missing parameters

## [3.6] - 2020-05-03
### Added
- `JSONDeserializerFunctions`:  (split out from `JSONDeserializer`)
### Changed
- `JSONDeserializerFunctions`: improved handling of `fromJSON` (in companion object)

## [3.5] - 2020-04-23
### Changed
- `JSONConfig`: added `streamOutput` switch (for `json-ktor`)
- `JSONSerializerFunctions`: changed `toJsonCache` handling (more efficient)
- `JSONSerializer`, `JSONSerializerFunctions`: improved handling of classes output by `toString()`
- `JSONDeserializer`, `JSONSerializerFunctions`: add classes from `java.time` package

## [3.4.1] - 2020-04-21
### Changed
- `pom.xml`: updated dependency versions

## [3.4] - 2020-04-18
### Changed
- `JSONSerializer`, `JSONStringify`: added checks for circular references

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
