/*
 * @(#) JSONJavaTest.java
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
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JSONJavaTest {

    private static final String json = "{\"field1\":123,\"field2\":\"abcdef\"}";
    private static final String complexJson =
            "[{\"field1\":123,\"field2\":\"abcdef\"},{\"field1\":987,\"field2\":\"hello\"}]";

    @Test
    public void testJSONJavaStringify() {
        JavaClass1 javaClass1 = new JavaClass1(123, "abcdef");
        assertEquals(json, JSONJava.stringify(javaClass1));
    }

    @Test
    public void testJSONJavaStringifyOfKotlinClass() {
        Dummy1 dummy1 = new Dummy1("abcdefghi", 9876543);
        String expected = "{\"field1\":\"abcdefghi\",\"field2\":9876543}";
        assertEquals(expected, JSONJava.stringify(dummy1));
    }

    @Test
    public void testJSONJavaParseUsingClass() {
        JavaClass1 javaClass1 = new JavaClass1(123, "abcdef");
        assertEquals(javaClass1, JSONJava.parse(JavaClass1.class, json));
    }

    @Test
    public void testJSONJavaParseOfKotlinClass() {
        String jsonDummy1 = "{\"field1\":\"abcdefghi\",\"field2\":9876543}";
        Dummy1 expected = new Dummy1("abcdefghi", 9876543);
        assertEquals(expected, JSONJava.parse(Dummy1.class, jsonDummy1));
    }

    @Test
    public void testJSONJavaParseUsingType() throws Exception {
        List<JavaClass1> javaClass1List = new ArrayList<>();
        javaClass1List.add(new JavaClass1(123, "abcdef"));
        javaClass1List.add(new JavaClass1(987, "hello"));
        Type type = JavaClass2.class.getField("field1").getGenericType();
        assertEquals(javaClass1List, JSONJava.parse(type, complexJson));
    }

    @Test
    public void testAddToJSONMapping() {
        JSONConfig config = new JSONConfig();
        JSONJava.addToJSONMapping(config, Dummy1.class, new Dummy1ToJSON());
        JSONObject expected = JSONObject.create().putValue("a", "xyz").putValue("b", 888);
        assertEquals(expected, JSONSerializer.INSTANCE.serialize(new Dummy1("xyz", 888), config));
    }

    @Test
    public void testAddFromJSONMapping() {
        JSONConfig config = new JSONConfig();
        JSONJava.addFromJSONMapping(config, Dummy1.class, new Dummy1FromJSON());
        Dummy1 expected = new Dummy1("xyz", 888);
        JSONObject json = JSONObject.create().putValue("a", "xyz").putValue("b", 888);
        assertEquals(expected, JSONDeserializer.INSTANCE.deserialize(json, config));
    }

    public static class Dummy1ToJSON implements JSONJava.ToJSONMapping<Dummy1> {

        @Override
        public JSONValue invoke(Dummy1 obj) {
            return obj == null ? null :
                    JSONObject.create().putValue("a", obj.getField1()).putValue("b", obj.getField2());
        }

    }

    public static class Dummy1FromJSON implements JSONJava.FromJSONMapping<Dummy1> {

        @Override
        public Dummy1 invoke(JSONValue json) {
            if (json == null)
                return null;
            if (!(json instanceof JSONObject))
                fail();
            JSONObject jsonObject = (JSONObject)json;
            return new Dummy1(jsonObject.getString("a"), jsonObject.getInt("b"));
        }

    }

}
