package com.subex.embeddedAI.controller;

import com.subex.embeddedAI.service.AIService;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AIController {

    @Autowired
    AIService aiService;

    @Autowired
    private ApplicationContext applicationContext;

    private final OllamaChatModel chatModel;

    private final EmbeddingModel embeddingModel;

    @Autowired
    public AIController(EmbeddingModel embeddingModel, OllamaChatModel chatModel) {
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
    }

    @GetMapping("/debug/beans")
    public String[] listBeans() {
        return applicationContext.getBeanNamesForType(EmbeddingModel.class);
    }

    @GetMapping("/jokes")
    public Map getJokes() {
        return aiService.getJokes();
    }

    @GetMapping("/books")
    public Map getBook(@RequestParam String category, @RequestParam String year) {
        return aiService.getBook(category, year);
    }

    @GetMapping("/add-documents")
    public String addDocuments() {
        aiService.addDocuments();
        return "Documents added successfully!";
    }

    @GetMapping("/search")
    public List<String> searchSimilarDocuments(@RequestParam String query) {
        return aiService.searchSimilarDocuments(query).stream()
                .map(doc -> doc.getContent())
                .toList();
    }

    @GetMapping("/ai/embedding")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }

    @GetMapping("/ai/generate")
    public Map<String,String> generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

//    @GetMapping("/query")
//    public String getQueryResults(@RequestParam String query){
//        return aiService.queryJSONVector(query).get(0).getMetadata().toString();
//    }

    @GetMapping("/query")
    public List<String> getQueryResults(@RequestParam String query){
        return aiService.queryJSONVector(query).stream().map(doc -> doc.getContent()).toList();
    }

    @GetMapping("/add-supersetdocument")
    public String addSuperSetDocuments(){
        aiService.addSupersetDocuments();
        return "Documents added successfully!";
    }

    @GetMapping("/supersetquery")
    public Map getSuperSetQueryResults(@RequestParam String query){
        return aiService.querySupersetJSONVector(query);
    }

}
