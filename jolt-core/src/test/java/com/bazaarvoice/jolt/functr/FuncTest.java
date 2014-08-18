package com.bazaarvoice.jolt.functr;

import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.bazaarvoice.jolt.Chainr;
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
                   "\"value\": \"com.bazaarvoice.jolt.functr.FuncTest.transform\""+
               "}"+
            "}"+
           "]";
    
    private String spec_2 = "["+ 
            "{"+
               "\"operation\": \"func\","+
               "\"spec\": {"+
                 "\"amount\":{" +
                   "\"value\": \"com.bazaarvoice.jolt.functr.FuncTest.transform\""+
                 "}" +
               "}"+
            "}"+
           "]";   
    
    
    
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
    
    
    @SuppressWarnings("unchecked")
    public static Object transform(Object value, Map<String, Object> model){
        String val = String.valueOf(value);
        int dot = val.indexOf(".");
        int prec = dot == -1 ? prec = 0 : val.length() - 1 - dot;
        int want = 0;
        if (model.containsKey("amount")) {
            want = (Integer) ((Map<String, Object>) model.get("amount")).get("precision");   
        } else {
            want = (Integer) model.get("precision");
        }
        if (want > prec) {
            val = val + "000000000000000000000000000000".substring(0, want - prec);
        } else if (want < prec) {
            val = val.substring(0, val.length() - (prec - want));
        }
        val = val.replaceAll("\\.", "");
        return Integer.valueOf(val);    
    }
    
}
