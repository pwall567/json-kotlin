/*
 * @(#) JSONDeserializer.kt
 *
 * json-kotlin Kotlin JSON Auto Serialize/deserialize
 * Copyright (c) 2019, 2020, 2021 Peter Wall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pwall.json

import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.MonthDay
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Period
import java.time.Year
import java.time.YearMonth
import java.time.ZonedDateTime
import java.util.BitSet
import java.util.Calendar
import java.util.Date
import java.util.LinkedList
import java.util.UUID
import java.util.stream.DoubleStream
import java.util.stream.IntStream
import java.util.stream.LongStream
import java.util.stream.Stream

import net.pwall.json.JSONDeserializerFunctions.createUUID
import net.pwall.json.JSONDeserializerFunctions.findFromJSON
import net.pwall.json.JSONDeserializerFunctions.findParameterName
import net.pwall.json.JSONDeserializerFunctions.hasSingleParameter
import net.pwall.json.JSONKotlinException.Companion.fail
import net.pwall.json.pointer.JSONPointer
import net.pwall.util.ISO8601Date

/**
 * JSON Auto deserialize for Kotlin.
 *
 * @author  Peter Wall
 */
@OptIn(ExperimentalStdlibApi::class)
object JSONDeserializer {

    private val anyQType = Any::class.createType(emptyList(), true)

    /**
     * Deserialize a parsed [JSONValue] to a specified [KType].
     *
     * @param   resultType  the target type
     * @param   json        the parsed JSON, as a [JSONValue] (or `null`)
     * @param   config      an optional [JSONConfig]
     * @return              the converted object
     */
    fun deserialize(resultType: KType, json: JSONValue?, config: JSONConfig = JSONConfig.defaultConfig): Any? =
            deserialize(resultType, json, JSONPointer.root, config)

    /**
     * Deserialize a parsed [JSONValue] to a specified [KType] (specifying [JSONPointer]).
     *
     * @param   resultType  the target type
     * @param   json        the parsed JSON, as a [JSONValue] (or `null`)
     * @param   pointer     the [JSONPointer]
     * @param   config      an optional [JSONConfig]
     * @return              the converted object
     */
    fun deserialize(resultType: KType, json: JSONValue?, pointer: JSONPointer,
            config: JSONConfig = JSONConfig.defaultConfig): Any? {
        config.findFromJSONMapping(resultType)?.let {
            try {
                return it(json)
            }
            catch (e: Exception) {
                fail("Error in custom fromJSON", pointer, e)
            }
        }
        if (json == null) {
            if (!resultType.isMarkedNullable)
                fail("Can't deserialize null as ${resultType.simpleName}", pointer)
            return null
        }
        val classifier = resultType.classifier as? KClass<*> ?:
                fail("Can't deserialize ${resultType.simpleName}", pointer)
        return deserialize(resultType, classifier, resultType.arguments, json, pointer, config)
    }

    /**
     * Deserialize a parsed [JSONValue] to a specified [KClass].
     *
     * @param   resultClass the target class
     * @param   json        the parsed JSON, as a [JSONValue] (or `null`)
     * @param   config      an optional [JSONConfig]
     * @param   T           the target class
     * @return              the converted object
     */
    @Suppress("UNCHECKED_CAST")
    fun <T: Any> deserialize(resultClass: KClass<T>, json: JSONValue?,
            config: JSONConfig = JSONConfig.defaultConfig): T? {
        config.findFromJSONMapping(resultClass)?.let { return it(json) as T }
        if (json == null)
            return null
        return deserialize(resultClass.starProjectedType, resultClass, emptyList(), json, JSONPointer.root, config)
    }

    /**
     * Deserialize a parsed [JSONValue] to a specified [KClass], where the result may not be `null`.
     *
     * @param   resultClass the target class
     * @param   json        the parsed JSON, as a [JSONValue]
     * @param   config      an optional [JSONConfig]
     * @param   T           the target class
     * @return              the converted object
     */
    fun <T: Any> deserializeNonNull(resultClass: KClass<T>, json: JSONValue?,
            config: JSONConfig = JSONConfig.defaultConfig): T =
                    deserializeNonNull(resultClass, json, JSONPointer.root, config)

