/*
 * @(#) JSONFun.kt
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

import kotlin.reflect.KClass
import kotlin.reflect.KType

typealias JSONInt = JSONInteger

/**
 * Convert a [CharSequence] ([String], [StringBuilder] etc.) to a [JSONValue].
 *
 * @receiver    the [CharSequence] to be converted
 * @return      the [JSONValue]
 */
fun CharSequence.asJSONValue(): JSONValue = JSONString(this)

/**
 * Convert a [Char] to a [JSONValue].
 *
 * @receiver    the [Char] to be converted
 * @return      the [JSONValue]
 */
fun Char.asJSONValue(): JSONValue = JSONString(StringBuilder().append(this))

/**
 * Convert an [Int] to a [JSONValue].
 *
 * @receiver    the [Int] to be converted
 * @return      the [JSONValue]
 */
fun Int.asJSONValue(): JSONValue = JSONInt.valueOf(this)

/**
 * Convert a [Long] to a [JSONValue].
 *
 * @receiver    the [Long] to be converted
 * @return      the [JSONValue]
 */
fun Long.asJSONValue(): JSONValue = JSONLong.valueOf(this)

/**
 * Convert a [Short] to a [JSONValue].
 *
 * @receiver    the [Short] to be converted
 * @return      the [JSONValue]
 */
fun Short.asJSONValue(): JSONValue = JSONInt.valueOf(this.toInt())

/**
 * Convert a [Byte] to a [JSONValue].
 *
 * @receiver    the [Byte] to be converted
 * @return      the [JSONValue]
 */
fun Byte.asJSONValue(): JSONValue = JSONInt.valueOf(this.toInt())

/**
 * Convert a [Float] to a [JSONValue].
 *
 * @receiver    the [Float] to be converted
 * @return      the [JSONValue]
 */
fun Float.asJSONValue(): JSONValue = JSONFloat.valueOf(this)

/**
 * Convert a [Double] to a [JSONValue].
 *
 * @receiver    the [Double] to be converted
 * @return      the [JSONValue]
 */
fun Double.asJSONValue(): JSONValue = JSONDouble.valueOf(this)

/**
 * Convert a [Boolean] to a [JSONValue].
 *
 * @receiver    the [Boolean] to be converted
 * @return      the [JSONValue]
 */
fun Boolean.asJSONValue(): JSONValue = JSONBoolean.valueOf(this)

/**
 * Convert any object to a [JSONValue].
 *
 * @receiver        the object to be converted
 * @param   config  an optional [JSONConfig] to customise the conversion
 * @return          the [JSONValue]
 */
fun Any?.asJSONValue(config: JSONConfig = JSONConfig.defaultConfig): JSONValue? = JSONSerializer.serialize(this, config)

/**
 * Create a [Pair] of [String] and [JSONValue]? for use with [makeJSON].
 *
 * @receiver    the [String]
 * @param   str a [CharSequence] ([String], [StringBuilder] etc.)
 * @return      the [Pair]
 */
infix fun String.isJSON(str: CharSequence?): Pair<String, JSONValue?> = this to str?.asJSONValue()

/**
 * Create a [Pair] of [String] and [JSONValue]? for use with [makeJSON].
 *
 * @receiver    the [String]
 * @param   ch  a [Char]
 * @return      the [Pair]
 */
infix fun String.isJSON(ch: Char): Pair<String, JSONValue?> = this to ch.asJSONValue()

/**
 * Create a [Pair] of [String] and [JSONValue]? for use with [makeJSON].
 *
 * @receiver    the [String]
 * @param   i   an [Int]
 * @return      the [Pair]
 */
infix fun String.isJSON(i: Int): Pair<String, JSONValue?> = this to i.asJSONValue()

/**
 * Create a [Pair] of [String] and [JSONValue]? for use with [makeJSON].
 *
 * @receiver    the [String]
 * @param   i   a [Long]
 * @return      the [Pair]
 */
infix fun String.isJSON(i: Long): Pair<String, JSONValue?> = this to i.asJSONValue()

/**
 * Create a [Pair] of [String] and [JSONValue]? for use with [makeJSON].
 *
 * @receiver    the [String]
 * @param   s   a [Short]
 * @return      the [Pair]
 */
infix fun String.isJSON(s: Short): Pair<String, JSONValue?> = this to s.asJSONValue()

/**
 * Create a [Pair] of [String] and [JSONValue]? for use with [makeJSON].
 *
 * @receiver    the [String]
 * @param   b   a [Byte]
 * @return      the [Pair]
 */
infix fun String.isJSON(b: Byte): Pair<String, JSONValue?> = this to b.asJSONValue()

/**
 * Create a [Pair] of [String] and [JSONValue]? for use with [makeJSON].
 *
 * @receiver    the [String]
 * @param   f   a [Float]
 * @return      the [Pair]
 */
infix fun String.isJSON(f: Float): Pair<String, JSONValue?> = this to f.asJSONValue()

/**
 * Create a [Pair] of [String] and [JSONValue]? for use with [makeJSON].
 *
 * @receiver    the [String]
 * @param   d   a [Double]
 * @return      the [Pair]
 */
infix fun String.isJSON(d: Double): Pair<String, JSONValue?> = this to d.asJSONValue()

/**
 * Create a [Pair] of [String] and [JSONValue]? for use with [makeJSON].
 *
 * @receiver    the [String]
 * @param   b   a [Boolean]
 * @return      the [Pair]
 */
infix fun String.isJSON(b: Boolean): Pair<String, JSONValue?> = this to b.asJSONValue()

/**
 * Create a [JSONObject] from a list of [Pair]s of [String]s and [JSONValue]s.
 *
 * @param   pairs   the list of pairs
 * @return          the [JSONObject]
 */
fun makeJSON(vararg pairs: Pair<String, JSONValue?>) = JSONObject().apply { putAll(pairs) }

/**
 * Deserialize JSON from string ([CharSequence]) to a specified [KType].
 *
 * @receiver            the JSON in string form
 * @param   resultType  the target type
 * @param   config      an optional [JSONConfig] to customise the conversion
 * @return              the converted object
 */
fun CharSequence.parseJSON(resultType: KType, config: JSONConfig = JSONConfig.defaultConfig): Any? =
        JSONAuto.parse(resultType, this, config)

/**
 * Deserialize JSON from string ([CharSequence]) to a specified [KClass].
 *
 * @receiver            the JSON in string form
 * @param   resultClass the target class
 * @param   config      an optional [JSONConfig] to customise the conversion
 * @return              the converted object
 */
fun <T: Any> CharSequence.parseJSON(resultClass: KClass<T>, config: JSONConfig = JSONConfig.defaultConfig): T? =
        JSONAuto.parse(resultClass, this, config)

/**
 * Deserialize JSON from string ([CharSequence]) to a the inferred [KClass].
 *
 * @receiver        the JSON in string form
 * @param   config  an optional [JSONConfig] to customise the conversion
 * @return          the converted object
 */
inline fun <reified T: Any> CharSequence.parseJSON(config: JSONConfig = JSONConfig.defaultConfig): T? =
        JSONAuto.parse<T>(this, config)

/**
 * Stringify any object to JSON.
 *
 * @receiver        the object to be converted to JSON (`null` will be converted to `"null"`).
 * @param   config  an optional [JSONConfig] to customise the conversion
 * @return          the JSON string
 */
fun Any?.stringifyJSON(config: JSONConfig = JSONConfig.defaultConfig): String = JSONAuto.stringify(this, config)
