package org.example;

import org.example.model.rdfs.NameExtractors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import static org.example.service.ClassLoader.loadClass;
import static org.example.service.CsvReader.readFile;
import static org.example.service.TtlWriter.writeRdfsModel;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchFieldException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String csvFilePath = args[0];
        String classFilePath = args[1];
        Class<?> clazz = loadClass(classFilePath);
        File file = new File("out.ttl");
        Writer writer = new FileWriter(file);
        writeRdfsModel(clazz, readFile(csvFilePath, clazz),"", NameExtractors.NAME_FIELD, writer);
        writer.close();
    }
}