    /**
     * Deserialize a parsed [JSONValue] to a specified [KClass], where the result may not be `null` (specifying
     * [JSONPointer]).
     *
     * @param   resultClass the target class
     * @param   json        the parsed JSON, as a [JSONValue]
     * @param   pointer     the [JSONPointer]
     * @param   config      an optional [JSONConfig]
     * @param   T           the target class
     * @return              the converted object
     */
    @Suppress("UNCHECKED_CAST")
    fun <T: Any> deserializeNonNull(resultClass: KClass<T>, json: JSONValue?, pointer: JSONPointer,
            config: JSONConfig = JSONConfig.defaultConfig): T {
        config.findFromJSONMapping(resultClass)?.let { return it(json) as T }
        if (json == null)
            fail("Can't deserialize null as ${resultClass.simpleName}")
        return deserialize(resultClass.starProjectedType, resultClass, emptyList(), json, pointer, config)
    }

    /**
     * Deserialize a parsed [JSONValue] to a specified Java [Type].
     *
     * @param   javaType    the target type
     * @param   json        the parsed JSON, as a [JSONValue] (or `null`)
     * @param   config      an optional [JSONConfig]
     * @return              the converted object
     */
    fun deserialize(javaType: Type, json: JSONValue?, config: JSONConfig = JSONConfig.defaultConfig): Any? =
            deserialize(javaType.toKType(nullable = true), json, config)

    /**
     * Deserialize a parsed [JSONValue] to an unspecified([Any]) type.  Strings will be converted to `String`, numbers
     * to `Int`, `Long` or `BigDecimal`, booleans to `Boolean`, arrays to `ArrayList<Any?>` and objects to
     * `ListMap<String, Any?>` (a simple implementation of `Map` that preserves order).
     *
     * @param   json        the parsed JSON, as a [JSONValue] (or `null`)
     * @param   config      an optional [JSONConfig] (not used, accepted for compatibility with other functions)
     * @return              the converted object
     */
    fun deserializeAny(json: JSONValue?, @Suppress("UNUSED_PARAMETER") config: JSONConfig? = null): Any? =
            json?.toSimpleValue()

    /**
     * Deserialize a parsed [JSONValue] to the inferred [KType].
     *
     * @param   json        the parsed JSON, as a [JSONValue] (or `null`)
     * @param   config      an optional [JSONConfig]
     * @param   T           the target class
     * @return              the converted object
     */
    inline fun <reified T: Any> deserialize(json: JSONValue?, config: JSONConfig = JSONConfig.defaultConfig): T? =
            deserialize(typeOf<T>(), json, config) as T?

