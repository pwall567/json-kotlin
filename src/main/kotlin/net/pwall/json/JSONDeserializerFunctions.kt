/*
 * @(#) JSONDeserializerFunctions.kt
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
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSuperclassOf

import java.util.UUID

import net.pwall.json.validation.JSONValidation

object JSONDeserializerFunctions {

    private val fromJsonCache = HashMap<Pair<KClass<*>, KClass<*>>, KFunction<*>>()

    fun findFromJSON(resultClass: KClass<*>, parameterClass: KClass<*>): KFunction<*>? {
        val cacheKey = resultClass to parameterClass
        fromJsonCache[cacheKey]?.let { return it }
        val newEntry = try {
            resultClass.companionObject?.functions?.find { function ->
                function.name == "fromJSON" &&
                        function.parameters.size == 2 &&
                        function.parameters[0].type.classifier == resultClass.companionObject &&
                        (function.parameters[1].type.classifier as KClass<*>).isSuperclassOf(parameterClass) &&
                        function.returnType.classifier == resultClass
            }
        }
        catch (e: Exception) {
            null
        }
        return newEntry?.apply { fromJsonCache[cacheKey] = this }
    }

    fun KFunction<*>.hasSingleParameter(paramClass: KClass<*>) =
            parameters.size == 1 && parameters[0].type.classifier == paramClass

    fun findParameterName(parameter: KParameter, config: JSONConfig): String? =
            config.findNameFromAnnotation(parameter.annotations) ?: parameter.name

    fun createUUID(string: String): UUID {
        if (!JSONValidation.isUUID(string))
            throw IllegalArgumentException("Not a valid UUID - $string")
        return UUID.fromString(string)
    }

}
