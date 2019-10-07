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

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.starProjectedType

import net.pwall.json.annotation.JSONIgnore
import net.pwall.json.annotation.JSONName

/**
 * Configuration class for JSON Auto serialize / deserialize for Kotlin.
 *
 * @author  Peter Wall
 */
class JSONConfig {

    /** Read buffer size (for `json-ktor`) */
    var readBufferSize = defaultBufferSize

    /** Character set (for `json-ktor' and  `json-ktor-client' */
    var charset = defaultCharset

    private val fromJSONMap: MutableMap<KType, (JSONValue?) -> Any?> = HashMap()

    private val toJSONMap: MutableMap<KType, (Any?) -> JSONValue?> = HashMap()

    private val nameAnnotations: MutableList<Pair<KClass<*>, KProperty.Getter<String>>> =
            arrayListOf(namePropertyPair(JSONName::class, "name"))

    private val ignoreAnnotations: MutableList<KClass<*>> = arrayListOf(JSONIgnore::class, Transient::class)

    /**
     * Get a `fromJSON` mapping function for the specified [KType].
     *
     * @param   type    the target type
     * @return          the mapping function, or `null` if not found
     */
    fun getFromJSONMapping(type: KType): ((JSONValue?) -> Any?)? = fromJSONMap[type]

    /**
     * Get a `toJSON` mapping function for the specified [KType].
     *
     * @param   type    the source type
     * @return          the mapping function, or `null` if not found
     */
    fun getToJSONMapping(type: KType): ((Any?) -> JSONValue?)? = toJSONMap[type]

    /**
     * Find a `toJSON` mapping function that will accept the specified type, or any supertype of it.
     *
     * @param   type    the source type
     * @return          the mapping function, or `null` if not found
     */
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
     * @param   T       the type to be mapped
     * @return          the `JSONConfig` object (for chaining)
     */
    inline fun <reified T: Any> fromJSON(noinline mapping: (JSONValue?) -> T?) =
            fromJSON(T::class.starProjectedType, mapping)

    /**
     * Add custom mapping from an inferred type to JSON.
     *
     * @param   mapping the mapping function
     * @param   T       the type to be mapped
     * @return          the `JSONConfig` object (for chaining)
     */
    inline fun <reified T: Any> toJSON(noinline mapping: (T?) -> JSONValue?) =
            toJSON(T::class.starProjectedType) { mapping(it as T?) }

    /**
     * Add an annotation specification to the list of annotations that specify the name to be used when serializing or
     * deserializing a property.
     *
     * @param   nameAnnotationClass the annotation class to specify the name
     * @param   argumentName        the name of the argument to the annotation that holds the name
     * @param   T                   the annotation class
     * @return                      the `JSONConfig` object (for chaining)
     */
    fun <T: Annotation> addNameAnnotation(nameAnnotationClass: KClass<T>, argumentName: String): JSONConfig {
        nameAnnotations.add(namePropertyPair(nameAnnotationClass, argumentName))
        return this
    }

    private fun <T: Annotation> namePropertyPair(nameAnnotationClass: KClass<T>, argumentName: String):
            Pair<KClass<*>, KProperty.Getter<String>> {
        return nameAnnotationClass to findAnnotationStringProperty(nameAnnotationClass, argumentName).getter
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Annotation> findAnnotationStringProperty(annotationClass: KClass<T>, argumentName: String):
            KProperty<String> {
        annotationClass.members.forEach {
            if (it is KProperty<*> && it.name == argumentName && it.returnType == String::class.starProjectedType) {
                return it as KProperty<String>
            }
        }
        throw IllegalArgumentException(
                "Annotation class ${annotationClass.simpleName} does not have a String property $argumentName")
    }

    /**
     * Find the name to be used when serializing or deserializing a property, from the annotations supplied.
     *
     * @param   annotations the [Annotation]s (from the parameter, or the property, or both)
     * @return              the name to be used, or `null` if no annotation found
     */
    fun findNameFromAnnotation(annotations: List<Annotation>?): String? {
        nameAnnotations.forEach { entry ->
            annotations?.forEach {
                if (it::class.isSubclassOf(entry.first))
                    return entry.second.call(it)
            }
        }
        return null
    }

    /**
     * Add an annotation specification to the list of annotations that specify that the property is to be ignored when
     * serializing or deserializing.
     *
     * @param   ignoreAnnotationClass   the annotation class
     * @param   T                       the annotation class
     * @return                          the `JSONConfig` object (for chaining)
     */
    fun <T: Annotation> addIgnoreAnnotation(ignoreAnnotationClass: KClass<T>): JSONConfig {
        ignoreAnnotations.add(ignoreAnnotationClass)
        return this
    }

    /**
     * Test whether a property has an annotation to indicate that it is to be ignored when serializing or deserializing.
     *
     * @param   annotations the [Annotation]s (from the parameter, or the property, or both)
     * @return              `true` if an "ignore" annotation appears in the supplied list
     */
    fun hasIgnoreAnnotation(annotations: List<Annotation>?): Boolean {
        ignoreAnnotations.forEach { entry ->
            annotations?.forEach {
                if (it::class.isSubclassOf(entry))
                    return true
            }
        }
        return false
    }

    /**
     * Combine another `JSONConfig` into this one.
     *
     * @param   config  the other `JSONConfig`
     * @return          the `JSONConfig` object (for chaining)
     */
    fun combineAll(config: JSONConfig): JSONConfig {
        readBufferSize = config.readBufferSize
        charset = config.charset
        fromJSONMap.putAll(config.fromJSONMap)
        toJSONMap.putAll(config.toJSONMap)
        nameAnnotations.addAll(config.nameAnnotations)
        ignoreAnnotations.addAll(config.ignoreAnnotations)
        return this
    }

    /**
     * Combine custom mappings from another `JSONConfig` into this one.
     *
     * @param   config  the other `JSONConfig`
     * @return          the `JSONConfig` object (for chaining)
     */
    fun combineMappings(config: JSONConfig): JSONConfig {
        fromJSONMap.putAll(config.fromJSONMap)
        toJSONMap.putAll(config.toJSONMap)
        return this
    }

    companion object {

        const val defaultBufferSize = DEFAULT_BUFFER_SIZE

        val defaultCharset = Charsets.UTF_8

        val defaultConfig = JSONConfig()

    }

}
