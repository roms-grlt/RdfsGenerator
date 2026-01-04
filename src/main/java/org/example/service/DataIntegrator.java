package org.example.service;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Service to integrate multiple RDF datasets by creating links between related resources.
 * This class identifies similar films across different datasets (Amazon, Netflix, IMDB)
 * and creates owl:sameAs relationships, as well as a unified ontology.
 */
public class DataIntegrator {

    private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
    private static final String UNIFIED_NS = "http://www.filmdata.org/";

    // Namespace URIs for the different datasets
    private static final String AMAZON_NS = "http://www.amazon.org/";
    private static final String NETFLIX_NS = "http://www.netflix.org/";
    private static final String IMDB_NS = "http://www.imdb.org/";

    /**
     * Integrates multiple RDF files by finding matching films and creating owl:sameAs links.
     * Also creates a unified ontology schema.
     *
     * @param inputFiles Map of dataset names to their file paths
     * @param outputFile Path to write the integrated RDF model
     * @param createUnifiedOntology Whether to create a unified ontology
     * @throws IOException if file operations fail
     */
    public static void integrateDatasets(Map<String, String> inputFiles, String outputFile, boolean createUnifiedOntology)
            throws IOException {

        // Create a combined model
        Model combinedModel = ModelFactory.createDefaultModel();

        // Load all input files into the combined model
        Map<String, Model> datasetModels = new HashMap<>();
        for (Map.Entry<String, String> entry : inputFiles.entrySet()) {
            Model model = ModelFactory.createDefaultModel();
            model.read(entry.getValue(), "TURTLE");
            datasetModels.put(entry.getKey(), model);
            combinedModel.add(model);
        }

        // Add owl prefix
        combinedModel.setNsPrefix("owl", OWL_NS);
        combinedModel.setNsPrefix("unified", UNIFIED_NS);

        // Find and add sameAs relationships
        addSameAsRelationships(combinedModel, datasetModels);

        // Optionally create unified ontology
        if (createUnifiedOntology) {
            addUnifiedOntology(combinedModel);
        }

        // Add additional cross-dataset relationships
        addCrossDatasetRelationships(combinedModel, datasetModels);

        // Write the integrated model
        try (Writer writer = new FileWriter(outputFile)) {
            combinedModel.write(writer, "TURTLE");
        }

        System.out.println("Integration complete. Output written to: " + outputFile);
        printIntegrationStats(combinedModel, datasetModels);
    }

    /**
     * Finds matching films across datasets and adds owl:sameAs relationships.
     */
    private static void addSameAsRelationships(Model combinedModel, Map<String, Model> datasetModels) {
        // Extract films from each dataset with their titles
        Map<String, List<Resource>> filmsByDataset = new HashMap<>();

        for (Map.Entry<String, Model> entry : datasetModels.entrySet()) {
            String datasetName = entry.getKey();
            Model model = entry.getValue();
            List<Resource> films = new ArrayList<>();

            // Find all film instances
            StmtIterator filmIter = model.listStatements(null, RDF.type, (RDFNode) null);
            while (filmIter.hasNext()) {
                Statement stmt = filmIter.nextStatement();
                Resource film = stmt.getSubject();
                if (isFilmResource(film)) {
                    films.add(film);
                }
            }
            filmsByDataset.put(datasetName, films);
        }

        // Compare films across datasets and create sameAs links
        List<String> datasetNames = new ArrayList<>(filmsByDataset.keySet());
        int sameAsCount = 0;

        for (int i = 0; i < datasetNames.size(); i++) {
            for (int j = i + 1; j < datasetNames.size(); j++) {
                String dataset1 = datasetNames.get(i);
                String dataset2 = datasetNames.get(j);

                List<Resource> films1 = filmsByDataset.get(dataset1);
                List<Resource> films2 = filmsByDataset.get(dataset2);

                for (Resource film1 : films1) {
                    for (Resource film2 : films2) {
                        if (areFilmsSimilar(film1, film2, combinedModel)) {
                            combinedModel.add(film1, OWL.sameAs, film2);
                            sameAsCount++;
                        }
                    }
                }
            }
        }

        System.out.println("Added " + sameAsCount + " owl:sameAs relationships");
    }