    /**
     * Deserialize a parsed [JSONValue] to a parameterized [KClass], with the specified [KTypeProjection]s.
     *
     * @param   resultType  the target [KType]
     * @param   resultClass the target class
     * @param   types       the [KTypeProjection]s
     * @param   json        the parsed JSON, as a [JSONValue] (or `null`)
     * @param   pointer     a [JSONPointer] to the current location
     * @param   config      a [JSONConfig]
     * @param   T           the target class
     * @return              the converted object
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> deserialize(resultType: KType, resultClass: KClass<T>, types: List<KTypeProjection>,
            json: JSONValue, pointer: JSONPointer, config: JSONConfig): T {

        // check for JSONValue

        if (resultClass.isSubclassOf(JSONValue::class) && resultClass.isSuperclassOf(json::class))
            return json as T

        // does the target class companion object have a "fromJSON()" method?

        try {
            findFromJSON(resultClass, json::class)?.let {
                return it.call(resultClass.companionObjectInstance, json) as T
            }
        }
        catch (e: Exception) {
            fail("Error in custom in-class fromJSON - ${resultClass.simpleName}", pointer, e)
        }

        when (json) {

            is JSONBoolean -> {
                if (resultClass.isSuperclassOf(Boolean::class))
                    return json.booleanValue() as T
            }

            is JSONString -> return deserializeString(resultClass, json.toString(), pointer)

            is JSONNumberValue -> {

                when (resultClass) {

                    Int::class -> if (json is JSONInt || json is JSONZero)
                        return json.toInt() as T

                    Long::class -> if (json is JSONLong || json is JSONInt || json is JSONZero)
                        return json.toLong() as T

                    Double::class -> return json.toDouble() as T

                    Float::class -> return json.toFloat() as T

                    Short::class -> if (json is JSONInt || json is JSONZero)
                        return json.toShort() as T

                    Byte::class -> if (json is JSONInt || json is JSONZero)
                        return json.toByte() as T

                    BigInteger::class -> if (json is JSONLong || json is JSONInt || json is JSONZero)
                        return json.toBigInteger() as T

                    BigDecimal::class -> return json.toBigDecimal() as T

                }

                if (resultClass.isSuperclassOf(Number::class)) {
                    when (json) {
                        is JSONInt,
                        is JSONZero -> return json.toInt() as T
                        is JSONLong -> return json.toLong() as T
                        is JSONFloat -> return json.toFloat() as T
                        is JSONDouble -> return json.toDouble() as T
                        is JSONDecimal -> return json.toBigDecimal() as T
                    }
                }

                fail("Can't deserialize $json as ${resultClass.simpleName}", pointer)
            }

            is JSONSequence<*> -> return deserializeArray(resultType, resultClass, types, json, pointer, config)

            is JSONMapping<*> -> return deserializeObject(resultType, resultClass, types, json, pointer, config)

        }

        fail("Can't deserialize ${resultClass.simpleName}", pointer)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> deserializeString(resultClass: KClass<T>, str: String, pointer: JSONPointer): T {

        try {

            if (resultClass.isSuperclassOf(String::class))
                return str as T

            when (resultClass) {

                Char::class -> {
                    if (str.length != 1)
                        fail("Character must be string of length 1", pointer)
                    return str[0] as T
                }

                CharArray::class -> return str.toCharArray() as T

                Array<Char>::class -> return Array(str.length) { i -> str[i] } as T

                java.sql.Date::class -> return java.sql.Date.valueOf(str) as T

                java.sql.Time::class -> return java.sql.Time.valueOf(str) as T

                java.sql.Timestamp::class -> return java.sql.Timestamp.valueOf(str) as T

                Calendar::class -> return ISO8601Date.decode(str) as T

                Date::class -> return ISO8601Date.decode(str).time as T

                Instant::class -> return Instant.parse(str) as T

                LocalDate::class -> return LocalDate.parse(str) as T

                LocalDateTime::class -> return LocalDateTime.parse(str) as T

                LocalTime::class -> return LocalTime.parse(str) as T

                OffsetTime::class -> return OffsetTime.parse(str) as T

                OffsetDateTime::class -> return OffsetDateTime.parse(str) as T

                ZonedDateTime::class -> return ZonedDateTime.parse(str) as T

                Year::class -> return Year.parse(str) as T

                YearMonth::class -> return YearMonth.parse(str) as T

                MonthDay::class -> return MonthDay.parse(str) as T

                Duration::class -> return Duration.parse(str) as T

                Period::class -> return Period.parse(str) as T

                UUID::class -> return createUUID(str) as T

            }

            // is the target class an enum?

            if (resultClass.isSubclassOf(Enum::class))
                resultClass.staticFunctions.find { it.name == "valueOf" }?.let { return it.call(str) as T }

            // does the target class have a public constructor that takes String? (e.g. StringBuilder, URL, ... )

            resultClass.constructors.find { it.hasSingleParameter(String::class) }?.apply { return call(str) }

        }
        catch (e: JSONException) {
            throw e
        }
        catch (e: Exception) {
            fail("Error deserializing \"$str\" as ${resultClass.simpleName}", pointer, e)
        }

        fail("Can't deserialize \"$str\" as ${resultClass.simpleName}", pointer)
    }

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    private fun <T: Any> deserializeArray(resultType: KType, resultClass: KClass<T>, types: List<KTypeProjection>,
            json: JSONSequence<*>, pointer: JSONPointer, config: JSONConfig): T {
        return when (resultClass) {

            BooleanArray::class -> BooleanArray(json.size) {
                i -> deserializeNonNull(Boolean::class, json[i], pointer.child(i))
            }

            ByteArray::class -> ByteArray(json.size) {
                i -> deserializeNonNull(Byte::class, json[i], pointer.child(i))
            }

            CharArray::class -> CharArray(json.size) {
                i -> deserializeNonNull(Char::class, json[i], pointer.child(i))
            }

            DoubleArray::class -> DoubleArray(json.size) {
                i -> deserializeNonNull(Double::class, json[i], pointer.child(i))
            }

            FloatArray::class -> FloatArray(json.size) {
                i -> deserializeNonNull(Float::class, json[i], pointer.child(i))
            }

            IntArray::class -> IntArray(json.size) {
                i -> deserializeNonNull(Int::class, json[i], pointer.child(i))
            }

            LongArray::class -> LongArray(json.size) {
                i -> deserializeNonNull(Long::class, json[i], pointer.child(i))
            }

            ShortArray::class -> ShortArray(json.size) {
                i -> deserializeNonNull(Short::class, json[i], pointer.child(i))
            }

            Collection::class,
            MutableCollection::class,
            List::class,
            MutableList::class,
            Iterable::class,
            Any::class,
            ArrayList::class-> ArrayList<Any?>(json.size).fillFromJSON(resultType, json, getTypeParam(types), pointer,
                    config)

            LinkedList::class -> LinkedList<Any?>().fillFromJSON(resultType, json, getTypeParam(types), pointer, config)

            Stream::class -> {
                val list = ArrayList<Any?>(json.size).fillFromJSON(resultType, json, getTypeParam(types), pointer,
                        config)
                list.stream()
            }

            IntStream::class -> {
                val intArray = IntArray(json.size) {
                    i -> deserializeNonNull(Int::class, json[i], pointer.child(i), config)
                }
                IntStream.of(*intArray)
            }

            LongStream::class -> {
                val longArray = LongArray(json.size) {
                    i -> deserializeNonNull(Long::class, json[i], pointer.child(i), config)
                }
                LongStream.of(*longArray)
            }

            DoubleStream::class -> {
                val doubleArray = DoubleArray(json.size) {
                    i -> deserializeNonNull(Double::class, json[i], pointer.child(i), config)
                }
                DoubleStream.of(*doubleArray)
            }

            Set::class,
            MutableSet::class,
            LinkedHashSet::class -> LinkedHashSet<Any?>(json.size).fillFromJSON(resultType, json, getTypeParam(types),
                    pointer, config)

            HashSet::class -> HashSet<Any?>(json.size).fillFromJSON(resultType, json, getTypeParam(types), pointer,
                    config)

            Sequence::class -> json.mapIndexed { i, value ->
                deserializeNested(resultType, getTypeParam(types), value, pointer.child(i), config)
            }.asSequence()

            Pair::class -> {
                val result0 = deserializeNested(resultType, getTypeParam(types, 0), json[0], pointer.child(0), config)
                val result1 = deserializeNested(resultType, getTypeParam(types, 1), json[1], pointer.child(1), config)
                result0 to result1
            }

            Triple::class -> {
                val result0 = deserializeNested(resultType, getTypeParam(types, 0), json[0], pointer.child(0), config)
                val result1 = deserializeNested(resultType, getTypeParam(types, 1), json[1], pointer.child(1), config)
                val result2 = deserializeNested(resultType, getTypeParam(types, 2), json[2], pointer.child(2), config)
                Triple(result0, result1, result2)
            }

            BitSet::class -> {
                BitSet().apply {
                    json.mapIndexed { i, value ->
                        if (value !is JSONInt)
                            fail("Can't deserialize BitSet; array member not int", pointer.child(i))
                        set(value.value)
                    }
                }
            }

            else -> {
                if (resultClass.java.isArray) {
                    val type = getTypeParam(types)
                    val itemClass = type.classifier as? KClass<Any> ?: fail("Can't determine array type", pointer)
                    newArray(itemClass, json.size).apply {
                        for (i in json.indices)
                            this[i] = deserializeNested(resultType, type, json[i], pointer.child(i), config)
                    }
                }
                else {

                    // If the target class has a constructor that takes a single List parameter, create a List and
                    // invoke that constructor.  This should catch the less frequently used List classes.

                    resultClass.constructors.find { it.hasSingleParameter(List::class) }?.run {
                        val type = getTypeParam(parameters[0].type.arguments)
                        call(ArrayList<Any?>(json.size).fillFromJSON(resultType, json, type, pointer, config))
                    } ?: fail("Can't deserialize array as ${resultClass.simpleName}", pointer)
                }
            }

        } as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> newArray(itemClass: KClass<T>, size: Int): Array<T?> =
            // there appears to be no way of creating an array of dynamic type in Kotlin
            // other than to use Java reflection
            java.lang.reflect.Array.newInstance(itemClass.java, size) as Array<T?>

    private fun getTypeParam(types: List<KTypeProjection>, n: Int = 0): KType = types.getOrNull(n)?.type ?: anyQType

    private fun MutableCollection<Any?>.fillFromJSON(resultType: KType, json: JSONSequence<*>, type: KType,
            pointer: JSONPointer, config: JSONConfig): MutableCollection<Any?> {
        // using for rather than map to avoid creation of intermediate List
        for (i in json.indices) {
            if (!add(deserializeNested(resultType, type, json[i], pointer.child(i), config)))
                fail("Duplicate not allowed", pointer.child(i))
        }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> deserializeObject(resultType: KType, resultClass: KClass<T>, types: List<KTypeProjection>,
            json: JSONMapping<*>, pointer: JSONPointer, config: JSONConfig): T {
        if (resultClass.isSubclassOf(Map::class)) {
            when (resultClass) {
                HashMap::class -> return deserializeMap(resultType, HashMap(json.size), types, json, pointer,
                        config) as T
                Map::class,
                MutableMap::class,
                LinkedHashMap::class -> return deserializeMap(resultType, LinkedHashMap(json.size), types, json,
                        pointer, config) as T
            }
        }

        // If the target class has a constructor that takes a single Map parameter, create a Map and invoke that
        // constructor.  This should catch the less frequently used Map classes.

        resultClass.constructors.find { it.hasSingleParameter(Map::class) }?.apply {
            return call(deserializeMap(resultType, LinkedHashMap(json.size), parameters[0].type.arguments, json,
                    pointer, config))
        }

        val jsonCopy = JSONMapping<JSONValue>(json)

        if (resultClass.isSealed) {
            val subClassName = (jsonCopy.remove(config.sealedClassDiscriminator) as? JSONString)?.toString() ?:
            fail("No class name for sealed class", pointer)
            val subClass = resultClass.sealedSubclasses.find { it.simpleName == subClassName } ?:
            fail("Can't find named subclass for sealed class", pointer)
            return deserializeObject(subClass.createType(types, nullable = resultType.isMarkedNullable), subClass,
                    types, jsonCopy, pointer, config)
        }

        resultClass.objectInstance?.let {
            return setRemainingFields(resultType, resultClass, it, json, pointer, config)
        }

        if (resultClass.isSuperclassOf(Map::class))
            return deserializeMap(resultType, LinkedHashMap(json.size), types, json, pointer, config) as T

        findBestConstructor(resultClass.constructors, json, config)?.let { constructor ->
            val argMap = HashMap<KParameter, Any?>()
            for (parameter in constructor.parameters) {
                val paramName = findParameterName(parameter, config)
                if (!config.hasIgnoreAnnotation(parameter.annotations)) {
                    if (jsonCopy.containsKey(paramName)) {
                        argMap[parameter] = deserializeNested(resultType, parameter.type, jsonCopy[paramName],
                                pointer.child(paramName), config)
                    }
                    else {
                        if (!parameter.isOptional) {
                            if (parameter.type.isMarkedNullable)
                                argMap[parameter] = null
                            else
                                fail("Can't create $resultClass - missing property $paramName", pointer)
                        }
                    }
                }
                jsonCopy.remove(paramName)
            }
            return setRemainingFields(resultType, resultClass, constructor.callBy(argMap), jsonCopy, pointer, config)
        }
        // there is no matching constructor
        if (resultClass.constructors.size == 1) {
            val missing = resultClass.constructors.first().parameters.filter {
                !config.hasIgnoreAnnotation(it.annotations) && !it.isOptional && !it.type.isMarkedNullable
            }.map {
                findParameterName(it, config)
            }.filter {
                !jsonCopy.containsKey(it)
            }
            fail("Can't create ${resultClass.simpleName}; missing: ${missing.displayList()}", pointer)
        }
        val propMessage = when {
            jsonCopy.isNotEmpty() -> jsonCopy.keys.displayList()
            else -> "none"
        }
        fail("Can't locate constructor for ${resultClass.simpleName}; properties: $propMessage", pointer)
    }

    private fun Collection<Any?>.displayList(): String = joinToString(", ")

    private fun deserializeMap(resultType: KType, map: MutableMap<Any, Any?>, types: List<KTypeProjection>,
            json: JSONMapping<*>, pointer: JSONPointer, config: JSONConfig): MutableMap<Any, Any?> {
        val keyClass = getTypeParam(types, 0).classifier as? KClass<*> ?:
                fail("Key type can not be determined for Map", pointer)
        val valueType = getTypeParam(types, 1)
        for (entry in json.entries) {
            map[deserializeString(keyClass, entry.key, pointer)] = deserializeNested(resultType, valueType, entry.value,
                    pointer.child(entry.key), config)
        }
        return map
    }

    private fun <T: Any> setRemainingFields(resultType: KType, resultClass: KClass<T>, instance: T,
            json: Map<String, JSONValue?>, pointer: JSONPointer, config: JSONConfig): T {
        for (entry in json) { // JSONObject fields not used in constructor
            val member = findField(resultClass.members, entry.key, config)
            if (member != null) {
                if (!config.hasIgnoreAnnotation(member.annotations)) {
                    val value = deserializeNested(resultType, member.returnType, entry.value, pointer.child(entry.key),
                            config)
                    if (member is KMutableProperty<*>) {
                        val wasAccessible = member.isAccessible
                        member.isAccessible = true
                        try {
                            member.setter.call(instance, value)
                        }
                        catch (e: Exception) {
                            fail("Error setting property ${entry.key} in ${resultClass.simpleName}", pointer, e)
                        }
                        finally {
                            member.isAccessible = wasAccessible
                        }
                    }
                    else {
                        if (member.getter.call(instance) != value)
                            fail("Can't set property ${entry.key} in ${resultClass.simpleName}", pointer)
                    }
                }
            }
            else {
                if (!(config.allowExtra || config.hasAllowExtraPropertiesAnnotation(resultClass.annotations)))
                    fail("Can't find property ${entry.key} in ${resultClass.simpleName}", pointer)
            }
        }
        return instance
    }

    private fun findField(members: Collection<KCallable<*>>, name: String, config: JSONConfig): KProperty<*>? {
        for (member in members)
            if (member is KProperty<*> && (config.findNameFromAnnotation(member.annotations) ?: member.name) == name)
                    return member
        return null
    }

    private fun <T: Any> findBestConstructor(constructors: Collection<KFunction<T>>, json: JSONMapping<*>,
            config: JSONConfig): KFunction<T>? {
        var result: KFunction<T>? = null
        var best = -1
        for (constructor in constructors) {
            val parameters = constructor.parameters
            if (parameters.any { findParameterName(it, config) == null || it.kind != KParameter.Kind.VALUE })
                continue
            val n = findMatchingParameters(parameters, json, config)
            if (n > best) {
                result = constructor
                best = n
            }
        }
        return result
    }

    private fun findMatchingParameters(parameters: List<KParameter>, json: JSONMapping<*>, config: JSONConfig): Int {
        var n = 0
        for (parameter in parameters) {
            if (json.containsKey(findParameterName(parameter, config)))
                n++
            else {
                if (!(parameter.isOptional || parameter.type.isMarkedNullable))
                    return -1
            }
        }
        return n
    }

    private fun deserializeNested(enclosingType: KType, resultType: KType, json: JSONValue?, pointer: JSONPointer,
            config: JSONConfig): Any? =
                    deserialize(resultType.applyTypeParameters(enclosingType, pointer), json, pointer, config)

    private fun KType.applyTypeParameters(enclosingType: KType, pointer: JSONPointer): KType {
        (classifier as? KTypeParameter)?.let { typeParameter ->
            val enclosingClass = enclosingType.classifierAsClass(this, pointer)
            val index = enclosingClass.typeParameters.indexOfFirst { it.name == typeParameter.name }
            return enclosingType.arguments.getOrNull(index)?.type ?:
                    enclosingClass.typeParameters.getOrNull(index)?.upperBounds?.singleOrNull() ?:
                    fail("Can't create $simpleName - no type information for ${typeParameter.name}", pointer)
        }

        if (arguments.isEmpty())
            return this

        return classifierAsClass(this, pointer).createType(arguments.map { (variance, type) ->
            if (variance == null || type == null)
                KTypeProjection.STAR
            else
                type.applyTypeParameters(enclosingType, pointer).let {
                    when (variance) {
                        KVariance.INVARIANT -> KTypeProjection.invariant(it)
                        KVariance.IN -> KTypeProjection.contravariant(it)
                        KVariance.OUT -> KTypeProjection.covariant(it)
                    }
                }
        }, isMarkedNullable, annotations)
    }

    private fun KType.classifierAsClass(target: KType, pointer: JSONPointer): KClass<*> =
            classifier as? KClass<*> ?: fail("Can't create $target - insufficient type information", pointer)

    private val KType.simpleName
        get() = (classifier as? KClass<*>)?.simpleName ?: toString()

}
