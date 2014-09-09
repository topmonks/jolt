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
package com.bazaarvoice.jolt;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

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
        for (Entry<String, Object> inputEntry : input.entrySet()) {
            String inputKey = inputEntry.getKey();
            if (inputEntry.getValue() instanceof ArrayList) inputKey += "[]";
            if (s.containsKey(inputKey)) {
                Object specValue = s.get(inputKey);
                if (specValue instanceof Map) {
                    if (inputEntry.getValue() instanceof Map) {
                        walk((Map<String, Object>) inputEntry.getValue(), (Map<String, Object>) specValue, model);
                    } else if (inputEntry.getValue() instanceof ArrayList) {
                        ArrayList list = (ArrayList) inputEntry.getValue();
                        Map<String, Object> specMap = (Map<String, Object>) specValue;
                        for (Entry<String, Object> specEntry : specMap.entrySet()) {
                            if (specEntry.getKey().equals("*")) {
                                for (Object map : list) {
                                    walk((Map<String, Object>) map, (Map<String, Object>) specEntry.getValue(), model);
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
                    Object result = callFunction(func, inputEntry.getValue(), model, input);
                    inputEntry.setValue(result);
                }
            }
        }
    }

    private Object callFunction(String function, Object value, Map<String, Object> model, Map<String, Object> local) {
        Func func = new Func(function);
        Class<?>[] classes = null;
        try {
            classes = new Class[func.params.length + 1];
            for (int i = 0; i < classes.length; i++) {
                classes[i] = Object.class;
            }
            Object[] objects = new Object[func.params.length + 1];
            objects[0] = value;
            for (int i = 1; i < objects.length; i++) {
                String param = func.params[i - 1];
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
            Class<?> funcClass = Class.forName(func.className);
            Method funcMethod = funcClass.getMethod(func.methodName, classes);
            Object retval = funcMethod.invoke(null, objects);
            return retval;
        } catch (Throwable e) {
            StringBuilder sb = new StringBuilder();
            for (Class<?> clazz : classes) {
                if (sb.length() != 0) sb.append(", ");
                sb.append(clazz.getSimpleName());
            }
            throw new SpecException("Call function error - function='" + function + "'\n                                               expected function='" + func.className + "." + func.methodName + "(" + sb.toString() + ")'", e);
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

    private static class Func {
        private final String className;
        private final String methodName;
        private final String[] params;

        private Func(String function) {
            String f = function.replaceAll(" ", "");
            int leftParenthesis = f.indexOf("(");
            int rightParenthesis = f.indexOf(")");
            if (leftParenthesis != -1) {
                String p = f.substring(leftParenthesis + 1, rightParenthesis);
                params = p.split("\\,");
                f = f.substring(0, leftParenthesis);
            } else {
                params = new String[0];
            }
            int lastDot = f.lastIndexOf(".");
            className = f.substring(0, lastDot);
            methodName = f.substring(lastDot + 1);
        }

    }

}
