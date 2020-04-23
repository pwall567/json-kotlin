/*
 * @(#) JSONStringify.kt
 *
 * json-kotlin Kotlin JSON Auto Serialize/deserialize
 * Copyright (c) 2020 Peter Wall
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

import net.pwall.json.JSONSerializerFunctions.findToJSON
import net.pwall.json.JSONSerializerFunctions.formatISO8601
import net.pwall.json.JSONSerializerFunctions.isSealedSubclass
import net.pwall.json.JSONSerializerFunctions.isToStringClass
import net.pwall.util.Strings

/**
 * JSON Auto serialize for Kotlin - serialize direct to `String`.
 *
 * @author  Peter Wall
 */
object JSONStringify {

    /**
     * Serialize an object to JSON. (The word "stringify" is borrowed from the JavaScript implementation of JSON.)
     *
     * @param   obj     the object
     * @param   config  an optional [JSONConfig] to customise the conversion
     * @return          the JSON form of the object
     */
    fun stringify(obj: Any?, config: JSONConfig = JSONConfig.defaultConfig) : String {
        return when (obj) {
            null -> "null"
            else -> StringBuilder(config.stringifyInitialSize).apply {
                appendJSON(obj, config)
            }.toString()
        }
    }

    /**
     * Append the serialized form of an object to an [Appendable] in JSON.
     *
     * @receiver        the [Appendable] (e.g. [StringBuilder])
     * @param   obj     the object
     * @param   config  an optional [JSONConfig] to customise the conversion
     */
    fun Appendable.appendJSON(obj: Any?, config: JSONConfig = JSONConfig.defaultConfig) {
        appendJSON(obj, config, mutableSetOf())
    }

    private fun Appendable.appendJSON(obj: Any?, config: JSONConfig, references: MutableSet<Any>) {

        if (obj == null) {
            append("null")
            return
        }

        config.findToJSONMapping(obj::class)?.let {
            JSON.appendJSON(this, it(obj))
            return
        }

        when (obj) {
            is JSONValue -> obj.appendJSON(this)
            is CharSequence -> appendJSONString(obj)
            is CharArray -> {
                append('"')
                for (ch in obj)
                    appendJSONChar(ch)
                append('"')
            }
            is Char -> {
                append('"')
                appendJSONChar(obj)
                append('"')
            }
            is Number -> appendJSONNumber(obj, config, references)
            is Boolean -> append(if (obj) "true" else "false")
            is Array<*> -> appendJSONArray(obj, config, references)
            is Pair<*, *> -> appendJSONPair(obj, config, references)
            is Triple<*, *, *> -> appendJSONTriple(obj, config, references)
            else -> appendJSONObject(obj, config, references)
        }

    }

    private fun Appendable.appendJSONNumber(number: Number, config: JSONConfig, references: MutableSet<Any>) {
        when (number) {
            is Int -> Strings.appendInt(this, number)
            is Short, is Byte -> Strings.appendInt(this, number.toInt())
            is Long -> Strings.appendLong(this, number)
            is Float, is Double -> append(number.toString())
            is BigInteger -> {
                if (config.bigIntegerString) {
                    append('"')
                    append(number.toString())
                    append('"')
                }
                else
                    append(number.toString())
            }
            is BigDecimal -> {
                if (config.bigDecimalString) {
                    append('"')
                    append(number.toString())
                    append('"')
                }
                else
                    append(number.toString())
            }
            else -> appendJSONObject(number, config, references)
        }
    }

    private fun Appendable.appendJSONArray(array: Array<*>, config: JSONConfig, references: MutableSet<Any>) {
        if (array.isArrayOf<Char>()) {
            append('"')
            for (ch in array)
                appendJSONChar(ch as Char)
            append('"')
        }
        else {
            append('[')
            if (array.isNotEmpty()) {
                for (i in array.indices) {
                    if (i > 0)
                        append(',')
                    appendJSON(array[i], config, references)
                }
            }
            append(']')
        }
    }

    private fun Appendable.appendJSONPair(pair: Pair<*, *>, config: JSONConfig, references: MutableSet<Any>) {
        append('[')
        appendJSON(pair.first, config, references)
        append(',')
        appendJSON(pair.second, config, references)
        append(']')
    }

    private fun Appendable.appendJSONTriple(pair: Triple<*, *, *>, config: JSONConfig, references: MutableSet<Any>) {
        append('[')
        appendJSON(pair.first, config, references)
        append(',')
        appendJSON(pair.second, config, references)
        append(',')
        appendJSON(pair.third, config, references)
        append(']')
    }

