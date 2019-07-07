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
- `Array`, `IntArray`, `LongArray`, `ShortArray`, `ByteArray`, `DoubleArray`, `FloatArray`, `FloatArray`
- `Boolean`, `BooleanArray`
- `Collection`, `List`, `ArrayList`, `LinkedList`, `Set`, `HashSet`, `LinkedHashSet`, `Sequence`
- `Map`, `HashMap`, `LinkedHashMap`
- `Pair`, `Triple`
- `Enum`

Also, support is included for the following standard Java classes:

- `java.util.Date`, `java.util.Calendar`, `java.util.Enumeration`, `java.util.Bitset`, `java.util.UUID`
- `java.sql.Date`, `java.sql.Time`, `java.sql.Timestamp`
- `java.time.Instant`, `java.time.LocalDate`, `java.time.LocalDateTime`, `java.time.OffsetDateTime`,
  `java.time.OffsetTime`, `java.time.ZonedDateTime`, `java.time.Year`, `java.time.YearMonth`, `java.time.Duration`,
  `java.time.Period`

## Serialization

To serialize any object:
```kotlin
    val serialized = JSONSerializer.serialize(anyObject)
```

The returned value is an object that implements the `JSONValue` interface as described in the
[jsonutil](https://github.com/pwall567/jsonutil) library.
This object may then be output as a string by:
```kotlin
    val stringForm = serialized.toJSON()
```

Or, to combine the two operations into one:
```kotlin
    val stringForm = JSONAuto.stringify(anyObject)
```


## Deserialization

To deserialize any object:
```kotlin
    val newObject: ExpectedType = JSONDeserializer.deserialize(jsonValue)
```

Or, to specify the type dynamically (`expectedType` is a `Ktype`):
```kotlin
    val newObject = JSONDeserializer.deserialize(expectedType, anyObject)
```

In both cases, `jsonValue` is a `JSONValue` as above.
This object may be the result of parsing an input string:
```kotlin
    val jsonValue = JSON.parse(stringForm)
```

Again, the two operations may be combined into one:
```kotlin
    val newObject: ExpectedType = JSONAuto.parse(stringForm)
```

## Usage
###Maven
```xml
    <dependency>
      <groupId>net.pwall.json</groupId>
      <artifactId>json-kotlin</artifactId>
      <version>0.8</version>
    </dependency>
```
###Gradle
```groovy
    implementation "net.pwall.json:json-kotlin:0.8"
```
###Gradle (kts)
```kotlin
    implementation("net.pwall.json:json-kotlin:0.8")
```
