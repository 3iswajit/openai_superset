package com.subex.embeddedAI.service;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
//import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.reader.JsonMetadataGenerator;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class AIService {

//    private final OpenAiChatModel chatModel;

    private final OllamaChatModel chatModel;

    private final VectorStore vectorStore;

    @Autowired
    public AIService(OllamaChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    public Map getJokes(){
        return Map.of("generation", this.chatModel.call("Tell me a joke"));
    }

    public Map getBook(String category, String year){
        PromptTemplate promptTemplate = new PromptTemplate("""
                Please provide me best book for the given {category} and the {year}.
                Please do provide a summary of the book as well, the information should be
                limited and not much in depth. Please provide the details in the JSON format
                containing this information : category, book, year, review, author, summary
                """);
        promptTemplate.add("category", category);
        promptTemplate.add("year", year);
        ChatResponse response = chatModel.call(promptTemplate.create());
        return Map.of("generation",response);
    }

    public void addDocuments() {
        List<Document> documents = List.of(
                new Document("Spring AI is amazing!", Map.of("author", "Alice")),
                new Document("Neo4j enables powerful graph queries.", Map.of("author", "Bob")),
                new Document("Embedding-based searches are efficient.", Map.of("author", "Carol"))
        );
        vectorStore.add(documents);
    }

    public List<Document> searchSimilarDocuments(String query) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.query("Spring").withTopK(1));
        return results;
    }

    @Value("classpath:/data/bikes.json")
    Resource bikesResouce;

    public List<Document> queryJSONVector(String query){

        // read json file
        JsonReader jsonReader = new JsonReader(bikesResouce, new ProductMetadataGenerator(),
                "name","shortDescription", "description", "price","tags");

        // create document object
        List<Document> documents = jsonReader.get();

        // add to vectorstore
        vectorStore.add(documents);

        // query vector search
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.defaults()
                        .withQuery(query)
                        .withTopK(1)
        );

        return results ;
    }


    public class ProductMetadataGenerator implements JsonMetadataGenerator {

        @Override
        public Map<String, Object> generate(Map<String, Object> jsonMap) {
            return Map.of("name", jsonMap.get("name"),
                    "shortDescription", jsonMap.get("shortDescription"));
        }

    }


    @Value("classpath:/data/supersetMetadata.json")
    Resource supersetResouce;

    public void addSupersetDocuments() {
        // read json file
        JsonReader jsonReader = new JsonReader(supersetResouce, new SupersetMetadataGenerator(),
                "name","query");

        // create document object
        List<Document> documents = jsonReader.get();

        // add to vectorstore
        vectorStore.add(documents);
    }

    public class SupersetMetadataGenerator implements JsonMetadataGenerator {

        @Override
        public Map<String, Object> generate(Map<String, Object> jsonMap) {
            return Map.of("name", jsonMap.get("name"),"query", jsonMap.get("query"));
        }

    }

    public Map querySupersetJSONVector(String query){
        return generateSQLQueryFromVectorSearch(query);
    }

    public Map generateSQLQueryFromVectorSearch(String query) {
        // Perform similarity search
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.defaults()
                        .withQuery(query)
                        .withTopK(2) // Get the most relevant document
        );

        if (results.isEmpty()) {
            String response = "No relevant information found for query" + query;
            return Map.of("sql_query", response);
        }

        for (Document doc : results) {
            System.out.println("Full Document: " + doc);
        }

        List<String> documentContents = results.stream()
                .map(Document::getContent)
                .toList();

        String documentContent = documentContents.get(0);
        System.out.println("Document Content: " + documentContent);

        PromptTemplate promptTemplate = new PromptTemplate("""
        Based on the following Superset chart metadata and user query, generate an optimized SQL query. 
        The SQL query should be formatted correctly, concise, and only contain the SQL statement with no extra commentary or formatting, including no newlines or tabs.
        
        Chart Information: {documentContent}
        User Query: {query}
        
        SQL Query:
        """);



        promptTemplate.add("documentContent", documentContent);
        promptTemplate.add("query", query);

        // Call LLM to generate SQL query
        ChatResponse response = chatModel.call(promptTemplate.create());

        // Extract only the SQL query from the response
        String sqlQuery = extractSQLQuery(response);

        return Map.of("sql_query", sqlQuery);

    }

    private String extractSQLQuery(ChatResponse response) {
        // Extract SQL query from LLM response
        String content = response.getResult().getOutput().getContent();

        // Remove markdown formatting (```sql ... ```)
        return content.replaceAll("```sql|```", "").trim();
    }


}
