package com.smarttax.recommendation;

import java.util.List;

public record RecommendationResponse(List<String> recommendations, String source) {
}
