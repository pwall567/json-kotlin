/*
 * @(#) JSONDelegate.kt
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

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A delegate class to create a property from a JSON string.
 *
 * @constructor
 * @param   json    the string to be parsed
 * @param   config  an optional [JSONConfig] to customise the conversion
 * @param   T       the target type
 * @author  Peter Wall
 */
class JSONDelegate<out T>(val json: String, val config: JSONConfig = JSONConfig.defaultConfig) :
        ReadOnlyProperty<Any?, T> {

    private var value: Any? = UNINITIALISED

    /**
     * Get the value of the property - the first time through this will parse the supplied string and store it for
     * subsequent references.
     *
     * This function is not intended to be thread- or coroutine-safe, but if it is used by multiple processes in
     * parallel the worst that can happen (in very rare cases) is that the string will be re-parsed unnecessarily.
     *
     * @param   thisRef     a reference to the containing object - not used
     * @param   property    the property descriptor - used to get the type
     */
    @Suppress("UNCHECKED_CAST")
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value === UNINITIALISED)
            value = JSONAuto.parse(property.returnType, json, config)
        return value as T
    }

    private object UNINITIALISED

}
