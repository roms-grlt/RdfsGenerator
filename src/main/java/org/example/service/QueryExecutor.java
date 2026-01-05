package org.example.service;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.util.FileManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class QueryExecutor {

    public static void executeQuery(String stringQuery, String turtleFilePath) {
        Model model = ModelFactory.createDefaultModel();
        FileManager.getInternal().readModel(model, turtleFilePath, "TURTLE");

        Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
        InfModel infModel = ModelFactory.createInfModel(reasoner, model);

        Query query = QueryFactory.create(stringQuery);
        List<String> queryParams = query.getResultVars();

        try (QueryExecution qexec = QueryExecutionFactory.create(query, infModel)) {
            ResultSet resultSet = qexec.execSelect();
            System.out.println("                  |=============================|");
            System.out.println("                  |      Request Results        |");
            System.out.println("                  |=============================|");

            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.next();

                System.out.print("|\t");
                for (String param : queryParams) {
                    Object result = solution.get(param);
                    String displayValue;

                    if (result == null) {
                        displayValue = "N/A";
                    } else if (result instanceof org.apache.jena.rdf.model.Literal) {
                        // Pour les littéraux, extraire uniquement la valeur sans le type
                        displayValue = ((org.apache.jena.rdf.model.Literal) result).getLexicalForm();
                    } else {
                        displayValue = result.toString();
                    }

                    System.out.print("\t" + param + " : " + displayValue + " \t|");
                }
                System.out.print("\n");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        String request = new String(Files.readAllBytes(Paths.get("./queries/sub-request-request.sparql")));
        QueryExecutor.executeQuery(request, "./data/integrated.ttl");

    }
}
