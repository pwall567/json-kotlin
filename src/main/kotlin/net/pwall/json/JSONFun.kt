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
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

/**
 * Type alias for `JSONInt` to make it closer to Kotlin standard class names.
 */
typealias JSONInt = JSONInteger

/**
 * Type alias to simplify the definition of `fromJSON` mapping functions.
 */
typealias FromJSONMapping = (JSONValue?) -> Any?

/**
 * Type alias to simplify the definition of `toJSON` mapping functions.
 */
typealias ToJSONMapping = (Any?) -> JSONValue?

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
 * @param   T           the target class
 * @return              the converted object
 */
fun <T: Any> CharSequence.parseJSON(resultClass: KClass<T>, config: JSONConfig = JSONConfig.defaultConfig): T? =
        JSONAuto.parse(resultClass, this, config)

/**
 * Deserialize JSON from string ([CharSequence]) to a the inferred [KClass].
 *
 * @receiver        the JSON in string form
 * @param   config  an optional [JSONConfig] to customise the conversion
 * @param   T       the target class
 * @return          the converted object
 */
inline fun <reified T: Any> CharSequence.parseJSON(config: JSONConfig = JSONConfig.defaultConfig): T? =
        JSONAuto.parse(this, config)

/**
 * Deserialize JSON from string ([CharSequence]) to a [List] of the specified [KClass].
 *
 * @receiver            the JSON in string form
 * @param   itemClass   the item class of the target [List]
 * @param   config      an optional [JSONConfig] to customise the conversion
 * @param   T           the target item class
 * @return              the converted [List]
 */
@Suppress("UNCHECKED_CAST")
fun <T: Any> CharSequence.parseListJSON(itemClass: KClass<T>, config: JSONConfig = JSONConfig.defaultConfig): List<T>? =
        parseJSON(targetKType(List::class, itemClass, nullable = true), config) as List<T>?

/**
 * Deserialize JSON from string ([CharSequence]) to a [Set] of the specified [KClass].
 *
 * @receiver            the JSON in string form
 * @param   itemClass   the item class of the target [Set]
 * @param   config      an optional [JSONConfig] to customise the conversion
 * @param   T           the target item class
 * @return              the converted [Set]
 */
@Suppress("UNCHECKED_CAST")
fun <T: Any> CharSequence.parseSetJSON(itemClass: KClass<T>, config: JSONConfig = JSONConfig.defaultConfig): Set<T>? =
        parseJSON(targetKType(Set::class, itemClass, nullable = true), config) as Set<T>?

/**
 * Deserialize JSON from string ([CharSequence]) to a [Map] of the specified key and value [KClass]es.
 *
 * @receiver            the JSON in string form
 * @param   keyClass    the key class of the target [Map]
 * @param   valueClass  the value class of the target [Map]
 * @param   config      an optional [JSONConfig] to customise the conversion
 * @param   K           the target key class
 * @param   V           the target value class
 * @return              the converted [Map]
 */
@Suppress("UNCHECKED_CAST")
fun <K: Any, V: Any> CharSequence.parseMapJSON(keyClass: KClass<K>, valueClass: KClass<V>,
        config: JSONConfig = JSONConfig.defaultConfig): Map<K, V>? =
                parseJSON(targetKType(Map::class, keyClass, valueClass, nullable = true), config) as Map<K, V>?

/**
 * Deserialize JSON from string ([CharSequence]) to a [List] of the specified [KClass].
 *
 * @receiver            the JSON in string form
 * @param   itemType    the item type of the target [List]
 * @param   config      an optional [JSONConfig] to customise the conversion
 * @return              the converted [List]
 */
@Suppress("UNCHECKED_CAST")
fun CharSequence.parseListJSON(itemType: KType, config: JSONConfig = JSONConfig.defaultConfig): List<*>? =
        parseJSON(targetKType(List::class, itemType, nullable = true), config) as List<*>?

