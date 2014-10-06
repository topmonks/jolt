/*
 * Copyright 2014 Topmonks, s r.o.
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
package com.bazaarvoice.jolt.substr;

import java.util.Map;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

/**
 * @author Michal Nikodim (michal.nikodim@topmonks.com)
 */
public class SubsTest {

    private String input_1 ="{ " +
                               "\"account\":{"+
                                  "\"iban\":\"A1\",\"bic\":\"B1\",\"som\":\"S1\","+
                                  "\"array\":[{\"iban\":\"A2\"},{\"bic\":\"B2\"},{\"iban\":\"A3\",\"bic\": \"B3\"}]"+
                               "},"+
                               "\"iban\":\"A5\""+
                            "}";
    
    private String spec_1 = "["+ 
            "{"+
               "\"operation\": \"subs\","+
               "\"spec\": {"+
                   "\"function\":\"com.bazaarvoice.jolt.substr.SubsTest.subsTest\"," +
                   "\"attrs\": {"+
                       "\"iban\": \"som\","+
                       "\"bic\": \"subst_bic\""+
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
    
    private String expected = "{\"account\":{\"iban\":\"A1\",\"bic\":\"B1\",\"som\":\"subbed_A1\",\"array\":[{\"iban\":\"A2\",\"som\":\"subbed_A2\"},{\"bic\":\"B2\",\"subst_bic\":\"subbed_B2\"},{\"iban\":\"A3\",\"bic\":\"B3\",\"som\":\"subbed_A3\",\"subst_bic\":\"subbed_B3\"}],\"subst_bic\":\"subbed_B1\"},\"iban\":\"A5\",\"som\":\"subbed_A5\"}";

    
    
    @SuppressWarnings("unchecked")
    @Test
    public void testInput1Spec1(){
        Object spec = JsonUtils.jsonToList(spec_1);
        Chainr chainr = Chainr.fromSpec(spec);
        Object input = JsonUtils.jsonToObject(input_1);
        Map<String, Object> transformed = (Map<String, Object>) chainr.transform(input);
        String result = JsonUtils.toJsonString(transformed);
        Assert.assertEquals(expected, result);
    }
    
    public static void subsTest(String srcName, String dstName, Map<String, Object> map){
        String value = (String) map.get(srcName);
        map.put(dstName, "subbed_" + value);
        
    }
}
