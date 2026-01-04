package org.example;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;

import java.io.*;
import java.util.Map;

public class TtlConverter {


    public static void convertToTtl(String format, String source, Map<String, String> prefixMap, Writer target) throws IOException {
        Model tempModel = ModelFactory.createDefaultModel();
        try (FileInputStream in = new FileInputStream(source)) {
            tempModel.read(in, null, format);
        } catch (RiotException e) {
            System.err.println("Error reading cleaned file: " + e.getMessage());
            throw e;
        }

        System.out.println("✓ Loaded " + tempModel.size() + " triples");

        Model modelWithPrefixes = ModelFactory.createDefaultModel();
        modelWithPrefixes.setNsPrefixes(prefixMap);

        modelWithPrefixes.add(tempModel);

        System.out.println("Applied prefixes");
        RDFDataMgr.write(target, modelWithPrefixes, RDFFormat.TURTLE_BLOCKS);

        System.out.println("Converted");
    }

}
