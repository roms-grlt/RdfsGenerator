package org.example.service;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class DataIntegrator {
    private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
    private static final String UNIFIED_NS = "http://example.org/unified/";

    public static void integrateDatasets(Map<String, String> inputFiles, String propertyIdentifier, List<String> classNames, String unifiedClassName, String outputFile)
            throws IOException {

        Model combinedModel = ModelFactory.createDefaultModel();

        Map<String, Model> datasetModels = new HashMap<>();
        for (Map.Entry<String, String> entry : inputFiles.entrySet()) {
            Model model = ModelFactory.createDefaultModel();
            model.read(entry.getValue(), "TURTLE");
            datasetModels.put(entry.getKey(), model);
            combinedModel.add(model);
        }

        combinedModel.setNsPrefix("owl", OWL_NS);
        combinedModel.setNsPrefix("unified", UNIFIED_NS);

        addSameAsRelationships(combinedModel, datasetModels, propertyIdentifier, classNames);

        unifyOntology(combinedModel, unifiedClassName, classNames);

        // Charge et integre l'ontologie enrichie
        loadEnrichedOntology(combinedModel, outputFile);

        try (Writer writer = new FileWriter(outputFile)) {
            combinedModel.write(writer, "TURTLE");
        }

        System.out.println("Integration complete. Output written to: " + outputFile);
        printIntegrationStats(combinedModel, datasetModels);
    }

    private static void loadEnrichedOntology(Model combinedModel, String outputFile) {
        try {
            // Determine le chemin de l'ontologie relative au fichier de sortie
            String ontologyPath = outputFile.substring(0, outputFile.lastIndexOf('/') + 1) + "ontology.ttl";

            java.io.File ontologyFile = new java.io.File(ontologyPath);
            if (ontologyFile.exists()) {
                // Cree un modele temporaire avec les prefixes necessaires
                Model ontologyModel = ModelFactory.createDefaultModel();
                ontologyModel.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
                ontologyModel.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
                ontologyModel.setNsPrefix("unified", UNIFIED_NS);
                ontologyModel.setNsPrefix("amazon", "http://example.org/amazon/");
                ontologyModel.setNsPrefix("netflix", "http://example.org/netflix/");
                ontologyModel.setNsPrefix("imdb", "http://example.org/imdb/");

                ontologyModel.read(ontologyPath, "TURTLE");

                // Ajoute seulement les triplets de l'ontologie qui ne sont pas déjà présents
                // (évite les doublons avec l'ontologie auto-générée)
                combinedModel.add(ontologyModel);

                System.out.println("Enriched ontology loaded from: " + ontologyPath);
            } else {
                System.out.println("No enriched ontology found at: " + ontologyPath);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load enriched ontology: " + e.getMessage());
        }
    }

    private static void unifyOntology(Model model, String unifiedClassName, List<String> classNames) {
        Map<String, Set<String>> propertiesByClass = calculatePropertiesByClass(model, classNames);
        Map<String, List<String>> propertiesToUnifyByName = extractPropertiesToUnify(propertiesByClass);

        for (Map.Entry<String, List<String>> entry : propertiesToUnifyByName.entrySet()) {
            addUnifiedProperty(model, entry.getKey(), unifiedClassName);
            for(String property : entry.getValue()) {
                mapPropertyToUnified(model, property, entry.getKey());
            }
        }

        Set<String> classesToUnifyUris = getClassUris(model, classNames);

        Resource unifiedFilm = model.createResource(UNIFIED_NS + unifiedClassName);
        unifiedFilm.addProperty(RDF.type, RDFS.Class);

        for (String uri : classesToUnifyUris) {
            model.getResource(uri).addProperty(RDFS.subClassOf, unifiedFilm);
        }
    }

    private static Set<String> getClassUris(Model model, List<String> classNames) {
        return model.listStatements(null, RDF.type, (RDFNode) null)
                .toList()
                .stream()
                .map(statement -> statement.getObject().asResource().getURI())
                .filter(uri-> classNames.stream().anyMatch(uri::contains))
                .collect(Collectors.toSet());
    }

    private static Map<String, List<String>> extractPropertiesToUnify(Map<String, Set<String>> propertiesByClass) {
        Map<String, List<String>> propertiesToUnify = new HashMap<>();

        for (int i = 0; i < propertiesByClass.size(); i++) {
            for (int j = i+1; j < propertiesByClass.size(); j++) {
                Set<String> properties1 = (Set<String>) propertiesByClass.values().toArray((size)-> new Set<?>[size])[i];
                Set<String> properties2 = (Set<String>) propertiesByClass.values().toArray((size)-> new Set<?>[size])[j];
                for (String property1 : properties1) {
                    for (String property2 : properties2) {
                        String propertyName1 = property1.substring(property1.lastIndexOf('/') + 1);
                        String propertyName2 = property2.substring(property2.lastIndexOf('/') + 1);
                        if(propertyName1.equals(propertyName2)) {
                            if(!propertiesToUnify.containsKey(propertyName1)) {
                                propertiesToUnify.put(propertyName1, new ArrayList<>());
                            }
                            if(!propertiesToUnify.get(propertyName1).contains(property2)) {
                                propertiesToUnify.get(propertyName1).add(property2);
                            }
                            if(!propertiesToUnify.get(propertyName1).contains(property1)) {
                                propertiesToUnify.get(propertyName1).add(property1);
                            }
                        }
                    }
                }
            }
        }

        return propertiesToUnify;
    }

    private static Map<String, Set<String>> calculatePropertiesByClass(Model model, List<String> classNames) {
        List<Statement> statements = model.listStatements(null, RDFS.domain, (RDFNode) null).toList();

        Map<String, Set<String>> propertiesByClassToUnify = classNames.stream()
                .collect(Collectors.toMap(
                        className -> className,
                        className -> new HashSet<>()
                ));

        for (Statement statement : statements) {
            String domain = statement.getObject().asResource().getLocalName();
            if (propertiesByClassToUnify.containsKey(domain)) {
                String propertyName = statement.getSubject().getURI();
                if(!propertiesByClassToUnify.containsKey(domain)) {
                    propertiesByClassToUnify.put(domain, new HashSet<>());
                }
                propertiesByClassToUnify.get(domain).add(propertyName);
            }
        }

        return propertiesByClassToUnify;
    }

    private static void addSameAsRelationships(Model combinedModel, Map<String, Model> datasetModels, String propertyIdentifier, List<String> classNames) {
        Map<String, List<Resource>> resourcesByDataset = new HashMap<>();

        for (Map.Entry<String, Model> entry : datasetModels.entrySet()) {
            String datasetName = entry.getKey();
            Model model = entry.getValue();

            List<Resource> resources = new ArrayList<>(model.listStatements(null, RDF.type, (RDFNode) null)
                    .toList()
                    .stream()
                    .filter(statement -> classNames.contains(statement.getObject().asResource().getLocalName()))
                    .map(Statement::getSubject)
                    .toList());

            resourcesByDataset.put(datasetName, resources);
        }

        List<String> datasetNames = new ArrayList<>(resourcesByDataset.keySet());

        for (int i = 0; i < datasetNames.size(); i++) {
            for (int j = i + 1; j < datasetNames.size(); j++) {
                List<Resource> resources1 = resourcesByDataset.get(datasetNames.get(i));
                List<Resource> resources2 = resourcesByDataset.get(datasetNames.get(j));

                for (Resource resource1 : resources1) {
                    for (Resource resource2 : resources2) {
                        if (areResourcesSimilar(resource1, resource2, combinedModel, propertyIdentifier)) {
                            combinedModel.add(resource1, OWL.sameAs, resource2);
                        }
                    }
                }
            }
        }
    }

    private static boolean areResourcesSimilar(Resource resource1, Resource resource2, Model model, String propertyIdentifier) {
        String identifier1 = extractIdentifier(resource1, model, propertyIdentifier);
        String identifier2 = extractIdentifier(resource2, model, propertyIdentifier);

        if (identifier1 == null || identifier2 == null) {
            return false;
        }

        identifier1 = normalizeIdentifier(identifier1);
        identifier2 = normalizeIdentifier(identifier2);

        if (identifier1.equals(identifier2)) {
            return true;
        }

        double similarity = calculateSimilarity(identifier1, identifier2);
        return similarity > 0.85;
    }

    private static String extractIdentifier(Resource film, Model model, String propertyIdentifier) {
        return model.listStatements(film, null, (RDFNode) null)
                .toList()
                .stream()
                .filter(statement -> propertyIdentifier.equals(statement.asTriple().getPredicate().getLocalName()))
                .findFirst()
                .map(statement -> (String) statement.asTriple().getObject().getLiteral().getValue())
                .orElse(null);
    }

    private static String normalizeIdentifier(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static double calculateSimilarity(String s1, String s2) {
        int distance = calculateDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        return 1.0 - ((double) distance / maxLength);
    }

    private static int calculateDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private static void addUnifiedProperty(Model model, String propName, String unifiedClassName) {
        Property prop = model.createProperty(UNIFIED_NS + propName);
        prop.addProperty(RDF.type, RDF.Property);
        prop.addProperty(RDFS.domain, model.getResource(UNIFIED_NS + unifiedClassName));
        prop.addProperty(RDFS.range, RDFS.Literal);
    }

    private static void mapPropertyToUnified(Model model, String specificPropUri, String unifiedPropName) {
        Property specificProp = model.getProperty(specificPropUri);
        Property unifiedProp = model.getProperty(UNIFIED_NS + unifiedPropName);

        if (specificProp != null && unifiedProp != null) {
            specificProp.addProperty(RDFS.subPropertyOf, unifiedProp);
        }
    }

    private static void printIntegrationStats(Model combinedModel, Map<String, Model> datasetModels) {
        System.out.println("\n=== Integration Statistics ===");

        for (Map.Entry<String, Model> entry : datasetModels.entrySet()) {
            System.out.println(entry.getKey() + " dataset: " + entry.getValue().size() + " triples");
        }

        System.out.println("Combined model: " + combinedModel.size() + " triples");

        long sameAsCount = combinedModel.listStatements(null, OWL.sameAs, (RDFNode) null).toList().size();
        System.out.println("owl:sameAs relationships: " + sameAsCount);

        long classCount = combinedModel.listStatements(null, RDF.type, RDFS.Class).toList().size();
        System.out.println("Total classes: " + classCount);

        long propCount = combinedModel.listStatements(null, RDF.type, RDF.Property).toList().size();
        System.out.println("Total properties: " + propCount);
    }

}
