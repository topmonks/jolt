/*
 * Copyright 2013 Topmonks, s r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bazaarvoice.jolt.functr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.Functr;
import com.bazaarvoice.jolt.JsonUtils;

/**
 * @author Michal Nikodim (michal.nikodim@topmonks.com)
 */
public class FuncTest {

    private String input_1 ="{" +
            "\"amount\": {" +
            "\"value\": 1234.5,"+
            "\"precision\": 3"+
            "}"+
            "}";
    private String input_2 ="{" +
            "\"amount\": {" +
            "\"value\": 1234.5678,"+
            "\"precision\": 4"+
            "}"+
            "}";
    private String input_3 ="{" +
            "\"amount\": {" +
            "\"value\": 1234.5678,"+
            "\"precision\": 2"+
            "}"+
            "}";
    private String input_4 ="{" +
            "\"amount\": {" +
            "\"value\": 1234,"+
            "\"precision\": 2"+
            "}"+
            "}";

    private String spec_1 = "["+
            "{"+
            "\"operation\": \"shift\","+
            "\"spec\": {"+
            "\"amount\": {"+
            "\"*\": \"&0\""+
            "}"+
            "}"+
            "},"+
            "{"+
            "\"operation\": \"func\","+
            "\"spec\": {"+
            "\"value\": \"com.bazaarvoice.jolt.functr.FuncTest.transform(.precision)\""+
            "}"+
            "}"+
            "]";

    private String spec_2 = "["+
            "{"+
            "\"operation\": \"func\","+
            "\"spec\": {"+
            "\"amount\":{" +
            "\"value\": \"com.bazaarvoice.jolt.functr.FuncTest.transform(.precision)\""+
            "}" +
            "}"+
            "}"+
            "]";

    private String inputArray1 = "[ [\"neco\",\"tady\",\"je\"] ]";
    private String specArray1 = "["+
            "{"+
            "\"operation\": \"func\","+
            "\"spec\": {"+
            "\"*\":{" +
            "\"*\": \"com.bazaarvoice.jolt.functr.FuncTest.test\""+
            "}" +
            "}"+
            "}"+
            "]";

    @SuppressWarnings("unchecked")
    @Test
    public void testInputArray1(){
        Object spec = JsonUtils.jsonToList(specArray1);
        Chainr chainr = Chainr.fromSpec(spec);
        Object input = JsonUtils.jsonToObject(inputArray1);
        List<Object> transformed = (List<Object>) chainr.transform(input);
        List<Object> subArray = (List<Object>) transformed.get(0);
        for (Object object : subArray) {
            AssertJUnit.assertEquals("TEST", object);
        }
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testInput1Spec1(){
        Object spec = JsonUtils.jsonToList(spec_1);
        Chainr chainr = Chainr.fromSpec(spec);
        Object input = JsonUtils.jsonToObject(input_1);
        Map<String, Object> transformed = (Map<String, Object>) chainr.transform(input);
        int value = (Integer) transformed.get("value");
        AssertJUnit.assertEquals(1234500, value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInput2Spec1(){
        Object spec = JsonUtils.jsonToList(spec_1);
        Chainr chainr = Chainr.fromSpec(spec);
        Object input = JsonUtils.jsonToObject(input_2);
        Map<String, Object> transformed = (Map<String, Object>) chainr.transform(input);
        int value = (Integer) transformed.get("value");
        AssertJUnit.assertEquals(12345678, value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInput3Spec1(){
        Object spec = JsonUtils.jsonToList(spec_1);
        Chainr chainr = Chainr.fromSpec(spec);
        Object input = JsonUtils.jsonToObject(input_3);
        Map<String, Object> transformed = (Map<String, Object>) chainr.transform(input);
        int value = (Integer) transformed.get("value");
        AssertJUnit.assertEquals(123456, value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInput4Spec1(){
        Object spec = JsonUtils.jsonToList(spec_1);
        Chainr chainr = Chainr.fromSpec(spec);
        Object input = JsonUtils.jsonToObject(input_4);
        Map<String, Object> transformed = (Map<String, Object>) chainr.transform(input);
        int value = (Integer) transformed.get("value");
        AssertJUnit.assertEquals(123400, value);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testInput1Spec2(){
        Object spec = JsonUtils.jsonToList(spec_2);
        Chainr chainr = Chainr.fromSpec(spec);
        Object input = JsonUtils.jsonToObject(input_1);
        Map<String, Object> transformed = (Map<String, Object>) chainr.transform(input);
        int value = (Integer) ((Map<String, Object>) transformed.get("amount")).get("value");
        AssertJUnit.assertEquals(1234500, value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInput2Spec2(){
        Object spec = JsonUtils.jsonToList(spec_2);
        Chainr chainr = Chainr.fromSpec(spec);
        Object input = JsonUtils.jsonToObject(input_2);
        Map<String, Object> transformed = (Map<String, Object>) chainr.transform(input);
        int value = (Integer) ((Map<String, Object>) transformed.get("amount")).get("value");
        AssertJUnit.assertEquals(12345678, value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInput3Spec2(){
        Object spec = JsonUtils.jsonToList(spec_2);
        Chainr chainr = Chainr.fromSpec(spec);
        Object input = JsonUtils.jsonToObject(input_3);
        Map<String, Object> transformed = (Map<String, Object>) chainr.transform(input);
        int value = (Integer) ((Map<String, Object>) transformed.get("amount")).get("value");
        AssertJUnit.assertEquals(123456, value);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInput4Spec2(){
        Object spec = JsonUtils.jsonToList(spec_2);
        Chainr chainr = Chainr.fromSpec(spec);
        Object input = JsonUtils.jsonToObject(input_4);
        Map<String, Object> transformed = (Map<String, Object>) chainr.transform(input);
        int value = (Integer) ((Map<String, Object>) transformed.get("amount")).get("value");
        AssertJUnit.assertEquals(123400, value);
    }

    public static Object transform(Object value, Object precision){
        String val = String.valueOf(value);
        int wantPrec = (Integer) precision;
        int dot = val.indexOf(".");
        int prec = dot == -1 ? prec = 0 : val.length() - 1 - dot;
        if (wantPrec > prec) {
            val = val + "000000000000000000000000000000".substring(0, wantPrec - prec);
        } else if (wantPrec < prec) {
            val = val.substring(0, val.length() - (prec - wantPrec));
        }
        val = val.replaceAll("\\.", "");
        return Integer.valueOf(val);
    }

    public static Object test(Object value){
        return "TEST";
    }

}