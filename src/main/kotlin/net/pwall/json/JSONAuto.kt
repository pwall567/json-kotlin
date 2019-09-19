/*
 * @(#) JSONAuto.kt
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

import java.lang.reflect.Type

object JSONAuto {

    /**
     * Serialize an object to JSON. (The word "stringify" is borrowed from the JavaScript implementation of JSON.)
     *
     * @param   obj     the object
     * @param   config  an optional [JSONConfig] to customise the conversion
     * @return          the JSON form of the object
     */
    fun stringify(obj: Any?, config: JSONConfig = JSONConfig.defaultConfig): String =
            JSONSerializer.serialize(obj, config)?.toJSON() ?: "null"

    /**
     * Deserialize JSON from string ([CharSequence]) to a specified [KType].
     *
     * @param   resultType  the target type
     * @param   str         the JSON in string form
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    fun parse(resultType: KType, str: CharSequence, config: JSONConfig = JSONConfig.defaultConfig): Any? =
            JSONDeserializer.deserialize(resultType, JSON.parse(str), config)

    /**
     * Deserialize JSON from string ([CharSequence]) to a specified [KClass].
     *
     * @param   resultClass the target class
     * @param   str         the JSON in string form
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    fun <T: Any> parse(resultClass: KClass<T>, str: CharSequence, config: JSONConfig = JSONConfig.defaultConfig): T? =
            JSONDeserializer.deserialize(resultClass, JSON.parse(str), config)

    /**
     * Deserialize JSON from string ([CharSequence]) to a the inferred [KClass].
     *
     * @param   str         the JSON in string form
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    inline fun <reified T: Any> parse(str: CharSequence, config: JSONConfig = JSONConfig.defaultConfig): T? =
            parse(T::class, str, config)

    /**
     * Deserialize JSON from string ([CharSequence]) to a specified Java [Type].
     *
     * @param   javaType    the target type
     * @param   str         the JSON in string form
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    fun parse(javaType: Type, str: CharSequence, config: JSONConfig = JSONConfig.defaultConfig): Any? =
            JSONDeserializer.deserialize(javaType, JSON.parse(str), config)

}
