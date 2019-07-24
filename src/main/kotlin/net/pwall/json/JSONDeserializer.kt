/*
 * @(#) JSONDeserializer.kt
 *
 * json-kotlin Kotlin JSON Auto Serialize/deserialize
 * Copyright (c) 2019 Peter Wall
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

import kotlin.collections.ArrayList
import kotlin.jvm.internal.markers.KMappedMarker
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.isAccessible

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
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

import net.pwall.util.ISO8601Date

/**
 * JSON Auto deserialize for Kotlin.
 *
 * @author  Peter Wall
 */
object JSONDeserializer {

    /**
     * Deserialize a parsed [JSONValue] to a specified [KType].
     *
     * @param   resultType  the target type
     * @param   json        the parsed JSON, as a [JSONValue] (or `null`)
     * @param   config      an optional [JSONConfig]
     * @return              the converted object
     */
    fun deserialize(resultType: KType, json: JSONValue?, config: JSONConfig = JSONConfig.defaultConfig): Any? {
        config.getFromJSONMapping(resultType)?.let { return it(json) }
        if (json == null) {
            if (!resultType.isMarkedNullable)
                throw JSONException("Can't deserialize null as $resultType")
            return null
        }
        val classifier = resultType.classifier as? KClass<*> ?: throw JSONException("Can't deserialize $resultType")
        return deserialize(classifier, resultType.arguments, json, config)
    }

    /**
     * Deserialize a parsed [JSONValue] to a specified [KClass].
     *
     * @param   resultClass the target class
     * @param   json        the parsed JSON, as a [JSONValue] (or `null`)
     * @return              the converted object
     */
    fun <T: Any> deserialize(resultClass: KClass<T>, json: JSONValue?,
            config: JSONConfig = JSONConfig.defaultConfig): T? {
        if (json == null)
            return null
        return deserialize(resultClass, emptyList(), json, config)
    }

    /**
     * Deserialize a parsed [JSONValue] to a specified [KClass], where the result may not be `null`.
     *
     * @param   resultClass the target class
     * @param   json        the parsed JSON, as a [JSONValue] (or `null`)
     * @return              the converted object
     */
    fun <T: Any> deserializeNonNull(resultClass: KClass<T>, json: JSONValue?,
            config: JSONConfig = JSONConfig.defaultConfig): T {
        if (json == null)
            throw JSONException("Can't deserialize null as ${resultClass.simpleName}")
        return deserialize(resultClass, emptyList(), json, config)
    }

    /**
     * Deserialize a parsed [JSONValue] to a parameterized [KClass], with the specified [KTypeProjection]s.
     *
     * @param   resultClass the target class
     * @param   types       the [KTypeProjection]s
     * @param   json        the parsed JSON, as a [JSONValue] (or `null`)
     * @param   config      an optional [JSONConfig]
     * @return              the converted object
     */
    @Suppress("UNCHECKED_CAST")
    fun <T: Any> deserialize(resultClass: KClass<T>, types: List<KTypeProjection>, json: JSONValue,
            config: JSONConfig = JSONConfig.defaultConfig): T {

        // check for JSONValue

        if (resultClass.isSubclassOf(JSONValue::class) && resultClass.isSuperclassOf(json::class))
            return json as T

        // does the target class companion object have a "fromJSON()" method?

        try {
            findFromJSON(resultClass)?.let {
                return it.call(resultClass.companionObjectInstance, json) as T
            }
        }
        catch (e: Exception) {
            throw JSONException("Error in custom fromJSON - ${resultClass.simpleName}", e)
        }

        when (json) {

            is JSONBoolean -> {
                if (resultClass == Boolean::class)
                    return json.booleanValue() as T
            }

            is JSONString -> return deserializeString(resultClass, json.toString())

            is Number -> {

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

                }

                throw JSONException("Can't deserialize $json as $resultClass")
            }

            is JSONArray -> return deserializeArray(resultClass, types, json, config)

            is JSONObject -> return deserializeObject(resultClass, types, json, config)

        }