    private fun Appendable.appendJSONObject(obj: Any, config: JSONConfig, references: MutableSet<Any>) {
        val objClass = obj::class
        if (objClass.isToStringClass() || obj is Enum<*>) {
            appendJSONString(obj.toString())
            return
        }
        objClass.findToJSON()?.let {
            try {
                it.call(obj).appendJSON(this)
                return
            }
            catch (e: Exception) {
                throw JSONException("Error in custom toJSON - ${objClass.simpleName}", e)
            }
        }
        when (obj) {
            is Iterable<*> -> appendJSONIterator(obj.iterator(), config, references)
            is Iterator<*> -> appendJSONIterator(obj, config, references)
            is Sequence<*> -> appendJSONIterator(obj.iterator(), config, references)
            is Enumeration<*> -> appendJSONEnumeration(obj, config, references)
            is Map<*, *> -> appendJSONMap(obj, config, references)
            is Calendar -> appendJSONString(obj.formatISO8601())
            is Date -> appendJSONString((Calendar.getInstance().apply { time = obj }).formatISO8601())
            is BitSet -> appendJSONBitSet(obj)
            else -> {
                try {
                    references.add(obj)
                    append('{')
                    var continuation = false
                    if (objClass.isSealedSubclass()) {
                        appendJSONString(config.sealedClassDiscriminator)
                        append(':')
                        appendJSONString(objClass.simpleName ?: "null")
                        continuation = true
                    }
                    val includeAll = config.hasIncludeAllPropertiesAnnotation(objClass.annotations)
                    val statics: Collection<KProperty<*>> = objClass.staticProperties
                    if (objClass.isData && objClass.constructors.isNotEmpty()) {
                        // data classes will be a frequent use of serialization, so optimise for them
                        val constructor = objClass.constructors.first()
                        for (parameter in constructor.parameters) {
                            val member = objClass.members.find { it.name == parameter.name }
                            if (member is KProperty<*>)
                                continuation = appendUsingGetter(member, parameter.annotations, obj, config, references,
                                        includeAll, continuation)
                        }
                        // now check whether there are any more properties not in constructor
                        for (member in objClass.members) {
                            if (member is KProperty<*> && !statics.contains(member) &&
                                    !constructor.parameters.any { it.name == member.name })
                                continuation = appendUsingGetter(member, member.annotations, obj, config, references,
                                        includeAll, continuation)
                        }
                    }
                    else {
                        for (member in objClass.members) {
                            if (member is KProperty<*> && !statics.contains(member)) {
                                val combinedAnnotations = ArrayList(member.annotations)
                                objClass.constructors.firstOrNull()?.parameters?.find { it.name == member.name }?.let {
                                    combinedAnnotations.addAll(it.annotations)
                                }
                                continuation = appendUsingGetter(member, combinedAnnotations, obj, config, references,
                                        includeAll, continuation)
                            }
                        }
                    }
                    append('}')
                }
                finally {
                    references.remove(obj)
                }
            }
        }
    }

    private fun Appendable.appendUsingGetter(member: KProperty<*>, annotations: List<Annotation>?, obj: Any,
            config: JSONConfig, references: MutableSet<Any>, includeAll: Boolean, continuation: Boolean): Boolean {
        if (!config.hasIgnoreAnnotation(annotations)) {
            val name = config.findNameFromAnnotation(annotations) ?: member.name
            val wasAccessible = member.isAccessible
            member.isAccessible = true
            try {
                val v = member.getter.call(obj)
                if (v != null && v in references)
                    throw JSONException("Circular reference: field ${member.name} in ${obj::class.simpleName}")
                if (v != null || config.hasIncludeIfNullAnnotation(annotations) || config.includeNulls || includeAll) {
                    if (continuation)
                        append(',')
                    appendJSONString(name)
                    append(':')
                    appendJSON(v, config, references)
                    return true
                }
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
        return continuation
    }

    private fun Appendable.appendJSONIterator(iterator: Iterator<*>, config: JSONConfig, references: MutableSet<Any>) {
        append('[')
        if (iterator.hasNext()) {
            while (true) {
                appendJSON(iterator.next(), config, references)
                if (!iterator.hasNext())
                    break
                append(',')
            }
        }
        append(']')
    }

    private fun Appendable.appendJSONEnumeration(enumeration: Enumeration<*>, config: JSONConfig,
            references: MutableSet<Any>) {
        append('[')
        if (enumeration.hasMoreElements()) {
            while (true) {
                appendJSON(enumeration.nextElement(), config, references)
                if (!enumeration.hasMoreElements())
                    break
                append(',')
            }
        }
        append(']')
    }

    private fun Appendable.appendJSONMap(map: Map<*, *>, config: JSONConfig, references: MutableSet<Any>) {
        append('{')
        map.entries.iterator().let {
            if (it.hasNext()) {
                while (true) {
                    val ( key, value ) = it.next()
                    appendJSONString(key.toString())
                    append(':')
                    appendJSON(value, config, references)
                    if (!it.hasNext())
                        break
                    append(',')
                }
            }
        }
        append('}')
    }

    private fun Appendable.appendJSONBitSet(bitSet: BitSet) {
        append('[')
        var continuation = false
        for (i in 0 until bitSet.length()) {
            if (bitSet.get(i)) {
                if (continuation)
                    append(',')
                Strings.appendInt(this, i)
                continuation = true
            }
        }
        append(']')
    }

    private fun Appendable.appendJSONString(cs: CharSequence) {
        append('"')
        for (ch in cs)
            appendJSONChar(ch)
        append('"')
    }

    private fun Appendable.appendJSONChar(ch: Char) {
        when (ch) {
            '"', '\\' -> append('\\').append(ch)
            in ' '..'\u007F' -> append(ch)
            '\n' -> append("\\n")
            '\t' -> append("\\t")
            '\r' -> append("\\r")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            else -> {
                append("\\u")
                Strings.appendHex(this, ch)
            }
        }
    }

}
