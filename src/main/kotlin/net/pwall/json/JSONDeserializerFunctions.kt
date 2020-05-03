package net.pwall.json

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSuperclassOf

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

}
