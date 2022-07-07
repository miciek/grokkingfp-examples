import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;

import java.util.Iterator;

public class ch11_QueryingWikidata {
    public static void main(String[] args) {
        String query =
                "PREFIX wd: <http://www.wikidata.org/entity/>\n" +
                "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "SELECT DISTINCT ?attraction ?label WHERE {\n" +
                "  ?attraction wdt:P31 wd:Q570116;\n" +
                "              rdfs:label ?label.\n" +
                "  FILTER(LANG(?label) = \"en\").\n" +
                "} LIMIT 3";
        
        RDFConnection connection = RDFConnectionRemote.create()
                .destination("https://query.wikidata.org/")
                .queryEndpoint("sparql")
                .build();

        QueryExecution execution = connection.query(QueryFactory.create(query));
        Iterator<QuerySolution> solutions = execution.execSelect();
        
        solutions.forEachRemaining(solution -> {
            String id = solution.getResource("attraction").getLocalName();
            String label = solution.getLiteral("label").getString();
            System.out.printf("Got attraction %s (id = %s)%n", label, id);
        });
        
        execution.close();
        connection.close();

        // or:
        // try(RDFConnection connection = RDFConnectionRemote.create()
        //         .destination("https://query.wikidata.org/")
        //         .queryEndpoint("sparql")
        //         .build()) {
        //     try(QueryExecution execution = connection.query(QueryFactory.create(query))) {
        //         Iterator<QuerySolution> solutions = execution.execSelect();
        //         solutions.forEachRemaining(solution -> {
        //             String id = solution.getResource("attraction").getLocalName();
        //             String label = solution.getLiteral("label").getString();
        //             System.out.printf("Got attraction %s (id = %s)%n", label, id);
        //         });
        //      }
        // }
    }
}
