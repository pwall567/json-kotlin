# json-kotlin

JSON serialization and deserialization for Kotlin

## Background

This library provides JSON serialization and deserialization functionality for Kotlin using the pre-existing `jsonutil`
Java library.
It uses Kotlin reflection to serialize and deserialize arbitrary objects, and it includes code to handle most of the
Kotlin standard library classes.

When instantiating deserialized objects it does not require the class to have a no-argument constructor, and unlike some
JSON libraries it does not use the `sun.misc.Unsafe` class to force instantiation.

## Supported Classes

Support is included for the following standard Kotlin classes:

- `String`, `StringBuilder`, `CharSequence`, `Char`, `CharArray`
- `Int`, `Long`, `Short`, `Byte`, `Double`, `Float`
- `Array`, `IntArray`, `LongArray`, `ShortArray`, `ByteArray`, `DoubleArray`, `FloatArray`
- `Boolean`, `BooleanArray`
- `Collection`, `List`, `ArrayList`, `LinkedList`, `Set`, `HashSet`, `LinkedHashSet`, `Sequence`
- `Map`, `HashMap`, `LinkedHashMap`
- `Pair`, `Triple`
- `Enum`

Also, support is included for the following standard Java classes:

- `java.math.BigDecimal`, `java.math.BigInteger`
- `java.net.URI`, `java.net.URL`
- `java.util.Enumeration`, `java.util.Bitset`, `java.util.UUID`, `java.util.Date`, `java.util.Calendar`
- `java.sql.Date`, `java.sql.Time`, `java.sql.Timestamp`
- `java.time.Instant`, `java.time.LocalDate`, `java.time.LocalDateTime`, `java.time.OffsetDateTime`,
  `java.time.OffsetTime`, `java.time.ZonedDateTime`, `java.time.Year`, `java.time.YearMonth`, `java.time.Duration`,
  `java.time.Period`

## Quick Start

To serialize any object (say, a `data class`):
```kotlin
    val json = dataClassInstance.stringifyJSON()
```
The result `json` is a `String` serialized from the object, recursively serializing any nested objects, collections
etc.
The JSON object will contain serialized forms of all of the properties of the object (as declared in `val` and `var`
statements).

For example, given the class:
```kotlin
    data class Example(val abc: String, val def: Int, val ghi: List<String>)
```
and the instantiation:
```kotlin
    val example = Example("hello", 12345, listOf("A", "B"))
```
then
```kotlin
    val json = example.stringifyJSON()
```
will yield:
```json
{"abc":"hello","def":12345,"ghi":["A","B"]}
```

Deserialization is slightly more complicated, because the target data type must be specified to the function.
This can be achieved in a number of ways (the following examples assume `json` is a `String` containing JSON):

The type can be inferred from the context:
```kotlin
    val example: Example? = json.parseJSON()
```

The type may be specified as a type parameter:
```kotlin
    val example = json.parseJSON<Example>()
```

The type may be specified as a `KClass`:
```kotlin
    val example = json.parseJSON(Example::class)
```

The type may be specified as a `KType`:
```kotlin
    val example = json.parseJSON(Example::class.starProjectedType) as Example
```

(The last form is generally only needed when deserializing parameterized types and the parameter types can not be
inferred; the `as` expression is needed because `KType` does not convey inferred type information.)

## More Detail

The serialization and deserialization functions both operate as a two stage process.
Serialization first creates a structured form of the input data using the
[`jsonutil`](https://github.com/pwall567/jsonutil) library ([Javadoc](https://pwall.net/oss/jsonutil/)).
The resulting structure is then converted to string form as a second pass.

Deserialization does the same in reverse - the JSON string is first parsed into the internal form, and then that
structure is traversed to produce the target object.

This information is of significance when custom serialization and deserialization are required.
More information will be provided in due course, but in the meantime, examples of this form of use may be found in the
unit test classes.

## Dependency Specification

The latest version of the library is 0.9, and it may be found the the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>net.pwall.json</groupId>
      <artifactId>json-kotlin</artifactId>
      <version>0.9</version>
    </dependency>
```
### Gradle
```groovy
    implementation "net.pwall.json:json-kotlin:0.9"
```
### Gradle (kts)
```kotlin
    implementation("net.pwall.json:json-kotlin:0.9")
```

Peter Wall
2019-07-18
