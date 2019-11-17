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
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSupertypeOf

import net.pwall.json.annotation.JSONIgnore
import net.pwall.json.annotation.JSONName

/**
 * Configuration class for JSON Auto serialize / deserialize for Kotlin.
 *
 * @author  Peter Wall
 */
class JSONConfig {

    /** Name of property to store sealed class subclass name as discriminator */
    var sealedClassDiscriminator = defaultSealedClassDiscriminator
        set(newValue) {
            if (newValue.isNotEmpty()) field = newValue else throw JSONException("Sealed class discriminator invalid")
        }

    /** Read buffer size (for `json-ktor`), arbitrarily limited to multiple of 16, not greater than 256K */
    var readBufferSize = defaultBufferSize
        set (newValue) {
            if ((newValue and 15) == 0 && newValue <= 256 * 1024)
                field = newValue
            else
                throw JSONException("Reda buffer size invalid - $newValue")
        }

    /** Character set (for `json-ktor' and  `json-ktor-client' */
    var charset = defaultCharset

    private val fromJSONMap: MutableMap<KType, FromJSONMapping> = LinkedHashMap()

    private val toJSONMap: MutableMap<KType, ToJSONMapping> = LinkedHashMap()

    private val nameAnnotations: MutableList<Pair<KClass<*>, KProperty.Getter<String>>> =
            arrayListOf(namePropertyPair(JSONName::class, "name"))

    private val ignoreAnnotations: MutableList<KClass<*>> = arrayListOf(JSONIgnore::class, Transient::class)

    /**
     * Find a `fromJSON` mapping function that will create the specified [KType], or the closest subtype of it.
     *
     * @param   type    the target type
     * @return          the mapping function, or `null` if not found
     */
    fun findFromJSONMapping(type: KType): FromJSONMapping? {
        var best: Map.Entry<KType, FromJSONMapping>? = null
        fromJSONMap.entries.forEach { entry ->
            if (entry.key.isSubtypeOf(type) && best.let { it == null || it.key.isSubtypeOf(entry.key) })
                best = entry
        }
        return best?.value
    }

    /**
     * Find a `fromJSON` mapping function that will create the specified [KClass], or the closest subclass of it.
     *
     * @param   targetClass the target class
     * @return              the mapping function, or `null` if not found
     */
    fun findFromJSONMapping(targetClass: KClass<*>): FromJSONMapping? {
        var best: KClass<*>? = null
        var nullable = false
        var result: FromJSONMapping? = null
        fromJSONMap.entries.forEach { entry ->
            val classifier = entry.key.classifier as KClass<*>
            if (classifier.isSubclassOf(targetClass) && best.let {
                        it == null ||
                                if (it == classifier) !nullable else it.isSubclassOf(classifier) }) {
                best = classifier
                nullable = entry.key.isMarkedNullable
                result = entry.value
            }
        }
        return result
    }

    /**
     * Find a `toJSON` mapping function that will accept the specified [KType], or the closest supertype of it.
     *
     * @param   type    the source type
     * @return          the mapping function, or `null` if not found
     */
    fun findToJSONMapping(type: KType): ToJSONMapping? {
        var best: Map.Entry<KType, ToJSONMapping>? = null
        toJSONMap.entries.forEach { entry ->
            if (entry.key.isSupertypeOf(type) && best.let { it == null || it.key.isSupertypeOf(entry.key) })
                best = entry
        }
        return best?.value
    }

    /**
     * Find a `toJSON` mapping function that will accept the specified [KClass], or the closest superclass of it.
     *
     * @param   sourceClass the source class
     * @return              the mapping function, or `null` if not found
     */
    fun findToJSONMapping(sourceClass: KClass<*>): ToJSONMapping? {
        var best: KClass<*>? = null
        var nullable = false
        var result: ToJSONMapping? = null
        toJSONMap.entries.forEach { entry ->
            val classifier = entry.key.classifier as KClass<*>
            if (classifier.isSuperclassOf(sourceClass) && best.let {
                        it == null ||
                                if (it == classifier) nullable else it.isSuperclassOf(classifier) }) {
                best = classifier
                nullable = entry.key.isMarkedNullable
                result = entry.value
            }
        }
        return result
    }

    /**
     * Add custom mapping from JSON to the specified type.
     *
     * @param   type    the target type
     * @param   mapping the mapping function
     */
    fun fromJSON(type: KType, mapping: FromJSONMapping) {
        fromJSONMap[type] = mapping
    }

