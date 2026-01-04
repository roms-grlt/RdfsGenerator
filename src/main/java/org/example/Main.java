package org.example;

import org.example.model.rdfs.NameExtractors;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.example.service.ClassLoader.loadClass;
import static org.example.service.CsvReader.readFile;
import static org.example.service.TtlImporter.importFrom;
import static org.example.service.TtlWriter.writeRdfsModel;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchFieldException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String command = args[0];
        switch (command) {
            case "csv" :
                generateTurtleFromCsv(args[1], args[2], args[3]);
                break;
            case "request" :
                generateTurtleFromRequest(args[1], args[2], args[3]);
                break;
        }
    }

    private static void generateTurtleFromRequest(String url, String requestPath, String target) throws IOException {
        String request = new String(Files.readAllBytes(Paths.get(requestPath)));
        Writer writer = new FileWriter(target, false);
        importFrom(url, request, writer);
    }


    private static void generateTurtleFromCsv(String csvFilePath, String classFilePath, String target) throws IOException, ClassNotFoundException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Class<?> clazz = loadClass(classFilePath);
        File file = new File(target);
        Writer writer = new FileWriter(file);
        writeRdfsModel(clazz, readFile(csvFilePath, clazz),"", NameExtractors.NAME_FIELD, writer);
        writer.close();
    }
}