/*
 * @(#) DummyClasses.kt
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

import net.pwall.json.annotation.JSONIgnore
import net.pwall.json.annotation.JSONName

data class Dummy1(val field1: String, val field2: Int = 999)

data class Dummy2(val field1: String, val field2: Int = 999) {
    var extra: String? = null
}

data class Dummy3(val dummy1: Dummy1, val text: String)

data class Dummy4(val listDummy1: List<Dummy1>, val text: String)

data class Dummy5(val field1: String, @JSONIgnore val field2: String, val field3: String)

data class DummyFromJSON(val int1: Int) {

    @Suppress("unused")
    fun toJSON(): JSONObject {
        return JSONObject.create().putValue("dec", int1.toString()).putValue("hex", int1.toString(16))
    }

    companion object {
        @Suppress("unused")
        fun fromJSON(json: JSONValue): DummyFromJSON {
            val jsonObject = json as JSONObject
            val dec = jsonObject.getString("dec").toInt()
            val hex = jsonObject.getString("hex").toInt(16)
            if (dec != hex)
                throw JSONException("Inconsistent values")
            return DummyFromJSON(dec)
        }
    }

}

enum class DummyEnum { ALPHA, BETA, GAMMA }

open class Super {

    var field1: String = "xxx"
    var field2: Int = 111

    override fun equals(other: Any?): Boolean {
        return other is Super && field1 == other.field1 && field2 == other.field2
    }

    override fun hashCode(): Int {
        return field1.hashCode() xor field2.hashCode()
    }

}

class Derived : Super() {

    var field3: Double = 0.1

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && other is Derived && field3 == other.field3
    }

    override fun hashCode(): Int {
        return super.hashCode() xor field3.toInt()
    }

}

class DummyAnno {

    var field1: String = "xxx"
    @JSONName("fieldX")
    var field2: Int = 111

    override fun equals(other: Any?): Boolean {
        return other is DummyAnno && field1 == other.field1 && field2 == other.field2
    }

    override fun hashCode(): Int {
        return field1.hashCode() xor field2.hashCode()
    }

}

data class DummyAnnoData(val field1: String, @JSONName("fieldX") val field2: Int)

interface DummyInterface

object DummyObject : DummyInterface {

    val field1: String = "abc"

}

class NestedDummy {

    val obj = DummyObject

}

class DummyWithVal {

    val field8: String = "blert"

    override fun equals(other: Any?): Boolean {
        return other is DummyWithVal && other.field8 == field8
    }

    override fun hashCode(): Int {
        return field8.hashCode()
    }

}
