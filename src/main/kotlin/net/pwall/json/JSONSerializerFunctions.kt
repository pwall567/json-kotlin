/*
 * @(#) JSONSerializerFunctions.kt
 *
 * json-kotlin Kotlin JSON Auto Serialize/deserialize
 * Copyright (c) 2019, 2020, 2022 Peter Wall
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
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.MonthDay
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Period
import java.time.Year
import java.time.YearMonth
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.LinkedList
import java.util.UUID

import net.pwall.util.ISO8601Date

/**
 * Utility functions for JSON Serialization.  These functions are not expected to be of use outside the `json-kotlin`
 * family of projects.
 *
 * @author  Peter Wall
 */
object JSONSerializerFunctions {

    private val toJsonCache = HashMap<KClass<*>, KFunction<JSONValue>?>()

    private val toStringClasses = setOf(java.sql.Date::class, java.sql.Time::class, java.sql.Timestamp::class,
            Instant::class, LocalDate::class, LocalDateTime::class, LocalTime::class, OffsetTime::class,
            OffsetDateTime::class, ZonedDateTime::class, Year::class, YearMonth::class, MonthDay::class,
            Duration::class, Period::class, URI::class, URL::class, UUID::class)

    private val uncachedClasses = setOf(Any::class, String::class, Boolean::class, Int::class, Long::class, Byte::class,
            Short::class, Double::class, Float::class, BigDecimal::class, BigInteger::class, ArrayList::class,
            LinkedList::class, HashMap::class, LinkedHashMap::class, HashSet::class)

    /**
     * Is the class best represented by a string of the `toString()` result?
     *
     * @receiver            the class of the object
     * @return              `true` if the object should be output as a string
     */
    fun KClass<*>.isToStringClass() = this in toStringClasses

    fun KClass<*>.findToJSON(): KFunction<JSONValue>? {
        if (this in uncachedClasses || this in toStringClasses)
            return null
        if (toJsonCache.containsKey(this))
            return toJsonCache[this]
        try {
            for (function in members) {
                if (function is KFunction<*> &&
                        function.name == "toJSON" &&
                        function.parameters.size == 1 &&
                        function.parameters[0].kind == KParameter.Kind.INSTANCE &&
                        function.returnType.isJSONValue())
                    @Suppress("UNCHECKED_CAST")
                    return (function as KFunction<JSONValue>).apply { toJsonCache[this@findToJSON] = this }
            }
        }
        catch (_: Throwable) {
        }
        toJsonCache[this] = null
        return null
    }

    private fun KType.isJSONValue(): Boolean {
        classifier?.let { if (it is KClass<*>) return it.isSubclassOf(JSONValue::class) }
        return false
    }

    fun KClass<*>.isSealedSubclass(): Boolean {
        for (supertype in supertypes) {
            (supertype.classifier as? KClass<*>)?.let {
                if (it.isSealed)
                    return true
                if (it != Any::class)
                    if (it.isSealedSubclass())
                        return true
            }
        }
        return false
    }

    fun Calendar.formatISO8601() : String = ISO8601Date.toString(this, true,
            ISO8601Date.YEAR_MASK or ISO8601Date.MONTH_MASK or ISO8601Date.DAY_OF_MONTH_MASK or
                    ISO8601Date.HOUR_OF_DAY_MASK or ISO8601Date.MINUTE_MASK or ISO8601Date.SECOND_MASK or
                    ISO8601Date.MILLISECOND_MASK or ISO8601Date.ZONE_OFFSET_MASK)

}
