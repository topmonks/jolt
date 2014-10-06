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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import com.bazaarvoice.jolt.exception.SpecException;

/**
 * @author Michal Nikodim (michal.nikodim@topmonks.com)
 */
public class Functr implements SpecDriven, Transform {

    public static final String ROOT_KEY = "root";
    private final Map<String, Object> spec;

    @SuppressWarnings("unchecked")
    @Inject
    public Functr(Object spec) {
        if (spec == null) {
            throw new SpecException("Functr expected a spec of Map type, got 'null'.");
        }
        if (!(spec instanceof Map)) {
            throw new SpecException("Functr expected a spec of Map type, got " + spec.getClass().getSimpleName());
        }
        this.spec = (Map<String, Object>) spec;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object transform(Object object) {
        Map<String, Object> input = (Map<String, Object>) object;
        walk(input, spec, input);
        return input;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void walk(Map<String, Object> input, Map<String, Object> s, Map<String, Object> model) {
        Set<String> keys = new LinkedHashSet<String>(input.keySet());
        for (String key : keys) {
            Object inputValue = input.get(key);
            if (inputValue instanceof ArrayList) key += "[]";
            if (s.containsKey(key)) {
                Object specValue = s.get(key);
                if (specValue instanceof Map) {
                    if (inputValue instanceof Map) {
                        walk((Map<String, Object>) inputValue, (Map<String, Object>) specValue, model);
                    } else if (inputValue instanceof ArrayList) {
                        ArrayList list = (ArrayList) inputValue;
                        Map<String, Object> specMap = (Map<String, Object>) specValue;
                        for (Entry<String, Object> specEntry : specMap.entrySet()) {
                            if (specEntry.getKey().equals("*")) {
                                for (Object map : list) {
                                   if (map instanceof Map) walk((Map<String, Object>) map, (Map<String, Object>) specEntry.getValue(), model);
                                }
                            } else {
                                try {
                                    Integer index = Integer.parseInt(specEntry.getKey());
                                    walk((Map<String, Object>) list.get(index), (Map<String, Object>) specEntry.getValue(), model);
                                } catch (NumberFormatException e) {
                                    throw new SpecException("In array is allowed only \"*\" or \"[number]\"");
                                }
                            }
                        }
                    }
                } else if (specValue instanceof String) {
                    String func = (String) specValue;
                    Object result = callFunction(func, inputValue, model, input);
                    input.put(key, result);
                }
            }
        }
    }

    private Object callFunction(String function, Object value, Map<String, Object> model, Map<String, Object> local) {
        FuncParser funcParser = new FuncParser(function);
        Class<?>[] classes = null;
        try {
            classes = new Class[funcParser.getParams().length + 1];
            for (int i = 0; i < classes.length; i++) {
                classes[i] = Object.class;
            }
            Object[] objects = new Object[funcParser.getParams().length + 1];
            objects[0] = value;
            for (int i = 1; i < objects.length; i++) {
                String param = funcParser.getParams()[i - 1];
                if ("#localMap".equalsIgnoreCase(param)) {
                    objects[i] = local;
                } else if ("#map".equalsIgnoreCase(param)) {
                    objects[i] = model;
                } else {
                    String[] extracted = null;
                    Map<String, Object> map = null;
                    if (param.startsWith(".")) {
                        extracted = param.substring(1).split("\\.");
                        map = local;
                    } else {
                        extracted = param.split("\\.");
                        map = model;
                    }
                    objects[i] = findValue(extracted, map);
                }
            }
            Class<?> funcClass = Class.forName(funcParser.getClassName());
            Method funcMethod = funcClass.getMethod(funcParser.getMethodName(), classes);
            Object retval = funcMethod.invoke(null, objects);
            return retval;
        } catch (Throwable e) {
            StringBuilder sb = new StringBuilder();
            for (Class<?> clazz : classes) {
                if (sb.length() != 0) sb.append(", ");
                sb.append(clazz.getSimpleName());
            }
            throw new SpecException("Call function error - function='" + function + "'\n                                               expected function='" + funcParser.getClassName() + "." + funcParser.getMethodName() + "(" + sb.toString() + ")'", e);
        }

    }

    @SuppressWarnings("unchecked")
    private Object findValue(String[] extracted, Map<String, Object> model) {
        Map<String, Object> m = model;
        for (int i = 0; i < extracted.length - 1; i++) {
            String token = extracted[i];
            m = (Map<String, Object>) m.get(token);
            if (m == null) throw new SpecException("Wrong param path '" + token + "'");
        }
        Object value = m.get(extracted[extracted.length - 1]);
        if (value == null) throw new SpecException("Wrong param path '" + extracted[extracted.length - 1] + "'");
        return value;
    }
    

}
