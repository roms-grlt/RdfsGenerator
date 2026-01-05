package org.example;

import org.example.model.rdfs.NameExtractors;
import org.example.service.QueryExecutor;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.example.service.TtlConverter.convertToTtl;
import static org.example.service.ClassLoader.loadClass;
import static org.example.service.CsvReader.readFile;
import static org.example.service.TtlImporter.importFrom;
import static org.example.service.TtlWriter.writeRdfsModel;
import static org.example.service.DataIntegrator.integrateDatasets;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchFieldException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String command = args[0];
        switch (command) {
            case "csv" :
                generateTurtleFromCsv(args[1], args[2], args[3], args[4], args[5]);
                break;
            case "request" :
                generateTurtleFromRequest(args[1], args[2], args[3]);
                break;
            case "convert" :
                convertToTurtle(args[1], args[2], args.length == 5 ? args[4] : null, args[3]);
                break;
            case "integrate" :
                integrateData(args);
                break;
            case "query" :
                executeQuery(args[1], args[2]);
        }
    }

    private static void executeQuery(String requestFilePath, String turtleFilePath) throws IOException {
        String request = new String(Files.readAllBytes(Paths.get(requestFilePath)));
        QueryExecutor.executeQuery(request, turtleFilePath);
    }

    private static void convertToTurtle(String format, String source, String prefixesPath, String target) throws IOException {
        convertToTtl(format, source, prefixMap(prefixesPath), new FileWriter(target, false));
    }

    private static Map<String, String> prefixMap(String prefixesPath) throws IOException {
        if (prefixesPath == null || prefixesPath.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> prefixMap = new HashMap<>();
        try (BufferedReader fileReader = new BufferedReader(new FileReader(prefixesPath))) {
            while (true) {
                String line = fileReader.readLine();
                if(line == null) break;
                String[] split = line.replace(" ", "").split(",");
                prefixMap.put(split[0], split[1]);
                System.out.printf("%s\t%s%n", split[0], split[1]);
            }
        }
        return prefixMap;
    }

    private static void generateTurtleFromRequest(String url, String requestPath, String target) throws IOException {
        String request = new String(Files.readAllBytes(Paths.get(requestPath)));
        Writer writer = new FileWriter(target, false);
        importFrom(url, request, writer);
    }


    private static void generateTurtleFromCsv(String csvFilePath, String classFilePath, String target, String prefix, String prefixFullValue) throws IOException, ClassNotFoundException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Class<?> clazz = loadClass(classFilePath);
        File file = new File(target);
        Writer writer = new FileWriter(file);
        writeRdfsModel(clazz, readFile(csvFilePath, clazz),prefix, prefixFullValue, NameExtractors.NAME_FIELD, writer);
        writer.close();
    }

    private static void integrateData(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: integrate <output_file> <dataset1_name> <dataset1_file> <dataset2_name> <dataset2_file> ... [--no-ontology]");
            return;
        }

        String outputFile = args[1];
        int numberOfDatasets = Integer.parseInt(args[2]);

        Map<String, String> inputFiles = new HashMap<>();
        for (int i = 0; i < numberOfDatasets; ++i) {
            String datasetName = args[3+2*i];
            String filePath = args[3+2*i + 1];
            inputFiles.put(datasetName, filePath);
        }

        String propertyIdentifier = args[3+2*numberOfDatasets];

        String unifiedClassName = args[4+2*numberOfDatasets];

        List<String> classesToUnify = new ArrayList<>(Arrays.asList(args).subList(5 + 2 * numberOfDatasets, args.length));

        integrateDatasets(inputFiles, propertyIdentifier, classesToUnify, unifiedClassName, outputFile);
    }
}