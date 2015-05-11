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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.bazaarvoice.jolt.exception.SpecException;

/**
 * @author Michal Nikodim (michal.nikodim@topmonks.com)
 */
public class Functr implements SpecDriven, Transform {

    public static final String ROOT_KEY = "root";
    public static final Object DONT_SET_RETURN_VALUE = new Object();

    private static Pattern isNumber = Pattern.compile("^[-|+]?[0-9]*\\.?[0-9]*$");
    private static Pattern patternExtractArrayIndex = Pattern.compile("\\[([0-9]+)\\]$");

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

    @Override
    public Object transform(Object input) {
        if (input == null) input = new HashMap<String, Object>();
        walk(input, spec, input);
        return input;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void walk(Object level, Map<String, Object> spec, Object input) {
        if (level instanceof List) {
            List<Object> list = (List<Object>) level;
            for (int i = 0; i < list.size(); i++) {
                Object subSpec = spec.get(String.valueOf(i));
                if (subSpec != null) {
                    if (subSpec instanceof String) {
                        Object retval = callFunction((String) subSpec, list.get(i), list, input);
                        if (retval != DONT_SET_RETURN_VALUE) list.set(i, retval);
                    } else if (subSpec instanceof Map) {
                        walk(list.get(i), (Map<String, Object>) subSpec, input);
                    }
                } else {
                    subSpec = spec.get("*");
                    if (subSpec != null) {
                        if (subSpec instanceof String) {
                            Object retval = callFunction((String) subSpec, list.get(i), list, input);
                            if (retval != DONT_SET_RETURN_VALUE) list.set(i, retval);
                        } else if (subSpec instanceof Map) {
                            walk(list.get(i), (Map<String, Object>) subSpec, input);
                        }
                    }
                }
            }
        } else if (level instanceof Map) {
            for (Entry<String, Object> specEntry : spec.entrySet()) {
                String specKey = specEntry.getKey();
                if (specKey.endsWith("[]")) specKey = specKey.substring(0, specKey.length() - 2);
                Object subLevel = ((Map) level).get(specKey);
                if (subLevel != null) {
                    if (specEntry.getValue() instanceof String) {
                        Object retval = callFunction((String) specEntry.getValue(), subLevel, level, input);
                        if (retval != DONT_SET_RETURN_VALUE) ((Map) level).put(specKey, retval);
                    } else if (specEntry.getValue() instanceof Map) {
                        walk(subLevel, (Map<String, Object>) specEntry.getValue(), input);
                    }
                }
            }
        }
    }

    private Object callFunction(String function, Object value, Object level, Object input) {
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
                    objects[i] = level;
                } else if ("#map".equalsIgnoreCase(param)) {
                    objects[i] = input;
                } else if (isNumber.matcher(param).find()) {
                    objects[i] = new BigDecimal(param);
                } else if (param.startsWith("'") && param.endsWith("'")) {
                    objects[i] = param.substring(1, param.length() - 1);
                } else {
                    Object data = null;
                    if (param.startsWith(".")) {
                        param = param.substring(1);
                        data = level;
                    } else {
                        data = input;
                    }
                    objects[i] = findValue(param.split("\\."), 0, data);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    Object findValue(String[] tokens, int index, Object data) {
        boolean last = index == tokens.length - 1;
        String token = tokens[index];
        if (data instanceof List) {
            List<Object> list = (List<Object>) data;
            int i = extractArrayIndex(token);
            if (i != -1 && list.size() > i) {
                if (last) {
                    return list.get(i);
                } else {
                    return findValue(tokens, index + 1, list.get(i));
                }
            }
        } else if (data instanceof Map) {
            Object subData = ((Map) data).get(token);
            if (subData != null) {
                if (last) {
                    return subData;
                } else {
                    return findValue(tokens, index + 1, subData);
                }
            }
        }
        return null;
    }

    private int extractArrayIndex(String token) {
        Matcher matcher = patternExtractArrayIndex.matcher(token);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

}