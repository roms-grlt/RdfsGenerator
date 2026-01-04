package org.example;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RiotException;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NTriplesValidator {

    public static void cleanAndConvert(String inputPath, String outputPath) throws IOException {
        System.out.println("Analyzing prefixes in " + inputPath + "...");

        Map<String, Integer> prefixCounts = discoverPrefixes(inputPath);

        System.out.println("\nTop prefixes found:");
        prefixCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .forEach(e -> System.out.println("  " + e.getKey() + " (" + e.getValue() + " occurrences)"));

        System.out.println("\nCleaning and converting...");

        File cleanedFile = new File(inputPath + ".cleaned");
        int totalLines = 0;
        int skippedLines = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(cleanedFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                totalLines++;

                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (containsInvalidURI(line)) {
                    String cleaned = cleanURI(line);
                    if (cleaned != null) {
                        writer.write(cleaned);
                        writer.newLine();
                    } else {
                        skippedLines++;
                    }
                } else {
                    writer.write(line);
                    writer.newLine();
                }

                if (totalLines % 10000 == 0) {
                    System.out.println("Processed " + totalLines + " lines...");
                }
            }
        }

        System.out.println("✓ Cleaned: " + totalLines + " lines, skipped: " + skippedLines);

        // Charge le modèle sans préfixes
        Model tempModel = ModelFactory.createDefaultModel();

        try (FileInputStream in = new FileInputStream(cleanedFile)) {
            tempModel.read(in, null, "N-TRIPLE");
        } catch (RiotException e) {
            System.err.println("Error reading cleaned file: " + e.getMessage());
            throw e;
        }

        System.out.println("✓ Loaded " + tempModel.size() + " triples");

        // *** CLEF : Créer un nouveau modèle AVEC préfixes et copier les triplets ***
        Model modelWithPrefixes = ModelFactory.createDefaultModel();
        setPrefixes(modelWithPrefixes, prefixCounts);

        // Copie tous les triplets dans le nouveau modèle
        modelWithPrefixes.add(tempModel);

        System.out.println("✓ Applied prefixes");

        // Écriture avec format compact
        try (FileOutputStream out = new FileOutputStream(outputPath)) {
            RDFDataMgr.write(out, modelWithPrefixes, RDFFormat.TURTLE_BLOCKS);
        }

        //cleanedFile.delete();

        System.out.println("✓ Converted to " + outputPath);
    }

    private static void setPrefixes(Model model, Map<String, Integer> prefixCounts) {
        // Préfixes standards
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        model.setNsPrefix("dc", "http://purl.org/dc/elements/1.1/");
        model.setNsPrefix("dcterms", "http://purl.org/dc/terms/");

        // Préfixes LinkedMDB découverts
        List<Map.Entry<String, Integer>> sortedPrefixes = new ArrayList<>(prefixCounts.entrySet());
        sortedPrefixes.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        Set<String> usedNames = new HashSet<>();

        for (Map.Entry<String, Integer> entry : sortedPrefixes) {
            String fullPrefix = entry.getKey();
            String shortName = generatePrefixName(fullPrefix);

            // Évite les doublons
            if (!usedNames.contains(shortName)) {
                model.setNsPrefix(shortName, fullPrefix);
                usedNames.add(shortName);
            }
        }
    }

    private static String generatePrefixName(String uri) {
        // LinkedMDB
        if (uri.contains("linkedmdb.org/resource/movie/")) return "movie";
        if (uri.contains("linkedmdb.org/resource/actor/")) return "actor";
        if (uri.contains("linkedmdb.org/resource/director/")) return "director";
        if (uri.contains("linkedmdb.org/resource/writer/")) return "writer";
        if (uri.contains("linkedmdb.org/resource/producer/")) return "producer";
        if (uri.contains("linkedmdb.org/resource/film/")) return "film";
        if (uri.contains("linkedmdb.org/resource/country/")) return "country";
        if (uri.contains("linkedmdb.org/resource/film_genre/")) return "genre";
        if (uri.contains("linkedmdb.org/resource/performance/")) return "perf";
        if (uri.contains("linkedmdb.org/resource/oddlinker/")) return "oddlinker";
        if (uri.contains("linkedmdb.org/resource/interlink/")) return "interlink";
        if (uri.contains("linkedmdb.org/resource/")) return "lmdb";

        // DBpedia
        if (uri.contains("dbpedia.org/ontology/")) return "dbo";
        if (uri.contains("dbpedia.org/property/")) return "dbp";
        if (uri.contains("dbpedia.org/resource/")) return "dbr";

        // Wikidata
        if (uri.contains("wikidata.org/entity/")) return "wd";
        if (uri.contains("wikidata.org/prop/direct/")) return "wdt";

        // Freebase
        if (uri.contains("freebase.com/")) return "freebase";

        // Autres
        if (uri.contains("schema.org/")) return "schema";

        // Par défaut, utilise le dernier segment de l'URI
        String[] parts = uri.split("/");
        if (parts.length > 3) {
            return parts[parts.length - 2];
        }

        return "ns";
    }

    private static Map<String, Integer> discoverPrefixes(String inputPath) throws IOException {
        Map<String, Integer> counts = new HashMap<>();
        Pattern uriPattern = Pattern.compile("<([^>]+)>");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null && lineCount < 100000) {
                lineCount++;

                Matcher matcher = uriPattern.matcher(line);
                while (matcher.find()) {
                    String uri = matcher.group(1);
                    String prefix = extractPrefix(uri);
                    if (prefix != null) {
                        counts.put(prefix, counts.getOrDefault(prefix, 0) + 1);
                    }
                }
            }
        }

        return counts;
    }

    private static String extractPrefix(String uri) {
        int lastSlash = Math.max(uri.lastIndexOf('/'), uri.lastIndexOf('#'));
        if (lastSlash > 0 && lastSlash < uri.length() - 1) {
            return uri.substring(0, lastSlash + 1);
        }
        return null;
    }

    private static boolean containsInvalidURI(String line) {
        int start = 0;
        while ((start = line.indexOf('<', start)) != -1) {
            int end = line.indexOf('>', start);
            if (end == -1) return false;

            String uri = line.substring(start + 1, end);
            if (uri.contains(" ") || uri.contains("\t")) {
                return true;
            }
            start = end + 1;
        }
        return false;
    }

    private static String cleanURI(String line) {
        try {
            StringBuilder result = new StringBuilder();
            int pos = 0;

            while (pos < line.length()) {
                int start = line.indexOf('<', pos);
                if (start == -1) {
                    result.append(line.substring(pos));
                    break;
                }

                result.append(line.substring(pos, start + 1));

                int end = line.indexOf('>', start);
                if (end == -1) {
                    return null;
                }

                String uri = line.substring(start + 1, end);

                String cleanedURI = uri.replace(" ", "%20")
                        .replace("\t", "%09")
                        .replace("\n", "")
                        .replace("\r", "");

                result.append(cleanedURI);
                result.append('>');

                pos = end + 1;
            }

            return result.toString();
        } catch (Exception e) {
            return null;
        }
    }
}