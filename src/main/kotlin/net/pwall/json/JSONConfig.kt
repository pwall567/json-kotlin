/*
 * @(#) JSONConfig.kt
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

import kotlin.reflect.KType
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.starProjectedType

/**
 * Configuration class for JSON Auto serialize / deserialize for Kotlin.
 *
 * @author  Peter Wall
 */
class JSONConfig {

    /** Read buffer size (for `json-ktor`) */
    var readBufferSize = defaultBufferSize

    private val fromJSONMap: MutableMap<KType, (JSONValue?) -> Any?> = HashMap()

    private val toJSONMap: MutableMap<KType, (Any?) -> JSONValue?> = HashMap()

    fun getFromJSONMapping(type: KType): ((JSONValue?) -> Any?)? = fromJSONMap[type]

    fun getToJSONMapping(type: KType): ((Any?) -> JSONValue?)? = toJSONMap[type]

    fun findToJSONMapping(type: KType): ((Any?) -> JSONValue?)? {
        val match = toJSONMap.keys.find { it.isSupertypeOf(type) } ?: return null
        return toJSONMap[match]
    }

    /**
     * Add custom mapping from JSON to the specified type.
     *
     * @param   type    the target type
     * @param   mapping the mapping function
     * @return          the `JSONConfig` object (for chaining)
     */
    fun fromJSON(type: KType, mapping: (JSONValue?) -> Any?): JSONConfig {
        fromJSONMap[type] = mapping
        return this
    }

    /**
     * Add custom mapping from a specified type to JSON.
     *
     * @param   type    the source type
     * @param   mapping the mapping function
     * @return          the `JSONConfig` object (for chaining)
     */
    fun toJSON(type: KType, mapping: (Any?) -> JSONValue?): JSONConfig {
        toJSONMap[type] = mapping
        return this
    }

    /**
     * Add custom mapping from JSON to the inferred type.
     *
     * @param   mapping the mapping function
     * @return          the `JSONConfig` object (for chaining)
     */
    inline fun <reified T: Any> fromJSON(noinline mapping: (JSONValue?) -> T?) =
            fromJSON(T::class.starProjectedType, mapping)

    /**
     * Add custom mapping from an inferred type to JSON.
     *
     * @param   mapping the mapping function
     * @return          the `JSONConfig` object (for chaining)
     */
    inline fun <reified T: Any> toJSON(noinline mapping: (T?) -> JSONValue?) =
            toJSON(T::class.starProjectedType) { mapping(it as T?) }

    companion object {
        const val defaultBufferSize = DEFAULT_BUFFER_SIZE
    }

}
