/*
 * @(#) JSONSerializer.kt
 *
 * json-kotlin Kotlin JSON Auto Serialize/deserialize
 * Copyright (c) 2019, 2020 Peter Wall
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

import kotlin.math.abs
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.staticProperties
import kotlin.reflect.jvm.isAccessible

import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.net.URL
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
import java.util.Enumeration
import java.util.UUID

import net.pwall.util.Strings

/**
 * JSON Auto serialize for Kotlin.
 *
 * @author  Peter Wall
 */
object JSONSerializer {

    fun serialize(obj: Any?, config: JSONConfig = JSONConfig.defaultConfig): JSONValue? {

        if (obj == null)
            return null

        config.findToJSONMapping(obj::class)?.let { return it(obj) }

        when (obj) {

            is JSONValue -> return obj

            is CharSequence -> return JSONString(obj)

            is Char -> return JSONString(StringBuilder().append(obj))

            is Number -> return serializeNumber(obj, config)

            is Boolean -> return if (obj) JSONBoolean.TRUE else JSONBoolean.FALSE

            is CharArray -> return JSONString(StringBuilder().append(obj))

            is Array<*> -> return serializeArray(obj, config)

            is Pair<*, *> -> return serializePair(obj, config)

            is Triple<*, *, *> -> return serializeTriple(obj, config)
        }

        try {
            findToJSON(obj::class)?.let { return it.call(obj) }
        }
        catch (e: Exception) {
            throw JSONException("Error in custom toJSON - ${obj::class.simpleName}", e)
        }

        when (obj) {

            is Iterable<*> -> return JSONArray(obj.map { serialize(it, config) })

            is Iterator<*> -> return JSONArray().apply { obj.forEach { add(serialize(it, config)) } }

            is Sequence<*> -> return JSONArray().apply { obj.forEach { add(serialize(it, config)) } }

            is Enumeration<*> -> return JSONArray().apply { obj.forEach { add(serialize(it, config)) } }

            is Map<*, *> -> return serializeMap(obj, config)

            is Enum<*> -> return JSONString(obj.toString())

            is java.sql.Date,
            is java.sql.Time,
            is java.sql.Timestamp,
            is Instant,
            is LocalDate,
            is LocalDateTime,
            is OffsetTime,
            is OffsetDateTime,
            is ZonedDateTime,
            is Year,
            is YearMonth,
            is Duration,
            is Period,
            is URI,
            is URL,
            is UUID -> return JSONString(obj.toString())

            is Calendar -> return serializeCalendar(obj)

            is Date -> return serializeDate(obj)

            is BitSet -> return serializeBitSet(obj)

        }

        val result = JSONObject()
        val objClass = obj::class

        if (objClass.isSealedSubclass())
            result[config.sealedClassDiscriminator] = JSONString(objClass.simpleName ?: "null")

        val includeAll = config.hasIncludeAllPropertiesAnnotation(objClass.annotations)
        if (objClass.isData && objClass.constructors.isNotEmpty()) {
            // data classes will be a frequent use of serialization, so optimise for them
            val constructor = objClass.constructors.first()
            constructor.parameters.forEach { parameter ->
                val member = objClass.members.find { it.name == parameter.name }
                if (member is KProperty<*>)
                    result.addUsingGetter(member, parameter.annotations, obj, config, includeAll)
            }
            // now check whether there are any more properties not in constructor
            val statics: Collection<KProperty<*>> = objClass.staticProperties
            objClass.members.forEach { member ->
                if (member is KProperty<*> && !statics.contains(member) &&
                        !constructor.parameters.any { it.name == member.name })
                    result.addUsingGetter(member, member.annotations, obj, config, includeAll)
            }
        }
        else {
            val statics: Collection<KProperty<*>> = objClass.staticProperties
            objClass.members.forEach { member ->
                if (member is KProperty<*> && !statics.contains(member)) {
                    val combinedAnnotations = ArrayList(member.annotations)
                    objClass.constructors.firstOrNull()?.parameters?.find { it.name == member.name }?.let {
                        combinedAnnotations.addAll(it.annotations)
                    }
                    result.addUsingGetter(member, combinedAnnotations, obj, config, includeAll)
                }
            }
        }
        return result
    }

    internal fun KClass<*>.isSealedSubclass(): Boolean {
        supertypes.forEach { supertype ->
            (supertype.classifier as? KClass<*>)?.let {
                if (it.isSealed)
                    return true
                if (it != Any::class)
                    if (it.isSealedSubclass())
                        return true
            }
        }
        return false
    }

