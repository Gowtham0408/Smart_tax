package com.smarttax.recommendation;

import java.math.BigDecimal;

public record RecommendationRequest(
        BigDecimal annualIncome,
        BigDecimal taxableIncome,
        BigDecimal totalTax,
        String regime,
        String userQuestion
) {
}
