package org.example.model.rdfs;

import java.lang.reflect.Field;

public enum NameExtractors implements NameExtractor {
    NAME_FIELD(obj -> {
        try {
            Field field = obj.getClass().getDeclaredField("name");
            boolean accessible = field.canAccess(obj);
            field.setAccessible(true);
            String value = (String) field.get(obj);
            field.setAccessible(accessible);
            value = value.replaceAll("[^\\p{L}\\p{N} ]", "");
            if(field.isAnnotationPresent(Replace.class)){
               value = value.replace(" ", field.getAnnotation(Replace.class).value());
            }
            return value;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    });

    private final NameExtractor delegate;

    NameExtractors(NameExtractor delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName(Object obj) {
        return delegate.getName(obj);
    }
}
