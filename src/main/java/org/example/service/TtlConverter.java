package org.example.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.*;
import java.util.Map;

public class TtlConverter {


    public static void convertToTtl(String format, String source, Map<String, String> prefixMap, Writer target) throws IOException {
        Model tempModel = ModelFactory.createDefaultModel();
        FileInputStream in = new FileInputStream(source);
        tempModel.read(in, null, format);
        Model modelWithPrefixes = ModelFactory.createDefaultModel();
        for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
            System.out.println(":" + entry.getValue());
            modelWithPrefixes.setNsPrefix(entry.getKey(), entry.getValue());
        }
        modelWithPrefixes.add(tempModel);
        RDFDataMgr.write(target, modelWithPrefixes, RDFFormat.TURTLE_BLOCKS);
    }

}