    /**
     * Checks if a resource represents a film.
     */
    private static boolean isFilmResource(Resource resource) {
        if (resource.isAnon()) {
            return false;
        }
        String uri = resource.getURI();
        return (uri.startsWith(AMAZON_NS) || uri.startsWith(NETFLIX_NS) || uri.startsWith(IMDB_NS))
                && !uri.contains("Film") && !uri.contains("#");
    }

    /**
     * Determines if two film resources represent the same film based on title similarity.
     */
    private static boolean areFilmsSimilar(Resource film1, Resource film2, Model model) {
        String title1 = extractTitle(film1, model);
        String title2 = extractTitle(film2, model);

        if (title1 == null || title2 == null) {
            return false;
        }

        // Normalize titles for comparison
        title1 = normalizeTitle(title1);
        title2 = normalizeTitle(title2);

        // Exact match
        if (title1.equals(title2)) {
            return true;
        }

        // Similarity-based matching using Levenshtein distance
        double similarity = calculateSimilarity(title1, title2);
        return similarity > 0.85; // 85% similarity threshold
    }

    /**
     * Extracts the title of a film from various possible properties.
     */
    private static String extractTitle(Resource film, Model model) {
        // Try different title properties
        String[] titleProperties = {"title", "name"};

        for (String propName : titleProperties) {
            Property prop = model.getProperty(film.getNameSpace(), propName);
            if (prop != null) {
                Statement stmt = film.getProperty(prop);
                if (stmt != null) {
                    return stmt.getObject().toString();
                }
            }
        }
        return null;
    }

