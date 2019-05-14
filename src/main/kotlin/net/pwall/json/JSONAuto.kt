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

object JSONAuto {

    /**
     * Serialize an object to JSON. (The word "stringify" is borrowed from the JavaScript implementation of JSON.)
     *
     * @param   obj     the object
     * @return          the JSON form of the object
     */
    fun stringify(obj: Any?): String = JSONSerializer.serialize(obj)?.toJSON() ?: "null"

    /**
     * Deserialize JSON from [String] to a specified [KType].
     *
     * @param   resultType  the target type
     * @param   str         the JSON in [String] form
     * @return              the converted object
     */
    fun parse(resultType: KType, str: String): Any? {
        return JSONDeserializer.deserialize(resultType, JSON.parse(str))
    }

    /**
     * Deserialize JSON from [String] to a specified [KClass].
     *
     * @param   resultClass the target class
     * @param   json        the parsed JSON, as a [JSONValue] (or `null`)
     * @return              the converted object
     */
    fun <T: Any> parse(resultClass: KClass<T>, str: String): T? {
        return JSONDeserializer.deserialize(resultClass, JSON.parse(str))
    }

    inline fun <reified T: Any> parse(str: String): T? = parse(T::class, str)

}
