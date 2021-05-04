/*
 * @(#) JSONAuto.kt
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

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

import java.io.File
import java.io.InputStream
import java.io.Reader
import java.lang.reflect.Type

@OptIn(ExperimentalStdlibApi::class)
object JSONAuto {

    /**
     * Serialize an object to JSON. (The word "stringify" is borrowed from the JavaScript implementation of JSON.)
     *
     * @param   obj     the object
     * @param   config  an optional [JSONConfig] to customise the conversion
     * @return          the JSON form of the object
     */
    fun stringify(obj: Any?, config: JSONConfig = JSONConfig.defaultConfig): String =
            JSONStringify.stringify(obj, config)

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
     * Deserialize JSON from string ([CharSequence]) to the inferred [KType].
     *
     * @param   str         the JSON in string form
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    inline fun <reified T: Any> parse(str: CharSequence, config: JSONConfig = JSONConfig.defaultConfig): T? =
            parse(typeOf<T>(), str, config) as T?

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

    /**
     * Deserialize JSON from a [Reader] to a specified [KType].
     *
     * @param   resultType  the target type
     * @param   reader      the JSON in the form of a [Reader]
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    fun parse(resultType: KType, reader: Reader, config: JSONConfig = JSONConfig.defaultConfig): Any? =
            JSONDeserializer.deserialize(resultType, JSON.parse(reader), config)

    /**
     * Deserialize JSON from a [Reader] to a specified [KClass].
     *
     * @param   resultClass the target class
     * @param   reader      the JSON in the form of a [Reader]
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    fun <T: Any> parse(resultClass: KClass<T>, reader: Reader, config: JSONConfig = JSONConfig.defaultConfig): T? =
            JSONDeserializer.deserialize(resultClass, JSON.parse(reader), config)

    /**
     * Deserialize JSON from a [Reader] to the inferred [KType].
     *
     * @param   reader      the JSON in the form of a [Reader]
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    inline fun <reified T: Any> parse(reader: Reader, config: JSONConfig = JSONConfig.defaultConfig): T? =
            parse(typeOf<T>(), reader, config) as T?

    /**
     * Deserialize JSON from a [Reader] to a specified Java [Type].
     *
     * @param   javaType    the target type
     * @param   reader      the JSON in the form of a [Reader]
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    fun parse(javaType: Type, reader: Reader, config: JSONConfig = JSONConfig.defaultConfig): Any? =
            JSONDeserializer.deserialize(javaType, JSON.parse(reader), config)

    /**
     * Deserialize JSON from an [InputStream] to a specified [KType].
     *
     * @param   resultType  the target type
     * @param   inputStream the JSON in the form of an [InputStream]
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    fun parse(resultType: KType, inputStream: InputStream, config: JSONConfig = JSONConfig.defaultConfig): Any? =
            JSONDeserializer.deserialize(resultType, JSON.parse(inputStream), config)

    /**
     * Deserialize JSON from an [InputStream] to a specified [KClass].
     *
     * @param   resultClass the target class
     * @param   inputStream the JSON in the form of an [InputStream]
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    fun <T: Any> parse(resultClass: KClass<T>, inputStream: InputStream,
            config: JSONConfig = JSONConfig.defaultConfig): T? =
                    JSONDeserializer.deserialize(resultClass, JSON.parse(inputStream), config)

    /**
     * Deserialize JSON from an [InputStream] to the inferred [KType].
     *
     * @param   inputStream the JSON in the form of an [InputStream]
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    inline fun <reified T: Any> parse(inputStream: InputStream, config: JSONConfig = JSONConfig.defaultConfig): T? =
            parse(typeOf<T>(), inputStream, config) as T?

    /**
     * Deserialize JSON from an [InputStream] to a specified Java [Type].
     *
     * @param   javaType    the target type
     * @param   inputStream the JSON in the form of an [InputStream]
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    fun parse(javaType: Type, inputStream: InputStream, config: JSONConfig = JSONConfig.defaultConfig): Any? =
            JSONDeserializer.deserialize(javaType, JSON.parse(inputStream), config)

    /**
     * Deserialize JSON from a [File] to a specified [KType].
     *
     * @param   resultType  the target type
     * @param   file        the JSON in the form of a [File]
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    fun parse(resultType: KType, file: File, config: JSONConfig = JSONConfig.defaultConfig): Any? =
            JSONDeserializer.deserialize(resultType, JSON.parse(file), config)

    /**
     * Deserialize JSON from a [File] to a specified [KClass].
     *
     * @param   resultClass the target class
     * @param   file        the JSON in the form of a [File]
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    fun <T: Any> parse(resultClass: KClass<T>, file: File, config: JSONConfig = JSONConfig.defaultConfig): T? =
            JSONDeserializer.deserialize(resultClass, JSON.parse(file), config)

    /**
     * Deserialize JSON from a [File] to the inferred [KType].
     *
     * @param   file        the JSON in the form of a [File]
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    inline fun <reified T: Any> parse(file: File, config: JSONConfig = JSONConfig.defaultConfig): T? =
            parse(typeOf<T>(), file, config) as T?

    /**
     * Deserialize JSON from a [File] to a specified Java [Type].
     *
     * @param   javaType    the target type
     * @param   file        the JSON in the form of a [File]
     * @param   config      an optional [JSONConfig] to customise the conversion
     * @return              the converted object
     */
    fun parse(javaType: Type, file: File, config: JSONConfig = JSONConfig.defaultConfig): Any? =
            JSONDeserializer.deserialize(javaType, JSON.parse(file), config)

}