    private fun JSONObject.addUsingGetter(member: KProperty<*>, annotations: List<Annotation>?, obj: Any,
            config: JSONConfig, includeAll: Boolean) {
        if (!config.hasIgnoreAnnotation(annotations)) {
            val name = config.findNameFromAnnotation(annotations) ?: member.name
            val wasAccessible = member.isAccessible
            member.isAccessible = true
            try {
                val v = member.getter.call(obj)
                if (v != null || config.hasIncludeIfNullAnnotation(annotations) || config.includeNulls || includeAll)
                    put(name, serialize(v, config))
            }
            catch (e: JSONException) {
                throw e
            }
            catch (e: Exception) {
                throw JSONException("Error getting property ${member.name} from ${obj::class.simpleName}", e)
            }
            finally {
                member.isAccessible = wasAccessible
            }
        }
    }

    private fun serializeNumber(number: Number, config: JSONConfig): JSONValue = when (number) {

        is Int -> JSONInt(number)

        is Long -> JSONLong(number)

        is Short -> JSONInt(number.toInt())

        is Byte -> JSONInt(number.toInt())

        is Float -> JSONFloat(number)

        is BigInteger -> if (config.bigIntegerString) JSONString(number.toString()) else JSONLong(number.toLong())

        is BigDecimal -> if (config.bigDecimalString) JSONString(number.toString()) else JSONDecimal(number)

        else -> JSONDouble(number.toDouble())

    }

    private fun serializeArray(array: Array<*>, config: JSONConfig): JSONValue {

        if (array.isArrayOf<Char>())
            return JSONString(StringBuilder().apply { array.forEach { append(it) } })

        return JSONArray(array.map { serialize(it, config) })
    }

    private fun serializePair(pair: Pair<*, *>, config: JSONConfig): JSONArray {
        return JSONArray().apply {
            add(serialize(pair.first, config))
            add(serialize(pair.second, config))
        }
    }

    private fun serializeTriple(triple: Triple<*, *, *>, config: JSONConfig): JSONArray {
        return JSONArray().apply {
            add(serialize(triple.first, config))
            add(serialize(triple.second, config))
            add(serialize(triple.third, config))
        }
    }

    private fun serializeMap(map: Map<*, *>, config: JSONConfig): JSONObject {
        return JSONObject().apply {
            map.entries.forEach { entry ->
                this[entry.key.toString()] = serialize(entry.value, config)
            }
        }
    }

    private fun serializeCalendar(cal: Calendar) = JSONString(StringBuilder().apply {
        appendCalendar(cal)
    })

    internal fun Appendable.appendCalendar(cal: Calendar) {
        Strings.appendPositiveInt(this, cal.get(Calendar.YEAR))
        append('-')
        Strings.append2Digits(this, cal.get(Calendar.MONTH) + 1)
        append('-')
        Strings.append2Digits(this, cal.get(Calendar.DAY_OF_MONTH))
        append('T')
        Strings.append2Digits(this, cal.get(Calendar.HOUR_OF_DAY))
        append(':')
        Strings.append2Digits(this, cal.get(Calendar.MINUTE))
        append(':')
        Strings.append2Digits(this, cal.get(Calendar.SECOND))
        append('.')
        Strings.append3Digits(this, cal.get(Calendar.MILLISECOND))
        val offset = (cal.get(Calendar.ZONE_OFFSET) +
                if (cal.timeZone.inDaylightTime(cal.time)) cal.get(Calendar.DST_OFFSET) else 0) / 60_000
        if (offset == 0)
            append('Z')
        else {
            append(if (offset < 0) '-' else '+')
            abs(offset).let {
                Strings.append2Digits(this, it / 60)
                append(':')
                Strings.append2Digits(this, it % 60)
            }
        }
    }

    private fun serializeDate(date: Date): JSONString {
        val cal = Calendar.getInstance()
        cal.time = date
        return serializeCalendar(cal)
    }

    private fun serializeBitSet(bitSet: BitSet) = JSONArray(ArrayList<JSONValue>().apply {
            (0 until bitSet.length()).forEach { i -> if (bitSet.get(i)) add(JSONInt(i)) }
        })

    private val toJsonCache = HashMap<KClass<*>, KFunction<JSONValue>>()

    internal fun findToJSON(objClass: KClass<*>): KFunction<JSONValue>? {
        toJsonCache[objClass]?.let { return it }
        try {
            objClass.members.forEach { function ->
                if (function is KFunction<*> &&
                        function.name == "toJSON" &&
                        function.parameters.size == 1 &&
                        function.parameters[0].kind == KParameter.Kind.INSTANCE &&
                        function.returnType.isJSONValue())
                    @Suppress("UNCHECKED_CAST")
                    return (function as KFunction<JSONValue>).apply { toJsonCache[objClass] = this }
            }
        }
        catch (ignore: kotlin.reflect.jvm.internal.KotlinReflectionInternalError) {
            // seems like a bug in Kotlin runtime
        }
        catch (ignore: Exception) {
        }
        return null
    }

    private fun KType.isJSONValue(): Boolean {
        classifier?.let { if (it is KClass<*>) return it.isSubclassOf(JSONValue::class) }
        return false
    }

    private fun <T> Enumeration<T>.forEach(action: (T) -> Unit) {
        while (hasMoreElements())
            action(nextElement())
    }

}