    /**
     * Add custom mapping from JSON to the specified type, using a constructor that takes a single [String] parameter.
     *
     * @param   type    the target type
     */
    fun fromJSONString(type: KType) {
        fromJSONMap[type] = { json ->
            when (json) {
                null -> if (type.isMarkedNullable) null else throw JSONException("Can't deserialize null as $type")
                is JSONString -> {
                    val resultClass = type.classifier as? KClass<*> ?: throw JSONException("Can't deserialize $type")
                    val constructor = resultClass.constructors.find {
                        it.parameters.size == 1 && it.parameters[0].type == stringType
                    }
                    constructor?.call(json.toString()) ?: throw JSONException("Can't deserialize $type")
                }
                else -> throw JSONException("Can't deserialize ${json::class.simpleName} as $type")
            }
        }
    }

    /**
     * Add custom mapping from a specified type to JSON.
     *
     * @param   type    the source type
     * @param   mapping the mapping function
     */
    fun toJSON(type: KType, mapping: ToJSONMapping) {
        toJSONMap[type] = mapping
    }

    /**
     * Add custom mapping from a specified type to JSON using the `toString()` function to create a JSON string.
     *
     * @param   type    the source type
     */
    fun toJSONString(type: KType) {
        toJSONMap[type] = { obj -> obj?.let { JSONString(it.toString())} }
    }

    /**
     * Add custom mapping from JSON to the inferred type.
     *
     * @param   mapping the mapping function
     * @param   T       the type to be mapped
     */
    inline fun <reified T: Any> fromJSON(noinline mapping: (JSONValue?) -> T?) {
        fromJSON(T::class.createType(nullable = false), mapping)
    }

    /**
     * Add custom mapping from JSON to the inferred type, using a constructor that takes a single [String] parameter.
     *
     * @param   T       the type to be mapped
     */
    inline fun <reified T: Any> fromJSONString() {
        fromJSONString(T::class.createType(nullable = false))
    }

    /**
     * Add custom mapping from an inferred type to JSON.
     *
     * @param   mapping the mapping function
     * @param   T       the type to be mapped
     */
    inline fun <reified T: Any> toJSON(noinline mapping: (T?) -> JSONValue?) {
        toJSON(T::class.createType(nullable = true)) { mapping(it as T?) }
    }

    /**
     * Add custom mapping from an inferred type to JSON using the `toString()` function to create a JSON string.
     *
     * @param   T       the type to be mapped
     */
    inline fun <reified T: Any> toJSONString() {
        toJSONString(T::class.createType(nullable = true))
    }

    /**
     * Add an annotation specification to the list of annotations that specify the name to be used when serializing or
     * deserializing a property.
     *
     * @param   nameAnnotationClass the annotation class to specify the name
     * @param   argumentName        the name of the argument to the annotation that holds the name
     * @param   T                   the annotation class
     */
    fun <T: Annotation> addNameAnnotation(nameAnnotationClass: KClass<T>, argumentName: String) {
        nameAnnotations.add(namePropertyPair(nameAnnotationClass, argumentName))
    }

    private fun <T: Annotation> namePropertyPair(nameAnnotationClass: KClass<T>, argumentName: String):
            Pair<KClass<*>, KProperty.Getter<String>> {
        return nameAnnotationClass to findAnnotationStringProperty(nameAnnotationClass, argumentName).getter
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Annotation> findAnnotationStringProperty(annotationClass: KClass<T>, argumentName: String):
            KProperty<String> {
        annotationClass.members.forEach {
            if (it is KProperty<*> && it.name == argumentName && it.returnType == stringType) {
                return it as KProperty<String>
            }
        }
        throw IllegalArgumentException(
                "Annotation class ${annotationClass.simpleName} does not have a String property \"$argumentName\"")
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
     */
    fun <T: Annotation> addIgnoreAnnotation(ignoreAnnotationClass: KClass<T>) {
        ignoreAnnotations.add(ignoreAnnotationClass)
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
     */
    fun combineAll(config: JSONConfig) {
        sealedClassDiscriminator = config.sealedClassDiscriminator
        readBufferSize = config.readBufferSize
        charset = config.charset
        fromJSONMap.putAll(config.fromJSONMap)
        toJSONMap.putAll(config.toJSONMap)
        nameAnnotations.addAll(config.nameAnnotations)
        ignoreAnnotations.addAll(config.ignoreAnnotations)
    }

    /**
     * Combine custom mappings from another `JSONConfig` into this one.
     *
     * @param   config  the other `JSONConfig`
     */
    fun combineMappings(config: JSONConfig) {
        fromJSONMap.putAll(config.fromJSONMap)
        toJSONMap.putAll(config.toJSONMap)
    }

    companion object {

        val stringType = String::class.createType()

        const val defaultSealedClassDiscriminator = "class"

        const val defaultBufferSize = DEFAULT_BUFFER_SIZE

        val defaultCharset = Charsets.UTF_8

        val defaultConfig = JSONConfig()

    }

}
