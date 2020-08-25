/*
 * @(#) JSONJava.java
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

package net.pwall.json;

import java.lang.reflect.Type;

import kotlin.jvm.functions.Function1;

/**
 * Adapter class to allow access to Kotlin serialization and deserialization from Java.
 *
 * @author  Peter Wall
 */
public class JSONJava {

    /**
     * Serialize an object to JSON, using the specified {@link JSONConfig}.
     *
     * @param   obj     the object
     * @param   config  a {@link JSONConfig} to customise the conversion
     * @return          the JSON form of the object
     */
    public static String stringify(Object obj, JSONConfig config) {
        return JSONAuto.INSTANCE.stringify(obj, config);
    }

    /**
     * Serialize an object to JSON, using the default {@link JSONConfig}.
     *
     * @param   obj     the object
     * @return          the JSON form of the object
     */
    public static String stringify(Object obj) {
        return stringify(obj, JSONConfig.Companion.getDefaultConfig());
    }

    /**
     * Deserialize JSON from string ({@link CharSequence}) to a specified Java {@link Class}, using the specified
     * {@link JSONConfig}.
     *
     * @param   javaClass   the target class
     * @param   str         the JSON in string form
     * @param   config      a {@link JSONConfig} to customise the conversion
     * @param   <T>         the target class
     * @return              the converted object
     */
    @SuppressWarnings("unchecked")
    public static <T> T parse(Class<T> javaClass, CharSequence str, JSONConfig config) {
        return (T)JSONAuto.INSTANCE.parse(javaClass, str, config);
    }

    /**
     * Deserialize JSON from string ({@link CharSequence}) to a specified Java {@link Class}, using the default
     * {@link JSONConfig}.
     *
     * @param   javaClass   the target class
     * @param   str         the JSON in string form
     * @param   <T>         the target class
     * @return              the converted object
     */
    public static <T> T parse(Class<T> javaClass, CharSequence str) {
        return parse(javaClass, str, JSONConfig.Companion.getDefaultConfig());
    }

    /**
     * Deserialize JSON from string ({@link CharSequence}) to a specified Java {@link Type}, using the specified
     * {@link JSONConfig}.
     *
     * @param   javaType    the target type
     * @param   str         the JSON in string form
     * @param   config      a {@link JSONConfig} to customise the conversion
     * @return              the converted object
     */
    public static Object parse(Type javaType, CharSequence str, JSONConfig config) {
        return JSONAuto.INSTANCE.parse(JSONFunKt.toKType(javaType, true), str, config);
    }

    /**
     * Deserialize JSON from string ({@link CharSequence}) to a specified Java {@link Type}, using the default
     * {@link JSONConfig}.
     *
     * @param   javaType    the target type
     * @param   str         the JSON in string form
     * @return              the converted object
     */
    public static Object parse(Type javaType, CharSequence str) {
        return parse(javaType, str, JSONConfig.Companion.getDefaultConfig());
    }

    @SuppressWarnings("unchecked")
    public static <T> void addToJSONMapping(JSONConfig config, Class<T> javaClass, ToJSONMapping<T> mapping) {
        config.toJSON(JSONFunKt.toKType(javaClass, false), (ToJSONMapping<Object>)mapping);
    }

    public static void addToJSONMapping(JSONConfig config, Type javaType, ToJSONMapping<Object> mapping) {
        config.toJSON(JSONFunKt.toKType(javaType, false), mapping);
    }

    public static <T> void addFromJSONMapping(JSONConfig config, Class<T> javaClass, FromJSONMapping<T> mapping) {
        config.fromJSON(JSONFunKt.toKType(javaClass, true), mapping);
    }

    public static void addFromJSONMapping(JSONConfig config, Type javaType, FromJSONMapping<?> mapping) {
        config.fromJSON(JSONFunKt.toKType(javaType, true), mapping);
    }

    @FunctionalInterface
    public interface ToJSONMapping<T> extends Function1<T, JSONValue> {}

    @FunctionalInterface
    public interface FromJSONMapping<T> extends Function1<JSONValue, T> {}

}