/**
 * Deserialize JSON from string ([CharSequence]) to a [Set] of the specified [KClass].
 *
 * @receiver            the JSON in string form
 * @param   itemType    the item type of the target [Set]
 * @param   config      an optional [JSONConfig] to customise the conversion
 * @return              the converted [Set]
 */
@Suppress("UNCHECKED_CAST")
fun CharSequence.parseSetJSON(itemType: KType, config: JSONConfig = JSONConfig.defaultConfig): Set<*>? =
        parseJSON(targetKType(Set::class, itemType, nullable = true), config) as Set<*>?

/**
 * Deserialize JSON from string ([CharSequence]) to a [Map] of the specified key and value [KClass]es.
 *
 * @receiver            the JSON in string form
 * @param   keyType     the key type of the target [Map]
 * @param   valueType   the value type of the target [Map]
 * @param   config      an optional [JSONConfig] to customise the conversion
 * @return              the converted [Map]
 */
@Suppress("UNCHECKED_CAST")
fun CharSequence.parseMapJSON(keyType: KType, valueType: KType,
        config: JSONConfig = JSONConfig.defaultConfig): Map<*, *>? =
                parseJSON(targetKType(Map::class, keyType, valueType, nullable = true), config) as Map<*, *>?

/**
 * Stringify any object to JSON.
 *
 * @receiver        the object to be converted to JSON (`null` will be converted to `"null"`).
 * @param   config  an optional [JSONConfig] to customise the conversion
 * @return          the JSON string
 */
fun Any?.stringifyJSON(config: JSONConfig = JSONConfig.defaultConfig): String = JSONAuto.stringify(this, config)

/**
 * Helper method to create a [KType] for a parameterised type, for use as the target type of a deserialization.
 *
 * @param   mainClass       the parameterised class
 * @param   paramClasses    the parameter classes
 * @param   nullable        `true` if the [KType] is to be nullable
 * @return                  the [KType]
 */
fun targetKType(mainClass: KClass<*>, vararg paramClasses: KClass<*>, nullable: Boolean = false): KType =
        mainClass.createType(paramClasses.map { KTypeProjection.covariant(it.starProjectedType) }, nullable)

/**
 * Helper method to create a [KType] for a parameterised type, for use as the target type of a deserialization.
 *
 * @param   mainClass       the parameterised class
 * @param   paramTypes      the parameter types
 * @param   nullable        `true` if the [KType] is to be nullable
 * @return                  the [KType]
 */
fun targetKType(mainClass: KClass<*>, vararg paramTypes: KType, nullable: Boolean = false): KType =
        mainClass.createType(paramTypes.map { KTypeProjection.covariant(it) }, nullable)

/**
 * Convert a Java [Type] to a Kotlin [KType].  This allows Java [Type]s to be used as the target type of a
 * deserialization operation.
 *
 * This function is not complete, but with any luck it will cover the great majority of usages.
 *
 * @receiver    the Java [Type] to be converted
 * @param       nullable    `true` if the [KType] is to be nullable
 * @return      the resulting Kotlin [KType]
 * @throws      JSONException if the [Type] can not be converted
 */
fun Type.toKType(nullable: Boolean = false): KType = when (this) {
    is Class<*> -> this.kotlin.createType(nullable = nullable)
    is ParameterizedType -> (this.rawType as Class<*>).kotlin.createType(this.actualTypeArguments.map {
        when (it) {
            is WildcardType ->
                if (it.lowerBounds?.firstOrNull() == null)
                    KTypeProjection.covariant((it.upperBounds[0] as Class<*>).kotlin.starProjectedType)
                else
                    KTypeProjection.contravariant((it.lowerBounds[0] as Class<*>).kotlin.starProjectedType)
            else -> KTypeProjection.invariant(it.toKType())
        } }, nullable)
    else -> throw JSONException("Can't handle type: $this")
}