    /**
     * Normalizes a title for comparison (lowercase, remove punctuation, trim).
     */
    private static String normalizeTitle(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Calculates similarity between two strings using normalized Levenshtein distance.
     */
    private static double calculateSimilarity(String s1, String s2) {
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * Computes Levenshtein distance between two strings.
     */
    private static int levenshteinDistance(String s1, String s2) {
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
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1),     // insertion
                    dp[i - 1][j - 1] + cost // substitution
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Creates a unified ontology that maps classes and properties from different datasets.
     */
    private static void addUnifiedOntology(Model model) {
        // Create unified Film class
        Resource unifiedFilm = model.createResource(UNIFIED_NS + "Film");
        unifiedFilm.addProperty(RDF.type, RDFS.Class);
        unifiedFilm.addProperty(RDFS.label, "Film");
        unifiedFilm.addProperty(RDFS.comment, "Unified film class integrating Amazon, Netflix, and IMDB data");

        // Map dataset-specific classes to unified class
        Resource amazonFilm = model.getResource(AMAZON_NS + "AmazonFilm");
        Resource netflixFilm = model.getResource(NETFLIX_NS + "NetflixFilm");
        Resource imdbFilm = model.getResource(IMDB_NS + "ImdbFilm");

        if (amazonFilm != null) amazonFilm.addProperty(RDFS.subClassOf, unifiedFilm);
        if (netflixFilm != null) netflixFilm.addProperty(RDFS.subClassOf, unifiedFilm);
        if (imdbFilm != null) imdbFilm.addProperty(RDFS.subClassOf, unifiedFilm);

        // Create unified properties
        addUnifiedProperty(model, "title", "Title of the film");
        addUnifiedProperty(model, "rating", "Rating score");
        addUnifiedProperty(model, "releaseYear", "Year of release");
        addUnifiedProperty(model, "genre", "Film genre");
        addUnifiedProperty(model, "runtime", "Duration in minutes");

        // Map dataset-specific properties to unified properties
        mapPropertyToUnified(model, AMAZON_NS + "title", "title");
        mapPropertyToUnified(model, NETFLIX_NS + "title", "title");
        mapPropertyToUnified(model, IMDB_NS + "name", "title");

        mapPropertyToUnified(model, AMAZON_NS + "rating", "rating");
        mapPropertyToUnified(model, NETFLIX_NS + "imdbScore", "rating");
        mapPropertyToUnified(model, IMDB_NS + "rate", "rating");

        mapPropertyToUnified(model, AMAZON_NS + "releaseYear", "releaseYear");
        mapPropertyToUnified(model, IMDB_NS + "date", "releaseYear");

        mapPropertyToUnified(model, NETFLIX_NS + "genre", "genre");
        mapPropertyToUnified(model, IMDB_NS + "genre", "genre");

        mapPropertyToUnified(model, NETFLIX_NS + "runtime", "runtime");
        mapPropertyToUnified(model, IMDB_NS + "duration", "runtime");

        System.out.println("Unified ontology created");
    }

    /**
     * Creates a unified property with domain and range.
     */
    private static void addUnifiedProperty(Model model, String propName, String comment) {
        Property prop = model.createProperty(UNIFIED_NS + propName);
        prop.addProperty(RDF.type, RDF.Property);
        prop.addProperty(RDFS.comment, comment);
        prop.addProperty(RDFS.domain, model.getResource(UNIFIED_NS + "Film"));
        prop.addProperty(RDFS.range, RDFS.Literal);
    }

    /**
     * Maps a dataset-specific property to a unified property using rdfs:subPropertyOf.
     */
    private static void mapPropertyToUnified(Model model, String specificPropUri, String unifiedPropName) {
        Property specificProp = model.getProperty(specificPropUri);
        Property unifiedProp = model.getProperty(UNIFIED_NS + unifiedPropName);

        if (specificProp != null && unifiedProp != null) {
            specificProp.addProperty(RDFS.subPropertyOf, unifiedProp);
        }
    }

    /**
     * Adds additional cross-dataset relationships beyond owl:sameAs.
     * For example, links between films with similar ratings, genres, etc.
     */
    private static void addCrossDatasetRelationships(Model model, Map<String, Model> datasetModels) {
        // Create custom properties for cross-dataset relationships
        Property similarRating = model.createProperty(UNIFIED_NS + "hasSimilarRating");
        similarRating.addProperty(RDF.type, RDF.Property);
        similarRating.addProperty(RDFS.comment, "Indicates films with similar ratings");

        Property sameGenre = model.createProperty(UNIFIED_NS + "sharesSameGenre");
        sameGenre.addProperty(RDF.type, RDF.Property);
        sameGenre.addProperty(RDFS.comment, "Indicates films that share a genre");

        // This could be extended with more sophisticated relationship detection
        System.out.println("Cross-dataset relationship properties created");
    }

    /**
     * Prints statistics about the integration process.
     */
    private static void printIntegrationStats(Model combinedModel, Map<String, Model> datasetModels) {
        System.out.println("\n=== Integration Statistics ===");

        for (Map.Entry<String, Model> entry : datasetModels.entrySet()) {
            System.out.println(entry.getKey() + " dataset: " + entry.getValue().size() + " triples");
        }

        System.out.println("Combined model: " + combinedModel.size() + " triples");

        // Count owl:sameAs statements
        long sameAsCount = combinedModel.listStatements(null, OWL.sameAs, (RDFNode) null).toList().size();
        System.out.println("owl:sameAs relationships: " + sameAsCount);

        // Count unified classes
        long classCount = combinedModel.listStatements(null, RDF.type, RDFS.Class).toList().size();
        System.out.println("Total classes: " + classCount);

        // Count properties
        long propCount = combinedModel.listStatements(null, RDF.type, RDF.Property).toList().size();
        System.out.println("Total properties: " + propCount);
    }
}
