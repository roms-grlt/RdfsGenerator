package org.example.service;

import org.example.model.Ignore;
import org.example.model.Remove;
import org.example.dto.ImdbFilmCsvModel;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.example.utils.ClassUtils.getItemType;

public class CsvReader {

    public static <T> List<T> readFile(String fileName, Class<T> type) throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        System.out.println(new File(fileName).getAbsolutePath());
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        String[] fields = reader.readLine().toLowerCase().split(",");

        List<T> result = new ArrayList<>();

        while (true) {
            String line = reader.readLine();
            if (line == null)
                break;
            T instance = type.getDeclaredConstructor().newInstance();
            String[] values = splitAndClean(line);
            for (int i = 0; i < fields.length; ++i) {
                if (isNotBlank(values[i])) {
                    Field field = getField(type, fields[i]);
                    setValue(field, values[i], instance);
                }
            }
            result.add(instance);
        }
        return result;
    }

    private static String[] splitAndClean(String line) {
        return Arrays.stream(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))
                .map(str -> str.trim().replaceAll("^\"|\"$", ""))
                .toArray(String[]::new);

    }

    private static void setValue(Field field, String value, Object instance) throws IllegalAccessException, ClassNotFoundException {
        boolean accessibility = field.canAccess(instance);
        field.setAccessible(true);

        Class<?> fieldType = field.getType();
        Object valueToSet;

        if (field.isAnnotationPresent(Ignore.class) && value.equals(field.getAnnotation(Ignore.class).value()))
            return;

        if (field.isAnnotationPresent(Remove.class))
            value = value.replace(field.getAnnotation(Remove.class).regex(), "");

        if (fieldType.equals(String.class)) {
            valueToSet = value;
        } else if (fieldType.equals(Long.class)) {
            valueToSet = Long.parseLong(value);
        } else if (fieldType.equals(Integer.class)) {
            valueToSet = Integer.parseInt(value);
        } else if (fieldType.equals(LocalDate.class)) {
            valueToSet = LocalDate.parse(value);
        } else if (fieldType.equals(Double.class)) {
            valueToSet = Double.parseDouble(value);
        } else if (fieldType.equals(Boolean.class)) {
            valueToSet = Boolean.parseBoolean(value);
        } else if (fieldType.equals(List.class)) {
            valueToSet = deserializeList(value, field);
        } else throw new IllegalStateException();

        field.set(instance, valueToSet);

        field.setAccessible(accessibility);
    }

    private static List<?> deserializeList(String value, Field field) {
        String[] list = Arrays.stream(value.split(","))
                .map(String::trim)
                .toArray(String[]::new);
        List<?> listValue;
        Class<?> itemType = getItemType(field);

        if (itemType.equals(String.class)) {
            List<String> temp = new ArrayList<>();
            for (String item : list) {
                temp.add(item);
            }
            listValue = temp;
        } else if (itemType.equals(Long.class)) {
            List<Long> temp = new ArrayList<>();
            for (String item : list) {
                temp.add(Long.parseLong(item));
            }
            listValue = temp;
        } else if (itemType.equals(Integer.class)) {
            List<Integer> temp = new ArrayList<>();
            for (String item : list) {
                temp.add(Integer.parseInt(item));
            }
            listValue = temp;
        } else if (itemType.equals(LocalDate.class)) {
            List<LocalDate> temp = new ArrayList<>();
            for (String item : list) {
                temp.add(LocalDate.parse(item));
            }
            listValue = temp;
        } else if (itemType.equals(Double.class)) {
            List<Double> temp = new ArrayList<>();
            for (String item : list) {
                temp.add(Double.parseDouble(item));
            }
            listValue = temp;
        } else if (itemType.equals(Boolean.class)) {
            List<Boolean> temp = new ArrayList<>();
            for (String item : list) {
                temp.add(Boolean.parseBoolean(item));
            }
            listValue = temp;
        } else throw new IllegalArgumentException();
        return listValue;
    }

    private static Field getField(Class<?> type, String field) throws NoSuchFieldException {
        return type.getDeclaredField(field);
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    public static void main(String[] args) throws NoSuchFieldException, ClassNotFoundException, IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        List<ImdbFilmCsvModel> films = readFile("src/main/resources/imdb.csv", ImdbFilmCsvModel.class);

        for (String string : splitAndClean("\"salut\", 10, 8.5, \"salut, au revoir, bonsoir\""))
            System.out.println(": " + string);
    }
}
