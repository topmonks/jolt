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
package com.bazaarvoice.jolt;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.bazaarvoice.jolt.exception.SpecException;

/**
 * @author Michal Nikodim (michal.nikodim@topmonks.com)
 */
public class Substr implements SpecDriven, Transform {

    private Method funcMethod;
    private Map<String, String> attrs;

    @SuppressWarnings("unchecked")
    @Inject
    public Substr(Object spec) throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        if (spec == null) {
            throw new SpecException("Functr expected a spec of Map type, got 'null'.");
        }
        if (!(spec instanceof Map)) {
            throw new SpecException("Functr expected a spec of Map type, got " + spec.getClass().getSimpleName());
        }
        Map<String, Object> map = (Map<String, Object>) spec;
        Map<String, Object> attrsMap = (Map<String, Object>) map.get("attrs");
        FuncParser funcParser = new FuncParser((String) map.get("function"));

        Class<?> funcClass = Class.forName(funcParser.getClassName());
        funcMethod = funcClass.getMethod(funcParser.getMethodName(), new Class[] { String.class, String.class, Map.class });
        attrs = new HashMap<String, String>();
        for (Entry<String, Object> entry : attrsMap.entrySet()) {
            attrs.put(entry.getKey(), (String) entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object transform(Object input) {
        walk((Map<String, Object>) input);
        return input;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void walk(Map<String, Object> input) {
        Set<String> keys = new LinkedHashSet<String>(input.keySet());
        for (String key : keys) {
            Object inputValue = input.get(key);
            if (inputValue instanceof Map) {
                walk((Map<String, Object>) inputValue);
            } else if (inputValue instanceof ArrayList) {
                ArrayList list = (ArrayList) inputValue;
                for (Object map : list) {
                    walk((Map<String, Object>) map);
                }
            } else {
                String dstName = attrs.get(key);
                if (dstName != null) {
                    callFunction(key, dstName, input);
                }
            }
        }
    }

    private void callFunction(String srcName, String dstName, Map<String, Object> local) {
        try {
            Object[] objects = new Object[] { srcName, dstName, local };
            funcMethod.invoke(null, objects);
        } catch (Throwable e) {
            throw new SpecException("callFunction error", e);
        }
    }

}
