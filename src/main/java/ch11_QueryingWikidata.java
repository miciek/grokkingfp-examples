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

    /**
     * NOTE: this class is not part of the book and was added later. Wikidata (like all Wikimedia services)
     * started rejecting requests without a descriptive User-Agent header (403 Forbidden, see
     * https://meta.wikimedia.org/wiki/User-Agent_policy). To fix it without affecting how readers use this
     * repository when reading the book, we made Jena start this class automatically when it initializes
     * (it's listed in src/main/resources/META-INF/services/org.apache.jena.sys.JenaSubsystemLifecycle).
     * After this change, all requests to Wikidata (from examples, tests, and sbt console) send the header
     * without any changes to the book's code.
     */
    public static class WikidataUserAgent implements org.apache.jena.sys.JenaSubsystemLifecycle {
        @Override
        public void start() {
            org.apache.jena.http.sys.RegistryRequestModifier.get().addPrefix(
                    "https://query.wikidata.org/",
                    (params, headers) -> headers.put("User-Agent", "grokkingfp-examples (https://github.com/miciek/grokkingfp-examples)")
            );
        }

        @Override
        public void stop() {}
    }
}
