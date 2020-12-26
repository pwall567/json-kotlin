# json-kotlin

[![Build Status](https://travis-ci.org/pwall567/json-kotlin.svg?branch=master)](https://travis-ci.org/pwall567/json-kotlin)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/static/v1?label=Kotlin&message=v1.4.0&color=blue&logo=kotlin)](https://github.com/JetBrains/kotlin/releases/tag/v1.4.0)
[![Maven Central](https://img.shields.io/maven-central/v/net.pwall.json/json-kotlin?label=Maven%20Central)](https://search.maven.org/search?q=g:%22net.pwall.json%22%20AND%20a:%22json-kotlin%22)

JSON serialization and deserialization for Kotlin

## Background

This library provides JSON serialization and deserialization functionality for Kotlin.
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
- `java.util.stream.Stream`, `java.util.stream.IntStream`, `java.util.stream.LongStream`,
  `java.util.stream.DoubleStream`

## Quick Start

### Serialization

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

### Deserialization

Deserialization is slightly more complicated, because the target data type must be specified to the function.
This can be achieved in a number of ways (the following examples assume `jsonString` is a `String` containing JSON):

The type can be inferred from the context:
```kotlin
    val example: Example? = jsonString.parseJSON()
```

The type may be specified as a type parameter:
```kotlin
    val example = jsonString.parseJSON<Example>()
```

The type may be specified as a `KClass`:
```kotlin
    val example = jsonString.parseJSON(Example::class)
```

The type may be specified as a `KType`:
```kotlin
    val example = jsonString.parseJSON(Example::class.starProjectedType) as Example
```
(This form is generally only needed when deserializing parameterized types where the parameter types can not be
inferred; the `as` expression is needed because `KType` does not convey inferred type information.)

Because of the limitations caused by [type erasure](https://kotlinlang.org/docs/reference/generics.html#type-erasure),
when deserializing parameterized types (like generic collections), the above forms will not always convey sufficient
information.
For these cases, the `JSONDelegate` class provides a valuable mechanism:
```kotlin
    val listExample: List<Example> by JSONDelegate(jsonList)
```
or:
```kotlin
    val listExample by JSONDelegate<List<Example>>(jsonList)
```

## Sealed Classes

The library will handle Kotlin sealed classes, by adding a discriminator property to the JSON object to allow the
deserialization to select the correct derived class.
The Kotlin documentation on sealed classes uses the following example:
```kotlin
    sealed class Expr
    data class Const(val number: Double) : Expr()
    data class Sum(val e1: Expr, val e2: Expr) : Expr()
    object NotANumber : Expr()
```

A `Const` instance from the example will serialize as:
```json
{"class":"Const","number":1.234}
```
and the `NotANumber` object will serialize as:
```json
{"class":"NotANumber"}
```
The discriminator property name (default "class") may be modified by setting the `sealedClassDiscriminator` property of
a `JSONConfig` object (see Customization below).

## Customization

### Annotations

#### Change the name used for a property

When serializing or deserializing a Kotlin object, the property name discovered by reflection will be used as the name
in the JSON object.
An alternative name may be specified if required, by the use of the `@JSONName` annotation:
```kotlin
    data class Example(val abc: String, @JSONName("xyz") val def: Int)
```

#### Ignore a property on serialization

If it is not necessary (or desirable) to output a particular field, the `@JSONIgnore` annotation may be used to prevent
serialisation:
```kotlin
    data class Example(val abc: String, @Ignore val def: Int)
```

#### Include properties when null

If a property is `null`, the default behaviour when serializing is to omit the property from the output object.
If this behaviour is not desired, the property may be annotated with the `@JSONIncludeIfNull` annotation to indicate
that it is to be included even if `null`:
```kotlin
    data class Example(@JSONIncludeIfNull val abc: String?, val def: Int)
```

To indicate that all properties in a class are to be included in the output even if null, the
`@JSONIncludeAllProperties` may be used on the class:
```kotlin
    @JSONIncludeAllProperties
    data class Example(val abc: String?, val def: Int)
```

And to specify that all properties in all classes are to be output if null, the `includeNulls` flag may be set in the
`JSONConfig`:
```kotlin
    val config = JSONConfig().apply {
        includeNulls = true
    }
    val json = example.stringifyJSON(config)
```

#### Allow extra properties in a class to be ignored

The default behaviour when extra properties are found during deserialization is to throw an exception.
To allow (and ignore) any extra properties, the `@JSONAllowExtra` annotation may be added to the class:
```kotlin
    @JSONAllowExtra
    data class Example(val abc: String, val def: Int)
```

To allow (and ignore) extra properties throughout the deserialization process, the `allowExtra` flag may be set in the
`JSONConfig`:
```kotlin
    val config = JSONConfig().apply {
        allowExtra = true
    }
    val json = example.stringifyJSON(config)
```

#### Using existing tags from other software

If you have classes that already contain annotations for the above purposes, you can tell `json-kotlin` to use
those annotations by specifying them in a `JSONConfig`:
```kotlin
    val config = JSONConfig().apply {
        addNameAnnotation(MyName::class, "name")
        addIgnoreAnnotation(MyIgnore::class)
        addIncludeIfNullAnnotation(MyIncludeIfNull::class)
        addIncludeAllPropertiesAnnotation(MyIncludeAllProperties::class)
        addAllowExtraPropertiesAnnotation(MyAllowExtraProperties::class)
    }
    val json = example.stringifyJSON(config)
```

The `JSONConfig` may be supplied as an optional final argument on most `json-kotlin` function calls (see the KDoc or
source for more details).

### Custom Serialization

The `JSONConfig` is also used to specify custom serialization:
```kotlin
    val config = JSONConfig().apply {
        toJSON<Example> { obj ->
            obj?.let {
                JSONObject().apply {
                    putValue("custom1", it.abc)
                    putValue("custom2", it.def)
                }
            }
        }
    }
```
Or deserialization:
```kotlin
    val config = JSONConfig().apply {
        fromJSON { json ->
            require(json is JSONObject) { "Must be JSONObject" }
            Example(json.getString("custom1"), json.getInt("custom2"))
        }
    }
```

The `toJSON` function must supply a lambda will the signature `(Any?) -> JSONValue?` and the `fromJSON` function must
supply a lambda with the signature `(JSONValue?) -> Any?`.
`JSONValue` is the interface implemented by each node in the `jsonutil` library (see below).

Both `toJSON` and `fromJSON` may be specified repeatedly in the same `JSONConfig` to cover multiple classes.

## Mixed Kotlin and Java

If you need to serialize or deserialize a Kotlin class from Java, the `JSONJava` class provides this capability while
still retaining all the Kotlin functionality, like Kotlin-specific classes and nullability checking:
```
    String json = JSONJava.stringify(example);
```
Or:
```
    Example example = JSONJava.parse(Example.class, json);
```

## More Detail

The deserialization functions operate as a two-stage process.
The JSON string is first parsed into an internal form using the
[`jsonutil`](https://github.com/pwall567/jsonutil) library ([Javadoc](https://pwall.net/oss/jsonutil/));
the resulting tree of `JSONValue` objects is then traversed to create the desired classes.

(Note that the
[`json-stream`](https://github.com/pwall567/json-stream) library and its non-blocking coroutine-aware version
[`json-co-stream`](https://github.com/pwall567/json-co-stream) may be used to parse a stream of JSON data on-the-fly,
and in this case, each object may be converted to the required target form as its last character is read.)

It is possible to perform serialization using the same two-stage approach, but from version 3.2 of this library onwards,
the `JSONStringify` functions are used to stringify direct to a string, or, if the `appendJSON()` function is used, to
any form of `Appendable` including the various `Writer` classes.
As always, the KDoc, the source or the unit test classes provide more information.

This information is of significance when custom serialization and deserialization are required.
Regardless of whether the `JSONStringify` functions are used to output directly to a string, the custom serialization
is still required to create the internal `JSONValue`-based form.
This ensures that errant serialization functions don't disrupt the remainder of the JSON, for example by omitting a
trailing quote or bracket character.

## Dependency Specification

The latest version of the library is 3.14, and it may be obtained from the Maven Central repository.

### Maven
```xml
    <dependency>
      <groupId>net.pwall.json</groupId>
      <artifactId>json-kotlin</artifactId>
      <version>3.14</version>
    </dependency>
```
### Gradle
```groovy
    implementation 'net.pwall.json:json-kotlin:3.14'
```
### Gradle (kts)
```kotlin
    implementation("net.pwall.json:json-kotlin:3.14")
```

## Breaking change

Version 2.0 introduced a change to `JSONConfig` which makes it incompatible with earlier versions - the functions to add
information to the `JSONConfig` object (e.g. serialization and deserialization mappings) no longer return the object
itself for chaining purposes.
The recommended approach to perform repeat actions such as this is to use the Kotlin `apply {}` function.

If there is anyone affected by this change (unlikely, I know!) version 1.2 is still available.

Also, version 3.8 changed the visibility of an internal function in `JSONDeserializer` from `public` to `private`
(and changed its signature, although that's hardly relevant if it's no longer public).
It was never intended that this function would be part of the published API, so the "major version" has not been
incremented as would normally be the case for a breaking change.

Peter Wall

2020-11-30
