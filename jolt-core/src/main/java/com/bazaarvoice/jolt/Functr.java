package com.bazaarvoice.jolt;

import java.lang.reflect.Method;
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

    @SuppressWarnings("unchecked")
    private void walk(Map<String, Object> input, Map<String, Object> s, Map<String, Object> model) {
        for (Entry<String, Object> inputEntry : input.entrySet()) {
            if (s.containsKey(inputEntry.getKey())) {
                Object specValue = s.get(inputEntry.getKey());
                if (specValue instanceof Map && inputEntry.getValue() instanceof Map) {
                    walk((Map<String, Object>) inputEntry.getValue(), (Map<String, Object>) specValue, model);
                } else if (specValue instanceof String) {
                    String func = (String) specValue;
                    if (func != null) {
                        int lasDot = func.lastIndexOf(".");
                        String className = func.substring(0, lasDot);
                        String methodName = func.substring(lasDot + 1);
                        try {
                            Class<?> funcClass = Class.forName(className);
                            Method funcMethod = funcClass.getMethod(methodName, Object.class, Map.class);
                            Object retval = funcMethod.invoke(null, inputEntry.getValue(), model);
                            inputEntry.setValue(retval);
                        } catch (Throwable e) {
                            throw new SpecException( "Call function error - class '" + className + "' and method with signature 'public static Object " + methodName +"(Object value, Map<String, Object> model)'", e);
                        }
                    }
                }
            }
        }
    }

}
