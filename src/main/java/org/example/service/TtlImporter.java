package org.example.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;

public class TtlImporter {


    public static void main(String[] args) {
        String serviceURL = "https://dbpedia.org/sparql";

        String queryString = """
                PREFIX dbo: <http://dbpedia.org/ontology/>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                
                CONSTRUCT {
                  ?film a dbo:Film ;
                        rdfs:label ?title ;
                        dbo:director ?director .
                }
                WHERE {
                  ?film a dbo:Film ;
                        rdfs:label ?title ;
                        dbo:director ?director .
                  FILTER (lang(?title) = 'en')
                }
                LIMIT 1000
                """;

        Model model = QueryExecutionHTTP.create()
                .endpoint(serviceURL)
                .query(queryString)
                .construct();

        model.write(System.out, "TURTLE");
    }

}
