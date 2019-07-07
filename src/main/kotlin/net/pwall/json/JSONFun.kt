/*
 * @(#) JSONFun.kt
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

typealias JSONInt = JSONInteger

fun CharSequence.asJSON(): JSONValue = JSONString(this)

fun Int.asJSON(): JSONValue = JSONInt.valueOf(this)

fun Long.asJSON(): JSONValue = JSONLong.valueOf(this)

fun Float.asJSON(): JSONValue = JSONFloat.valueOf(this)

fun Double.asJSON(): JSONValue = JSONDouble.valueOf(this)

fun Boolean.asJSON(): JSONValue = JSONBoolean.valueOf(this)

infix fun String.toJSON(str: CharSequence?): Pair<String, JSONValue?> = this to str?.asJSON()

infix fun String.toJSON(i: Int): Pair<String, JSONValue?> = this to i.asJSON()

infix fun String.toJSON(i: Long): Pair<String, JSONValue?> = this to i.asJSON()

infix fun String.toJSON(f: Float): Pair<String, JSONValue?> = this to f.asJSON()

infix fun String.toJSON(d: Double): Pair<String, JSONValue?> = this to d.asJSON()

infix fun String.toJSON(b: Boolean): Pair<String, JSONValue?> = this to b.asJSON()

fun makeJSON(vararg pairs: Pair<String, JSONValue?>) = JSONObject().apply { putAll(pairs) }
