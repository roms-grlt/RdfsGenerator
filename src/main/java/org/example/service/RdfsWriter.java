package org.example.service;

import org.example.dto.ImdbFilmCsvModel;
import org.example.model.NameExtractor;
import org.example.model.NameExtractors;
import org.example.model.Range;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import static org.example.utils.ClassUtils.getItemType;

public class RdfsWriter {
    private static final List<Class<?>> DEFAULT_LITERAL_TYPES = Arrays.asList(
            String.class,
            Integer.class,
            int.class,
            Double.class,
            double.class,
            Long.class,
            long.class
    );

    public static <T> void writeRdfsModel(Class<T> clazz, List<T> objects, String prefix,  NameExtractor nameExtractor, Writer writer) throws IOException, IllegalAccessException {
        writeRdfsModel(clazz, prefix, writer);
        for (T object : objects) {
            String name = nameExtractor.getName(object);
            writer.write(String.format(":%s a %s:%s .\n", name, prefix, clazz.getSimpleName()));
            for(Field field : clazz.getDeclaredFields()) {
                boolean accessible = field.canAccess(object);
                field.setAccessible(true);
                Object value = field.get(object);
                field.setAccessible(accessible);
                if(value!=null) {
                    if (field.getType().equals(List.class)) {
                        for (Object item : (List<?>) value) {
                            writeRdfsProperty(
                                    String.format(":%s", name),
                                    String.format("%s:%s", prefix, field.getName()),
                                    item,
                                    nameExtractor,
                                    writer
                            );
                        }
                    } else {
                        writeRdfsProperty(
                                String.format(":%s", name),
                                String.format("%s:%s", prefix, field.getName()),
                                value,
                                nameExtractor,
                                writer
                        );
                    }
                }
            }
        }
    }

    private static void writeRdfsProperty(String subject, String predicate, Object value, NameExtractor nameExtractor, Writer writer) throws IOException {
        String serializedValue;
        if(!DEFAULT_LITERAL_TYPES.contains(value.getClass())) {
            serializedValue = nameExtractor.getName(value);
        }else{
            serializedValue = String.valueOf(value);
        }
        writer.write(String.format("%s %s %s .\n", subject, predicate, serializedValue));
    }

    private static void writeRdfsModel(Class<?> clazz, String prefix, Writer writer) throws IOException {
        String className = clazz.getSimpleName();
        writer.write(String.format("%s:%s a rdfs:Class .\n", prefix, className));
        for (Field declaredField : clazz.getDeclaredFields()) {
            String fieldName = declaredField.getName();
            writer.write(String.format("%s:%s a rdfs:Property ;\n\t", prefix,  fieldName));
            writer.write(String.format("rdfs:domain %s ;\n\t", className));
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

    public static void main(String[] args) throws IOException, NoSuchFieldException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        List<ImdbFilmCsvModel> films = CsvReader.readFile("src/main/resources/imdb.csv", ImdbFilmCsvModel.class);
        File file = new File("src/main/resources/out.ttl");
        Writer writer = new FileWriter(file);
        writeRdfsModel(ImdbFilmCsvModel.class, List.of(films.get(0)),"", NameExtractors.NAME_FIELD, writer);
    }

}
