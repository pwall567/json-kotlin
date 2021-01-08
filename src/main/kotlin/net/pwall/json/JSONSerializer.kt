/*
 * @(#) JSONSerializer.kt
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

import kotlin.reflect.KProperty
import kotlin.reflect.full.staticProperties
import kotlin.reflect.jvm.isAccessible

import java.math.BigDecimal
import java.math.BigInteger
import java.util.BitSet
import java.util.Calendar
import java.util.Date
import java.util.Enumeration
import java.util.stream.BaseStream

import net.pwall.json.JSONKotlinException.Companion.fail
import net.pwall.json.JSONSerializerFunctions.findToJSON
import net.pwall.json.JSONSerializerFunctions.formatISO8601
import net.pwall.json.JSONSerializerFunctions.isSealedSubclass
import net.pwall.json.JSONSerializerFunctions.isToStringClass

/**
 * JSON Auto serialize for Kotlin.
 *
 * @author  Peter Wall
 */
object JSONSerializer {

    fun serialize(obj: Any?, config: JSONConfig = JSONConfig.defaultConfig): JSONValue? =
            serialize(obj, config, mutableSetOf())

    private fun serialize(obj: Any?, config: JSONConfig, references: MutableSet<Any>): JSONValue? {

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

            is Array<*> -> return serializeArray(obj, config, references)

            is Pair<*, *> -> return serializePair(obj, config, references)

            is Triple<*, *, *> -> return serializeTriple(obj, config, references)
        }

        val objClass = obj::class

        if (objClass.isToStringClass() || obj is Enum<*>)
            return JSONString(obj.toString())

        try {
            objClass.findToJSON()?.let { return it.call(obj) }
        }
        catch (e: Exception) {
            fail("Error in custom toJSON - ${objClass.simpleName}", e)
        }

        when (obj) {

            is Iterable<*> -> return JSONArray(obj.map { serialize(it, config, references) })

            is Iterator<*> -> return JSONArray().apply {
                for (item in obj)
                    add(serialize(item, config, references))
            }

            is BaseStream<*, *> -> return JSONArray().apply {
                for (item in obj)
                    add(serialize(item, config, references))
            }

            is Sequence<*> -> return JSONArray().apply {
                for (item in obj)
                    add(serialize(item, config, references))
            }

            is Enumeration<*> -> return JSONArray().apply {
                for (item in obj)
                    add(serialize(item, config, references))
            }

            is Map<*, *> -> return serializeMap(obj, config, references)

            is Calendar -> return JSONString(obj.formatISO8601())

            is Date -> return JSONString((Calendar.getInstance().apply { time = obj }).formatISO8601())

            is BitSet -> return serializeBitSet(obj)

        }

        val result = JSONObject()

        try {
            references.add(obj)
            if (objClass.isSealedSubclass())
                result[config.sealedClassDiscriminator] = JSONString(objClass.simpleName ?: "null")

            val includeAll = config.hasIncludeAllPropertiesAnnotation(objClass.annotations)
            if (objClass.isData && objClass.constructors.isNotEmpty()) {
                // data classes will be a frequent use of serialization, so optimise for them
                val constructor = objClass.constructors.first()
                for (parameter in constructor.parameters) {
                    val member = objClass.members.find { it.name == parameter.name }
                    if (member is KProperty<*>)
                        result.addUsingGetter(member, parameter.annotations, obj, config, references, includeAll)
                }
                // now check whether there are any more properties not in constructor
                val statics: Collection<KProperty<*>> = objClass.staticProperties
                for (member in objClass.members) {
                    if (member is KProperty<*> && !statics.contains(member) &&
                            !constructor.parameters.any { it.name == member.name })
                        result.addUsingGetter(member, member.annotations, obj, config, references, includeAll)
                }
            }
            else {
                val statics: Collection<KProperty<*>> = objClass.staticProperties
                for (member in objClass.members) {
                    if (member is KProperty<*> && !statics.contains(member)) {
                        val combinedAnnotations = ArrayList(member.annotations)
                        objClass.constructors.firstOrNull()?.parameters?.find { it.name == member.name }?.let {
                            combinedAnnotations.addAll(it.annotations)
                        }
                        result.addUsingGetter(member, combinedAnnotations, obj, config, references, includeAll)
                    }
                }
            }
        }
        finally {
            references.remove(obj)
        }
        return result
    }

    private fun JSONObject.addUsingGetter(member: KProperty<*>, annotations: List<Annotation>?, obj: Any,
            config: JSONConfig, references: MutableSet<Any>, includeAll: Boolean) {
        if (!config.hasIgnoreAnnotation(annotations)) {
            val name = config.findNameFromAnnotation(annotations) ?: member.name
            val wasAccessible = member.isAccessible
            member.isAccessible = true
            try {
                val v = member.getter.call(obj)
                if (v != null && v in references)
                    fail("Circular reference: field ${member.name} in ${obj::class.simpleName}")
                if (v != null || config.hasIncludeIfNullAnnotation(annotations) || config.includeNulls || includeAll)
                    put(name, serialize(v, config, references))
            }
            catch (e: JSONException) {
                throw e
            }
            catch (e: Exception) {
                fail("Error getting property ${member.name} from ${obj::class.simpleName}", e)
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

    private fun serializeArray(array: Array<*>, config: JSONConfig, references: MutableSet<Any>): JSONValue {

        if (array.isArrayOf<Char>())
            return JSONString(StringBuilder().apply {
                for (ch in array)
                    append(ch)
            })

        return JSONArray(array.map { serialize(it, config, references) })
    }

    private fun serializePair(pair: Pair<*, *>, config: JSONConfig, references: MutableSet<Any>) = JSONArray().apply {
        add(serialize(pair.first, config, references))
        add(serialize(pair.second, config, references))
    }

    private fun serializeTriple(triple: Triple<*, *, *>, config: JSONConfig, references: MutableSet<Any>) =
            JSONArray().apply {
        add(serialize(triple.first, config, references))
        add(serialize(triple.second, config, references))
        add(serialize(triple.third, config, references))
    }

    private fun serializeMap(map: Map<*, *>, config: JSONConfig, references: MutableSet<Any>) = JSONObject().apply {
        for (entry in map.entries) {
            this[entry.key.toString()] = serialize(entry.value, config, references)
        }
    }

    private fun serializeBitSet(bitSet: BitSet) = JSONArray(ArrayList<JSONValue>().apply {
        for (i in 0 until bitSet.length())
            if (bitSet.get(i))
                add(JSONInt(i))
    })

}
