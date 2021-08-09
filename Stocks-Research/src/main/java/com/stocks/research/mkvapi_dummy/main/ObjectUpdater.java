package com.stocks.research.mkvapi_dummy.main;

import com.stocks.research.mkvapi.main.FieldMap;
import com.stocks.research.mkvapi.main.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ObjectUpdater<T> {
    private static final Logger log = LoggerFactory.getLogger(ObjectUpdater.class);

    protected Class<T> clazz;

    protected Map<String, Method> setters = new HashMap<>();

    String objectId;

    public String getObjectId() {
        return this.objectId;
    }

    public ObjectUpdater(Class<T> clazz) {
        this.clazz = clazz;
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("set") && method.getParameterCount() == 1) {
                method.setAccessible(true);
                this.setters.put(name.substring(3), method);
            }
        }
        if (this.setters.size() == 0)
            log.error("no setters found on " + clazz.getName());
    }

    public void updateField(T instance, String name, String valueStr) throws Exception {
        if (instance instanceof FieldMap) {
            Method method = instance.getClass().getMethod("putValue", new Class[] { String.class, Value.class });
            method.invoke(instance, new Object[] { name, getTypedValue(valueStr) });
        } else {
            Method setter = this.setters.get(name);
            if (setter == null)
                return;
            Class<?> paramTypes = setter.getParameterTypes()[0];
            Object value = toType(valueStr, paramTypes);
            setter.invoke(instance, new Object[] { value });
            if (name.equalsIgnoreCase("ID"))
                this.objectId = value.toString();
        }
    }

    private Value getTypedValue(String valueStr) {
        try {
            int value = Integer.parseInt(valueStr);
            return Value.of(Integer.class, Integer.valueOf(value));
        } catch (NumberFormatException ne) {
            try {
                double value = Double.parseDouble(valueStr);
                return Value.of(Double.class, Double.valueOf(value));
            } catch (NumberFormatException numberFormatException) {
                return Value.of(String.class, valueStr);
            }
        }
    }

    static Object toType(String valueStr, Class theType) {
        if (theType.equals(String.class))
            return valueStr;
        if (theType.equals(Integer.class) || theType.equals(int.class))
            return Integer.valueOf(parseInt(valueStr));
        if (theType.equals(Double.class) || theType.equals(double.class))
            return Double.valueOf(parseDouble(valueStr));
        if (theType.equals(Float.class) || theType.equals(float.class))
            return Double.valueOf(parseDouble(valueStr));
        if (theType.equals(Boolean.class) || theType.equals(boolean.class))
            return Boolean.valueOf((valueStr.equalsIgnoreCase("true") || valueStr.equals("1")));
        log.error("error: can't convert type [{}] valueStr=[{}]", theType, valueStr);
        throw new RuntimeException("can't convert type " + theType);
    }

    Map<String, Method> getSetters() {
        return this.setters;
    }

    private static int parseInt(String s) {
        if (s == null || s.length() == 0)
            return 0;
        if (s.contains(":")) {
            String t = s.replace(":", "").trim();
            log.info("converting ion time to integer [{}] [{}]", s, t);
            s = t;
        }
        return Integer.parseInt(s);
    }

    private static double parseDouble(String s) {
        if (s == null || s.length() == 0)
            return 0.0D;
        return Double.parseDouble(s);
    }
}
