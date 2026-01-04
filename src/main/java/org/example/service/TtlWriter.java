package org.example.service;

import org.example.model.rdfs.NameExtractor;
import org.example.model.rdfs.Range;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.example.utils.ClassUtils.getItemType;

public class TtlWriter {
    private static final List<Class<?>> DEFAULT_LITERAL_TYPES = Arrays.asList(
            String.class,
            Integer.class,
            int.class,
            Double.class,
            double.class,
            Long.class,
            long.class
    );

    public static void writeRdfsModel(Class<?> clazz, List<?> objects, String prefix, String prefixFullValue,  NameExtractor nameExtractor, Writer writer) throws IOException, IllegalAccessException {
        writeRdfsModel(clazz, prefix, prefixFullValue, writer);
        int i = 0;
        for (Object object : objects) {
            writer.write(String.format("%s:%d a %s:%s .\n", prefix, ++i, prefix, clazz.getSimpleName()));
            for(Field field : clazz.getDeclaredFields()) {
                boolean accessible = field.canAccess(object);
                field.setAccessible(true);
                Object value = field.get(object);
                field.setAccessible(accessible);
                if(value!=null) {
                    if (field.getType().equals(List.class)) {
                        for (Object item : (List<?>) value) {
                            writeRdfsProperty(
                                    String.format("%s:%d", prefix, i),
                                    String.format("%s:%s", prefix, field.getName()),
                                    item,
                                    nameExtractor,
                                    writer
                            );
                        }
                    } else {
                        writeRdfsProperty(
                                String.format("%s:%d", prefix, i),
                                String.format("%s:%s", prefix, field.getName()),
                                value,
                                nameExtractor,
                                writer
                        );
                    }
                }
            }
        }
        writer.flush();
    }

    private static void writeRdfsProperty(String subject, String predicate, Object value, NameExtractor nameExtractor, Writer writer) throws IOException {
        String serializedValue;
        if(!DEFAULT_LITERAL_TYPES.contains(value.getClass())) {
            serializedValue = String.format(":%s", nameExtractor.getName(value));
        }else{
            if(value.getClass().equals(String.class)) {
                // Escape quotes and backslashes in string literals for Turtle syntax
                String escapedValue = value.toString()
                    .replace("\\", "\\\\")  // Escape backslashes first
                    .replace("\"", "\\\"")  // Escape double quotes
                    .replace("\n", "\\n")   // Escape newlines
                    .replace("\r", "\\r")   // Escape carriage returns
                    .replace("\t", "\\t");  // Escape tabs
                serializedValue = String.format("\"%s\"", escapedValue);
            }
            else serializedValue = String.valueOf(value);
        }
        writer.write(String.format("%s %s %s .\n", subject, predicate, serializedValue));
    }

    private static void writeRdfsModel(Class<?> clazz, String prefix, String prefixFullValue, Writer writer) throws IOException {
        writer.write(String.format("@prefix %s: <%s> .\n", prefix, prefixFullValue));
        writer.write("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns> .\n");
        writer.write("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema> .\n");
        String className = clazz.getSimpleName();
        writer.write(String.format("%s:%s a rdfs:Class .\n", prefix, className));
        for (Field declaredField : clazz.getDeclaredFields()) {
            String fieldName = declaredField.getName();
            writer.write(String.format("%s:%s a rdf:Property ;\n\t", prefix,  fieldName));
            writer.write(String.format("rdfs:domain %s:%s ;\n\t", prefix, className));
            writer.write(String.format("rdfs:range %s .\n", calculateRange(declaredField, prefix)));
        }
    }

    private static String calculateRange(Field field, String prefix) {
        Class<?> type = field.getType();

        if(type.equals(List.class)){
            type = getItemType(field);
        }

        if(field.isAnnotationPresent(Range.class)){
            return field.getAnnotation(Range.class).value();
        }

        if(DEFAULT_LITERAL_TYPES.contains(type)) {
            return "rdfs:Literal";
        }

        else return prefix + ":" + field.getType().getSimpleName();
    }
}