        throw JSONException("Can't deserialize $resultClass")
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> deserializeString(resultClass: KClass<T>, str: String): T {

        try {

            when (resultClass) {

                String::class -> return str as T

                Char::class -> {
                    if (str.length != 1)
                        throw JSONException("Character must be string of length 1")
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

                OffsetTime::class -> return OffsetTime.parse(str) as T

                OffsetDateTime::class -> return OffsetDateTime.parse(str) as T

                ZonedDateTime::class -> return ZonedDateTime.parse(str) as T

                Year::class -> return Year.parse(str) as T

                YearMonth::class -> return YearMonth.parse(str) as T

                Duration::class -> return Duration.parse(str) as T

                Period::class -> return Period.parse(str) as T

                UUID::class -> return UUID.fromString(str) as T

            }

        }
        catch (e: JSONException) {
            throw e
        }
        catch (e: Exception) {
            throw JSONException("Can't deserialize \"$str\" as $resultClass", e)
        }

        // is the target class an enum?

        if (resultClass.isSubclassOf(Enum::class))
            resultClass.staticFunctions.find { it.name == "valueOf" }?.let { return it.call(str) as T }

        // does the target class have a public constructor that takes String?
        // (e.g. StringBuilder, BigInteger, ... )

        resultClass.constructors.find { it.parameters.size == 1 && it.parameters[0].type.classifier == String::class }?.
                apply { return call(str) }

        throw JSONException("Can't deserialize \"$str\" as $resultClass")
    }

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    fun <T: Any> deserializeArray(resultClass: KClass<T>, types: List<KTypeProjection>, json: JSONArray,
            config: JSONConfig): T {
        try {

            return when (resultClass) {

                BooleanArray::class -> BooleanArray(json.size) { i -> deserializeNonNull(Boolean::class, json[i]) }

                ByteArray::class -> ByteArray(json.size) { i -> deserializeNonNull(Byte::class, json[i]) }

                CharArray::class -> CharArray(json.size) { i -> deserializeNonNull(Char::class, json[i]) }

                DoubleArray::class -> DoubleArray(json.size) { i -> deserializeNonNull(Double::class, json[i]) }

                FloatArray::class -> FloatArray(json.size) { i -> deserializeNonNull(Float::class, json[i]) }

                IntArray::class -> IntArray(json.size) { i -> deserializeNonNull(Int::class, json[i]) }

                LongArray::class -> LongArray(json.size) { i -> deserializeNonNull(Long::class, json[i]) }

                ShortArray::class -> ShortArray(json.size) { i -> deserializeNonNull(Short::class, json[i]) }

                ArrayList::class -> {
                    val type = types.firstOrNull()?.type ?: throw JSONException("Type not specified")
                    ArrayList<Any?>(json.size).apply {
                        json.forEach { add(deserialize(type, it, config)) }
                    }
                }

                LinkedList::class -> {
                    val type = types.firstOrNull()?.type ?: throw JSONException("Type not specified")
                    LinkedList<Any?>().apply {
                        json.forEach { add(deserialize(type, it, config)) }
                    }
                }

                List::class -> {
                    val type = types.firstOrNull()?.type ?: throw JSONException("Type not specified")
                    val result =  json.map { deserialize(type, it, config) }
                    if (isMutable(resultClass)) result.toMutableList() else result
                }

                HashSet::class -> {
                    val type = types.firstOrNull()?.type ?: throw JSONException("Type not specified")
                    HashSet<Any?>(json.size).apply {
                        json.forEach { add(deserialize(type, it, config)) }
                    }
                }

                LinkedHashSet::class -> {
                    val type = types.firstOrNull()?.type ?: throw JSONException("Type not specified")
                    LinkedHashSet<Any?>(json.size).apply {
                        json.forEach { add(deserialize(type, it, config)) }
                    }
                }

                Set::class -> {
                    val result = HashSet<Any?>(json.size)
                    val type = types.firstOrNull()?.type ?: throw JSONException("Type not specified")
                    json.forEach { result.add(deserialize(type, it, config)) }
                    if (isMutable(resultClass)) result.toMutableSet() else result.toSet()
                }

                Pair::class -> {
                    if (json.size != 2)
                        throw JSONException("Pair must have two members")
                    val type0 = types.firstOrNull()?.type ?: throw JSONException("First type not specified")
                    val type1 = types.getOrNull(1)?.type ?: throw JSONException("Second type not specified")
                    val result0 = deserialize(type0, json[0], config)
                    val result1 = deserialize(type1, json[1], config)
                    result0 to result1
                }

                Triple::class -> {
                    if (json.size != 3)
                        throw JSONException("Triple must have three members")
                    val type0 = types.firstOrNull()?.type ?: throw JSONException("First type not specified")
                    val type1 = types.getOrNull(1)?.type ?: throw JSONException("Second type not specified")
                    val type2 = types.getOrNull(2)?.type ?: throw JSONException("Third type not specified")
                    val result0 = deserialize(type0, json[0], config)
                    val result1 = deserialize(type1, json[1], config)
                    val result2 = deserialize(type2, json[2], config)
                    Triple(result0, result1, result2)
                }

                BitSet::class -> {
                    BitSet().apply {
                        json.forEach {
                            if (it !is JSONInt)
                                throw JSONException("Can't deserialize BitSet; array member not int")
                            set(it.get())
                        }
                    }
                }

                else -> throw JSONException("Can't deserialize array as $resultClass")

            } as T

        }
        catch (e: JSONException) {
            throw e
        }
        catch (e: Exception) {
            throw JSONException("Can't deserialize array as $resultClass", e)
        }

    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> deserializeObject(resultClass: KClass<T>, types: List<KTypeProjection>, json: JSONObject,
            config: JSONConfig): T {

        try {
            if (resultClass.isSubclassOf(Map::class)) {
                if (types.size != 2)
                    throw JSONException("Incorrect type arguments for Map")
                val keyClass = types[0].type?.classifier as? KClass<*> ?:
                        throw JSONException("Key type not specified for Map")
                val valueType = types[1].type ?: throw JSONException("Value type not specified for Map")
                val result =  HashMap<Any, Any?>()
                json.forEach { key: String ->
                    result[deserializeString(keyClass, key)] = deserialize(valueType, json[key], config)
                }
                return (if (isMutable(resultClass)) result.toMutableMap() else result.toMap()) as T
            }

            resultClass.objectInstance?.let { return setRemainingFields(resultClass, it, json, config) }

            findBestConstructor(resultClass.constructors, json, config)?.let { constructor ->
                val argMap = HashMap<KParameter, Any?>()
                val jsonCopy = HashMap<String, JSONValue?>(json)
                constructor.parameters.forEach { parameter ->
                    val paramName = findParameterName(parameter, config)
                    jsonCopy[paramName]?.let {
                        argMap[parameter] = deserialize(parameter.type, it, config)
                        jsonCopy.remove(paramName)
                    }
                }
                return setRemainingFields(resultClass, constructor.callBy(argMap), jsonCopy, config)
            }
        }
        catch (e: JSONException) {
            throw e
        }
        catch (e: Exception) {
            throw JSONException("Can't deserialize object as $resultClass", e)
        }

        throw JSONException("Can't deserialize object as $resultClass")

    }

    private fun <T: Any>  setRemainingFields(resultClass: KClass<T>, instance: T, json: Map<String, JSONValue?>,
            config: JSONConfig): T {
        json.forEach { entry -> // JSONObject fields not used in constructor
            val member = findField(resultClass.members, entry.key, config) ?:
                    throw JSONException("Can't find property ${entry.key} in ${resultClass.simpleName}")
            val value = deserialize(member.returnType, json[entry.key], config)
            if (member is KMutableProperty<*>) {
                val wasAccessible = member.isAccessible
                member.isAccessible = true
                try {
                    member.setter.call(instance, value)
                }
                catch (e: Exception) {
                    throw JSONException("Error setting property ${entry.key} in ${resultClass.simpleName}", e)
                }
                finally {
                    member.isAccessible = wasAccessible
                }
            }
            else {
                if (member.getter.call(instance) != value)
                    throw JSONException("Can't set property ${entry.key} in ${resultClass.simpleName}")
            }
        }
        return instance
    }

    private fun findField(members: Collection<KCallable<*>>, name: String, config: JSONConfig): KProperty<*>? {
        for (member in members) {
            if (member is KProperty<*> && (config.findNameFromAnnotation(member.annotations) ?: member.name) == name)
                    return member
        }
        return null
    }

    private fun <T: Any> findBestConstructor(constructors: Collection<KFunction<T>>, json: JSONObject,
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

    private fun findMatchingParameters(parameters: List<KParameter>, json: JSONObject, config: JSONConfig): Int {
        var n = 0
        for (parameter in parameters) {
            if (json.containsKey(findParameterName(parameter, config)))
                n++
            else {
                if (!parameter.isOptional)
                    return -1
            }
        }
        return n
    }

    private fun findParameterName(parameter: KParameter, config: JSONConfig): String? =
            config.findNameFromAnnotation(parameter.annotations) ?: parameter.name

    private val fromJsonCache = HashMap<KClass<*>, KFunction<*>>()

    private fun findFromJSON(resultClass: KClass<*>): KFunction<*>? {
        fromJsonCache[resultClass]?.let { return it }
        val newEntry = try {
            resultClass.companionObject?.functions?.find { function ->
                function.name == "fromJSON" &&
                        function.parameters.size == 2 &&
                        function.parameters[0].type.classifier == resultClass.companionObject &&
                        function.parameters[1].type.classifier == JSONValue::class &&
                        function.returnType.classifier == resultClass
            }
        }
        catch (e: Exception) {
            null
        }
        return newEntry?.apply { fromJsonCache[resultClass] = this }
    }

    private fun isMutable(resultClass: KClass<*>): Boolean = resultClass.isSubclassOf(KMappedMarker::class)
    // NOTE - KMappedMarker is a marker interface indicating mutability

    inline fun <reified T: Any> deserialize(json: JSONValue): T? = deserialize(T::class, json)

}
