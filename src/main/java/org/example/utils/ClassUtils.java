package org.example.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ClassUtils {


    public static Class<?> getItemType(Field fieldType) {
        if (fieldType.getGenericType() instanceof ParameterizedType parameterizedType) {
            Type[] actualTypes = parameterizedType.getActualTypeArguments();
            return (Class<?>) actualTypes[0];
        }
        throw new IllegalArgumentException();
    }
}
