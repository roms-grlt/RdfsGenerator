package org.example.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;

import java.io.Writer;

public class TtlImporter {


    public static void importFrom(String url, String query, Writer target){
        Model model = QueryExecutionHTTP.create()
                .endpoint(url)
                .query(query)
                .construct();

        model.write(target, "TURTLE");
    }

}
