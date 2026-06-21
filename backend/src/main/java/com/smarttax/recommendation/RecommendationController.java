package com.smarttax.recommendation;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final String ollamaUrl;
    private final String ollamaModel;

    public RecommendationController(
            @Value("${smarttax.ollama-url}") String ollamaUrl,
            @Value("${smarttax.ollama-model}") String ollamaModel
    ) {
        this.ollamaUrl = ollamaUrl;
        this.ollamaModel = ollamaModel;
    }

    @PostMapping
    RecommendationResponse recommend(@RequestBody RecommendationRequest request) {
        List<String> recommendations = new ArrayList<>();
        recommendations.add("Keep Form 16, rent receipts, investment proofs, and salary slips in one folder before filing.");
        recommendations.add("Run both old and new regime scenarios before selecting the filing option.");
        recommendations.add("Review TDS, deductions, and exemptions quarterly instead of waiting until year end.");

        if (request.userQuestion() != null && !request.userQuestion().isBlank()) {
            recommendations.add("Question received: " + request.userQuestion() + ". In production this can be sent to a local Ollama model.");
        }

        String source = "rule-based-fallback; ollama-ready at " + ollamaUrl + " using model " + ollamaModel;
        return new RecommendationResponse(recommendations, source);
    }
}
