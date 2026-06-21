package com.smarttax.tax;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TaxCalculationResponse(
        Long recordId,
        BigDecimal taxableIncome,
        BigDecimal taxBeforeCess,
        BigDecimal cess,
        BigDecimal totalTax,
        BigDecimal refundOrPayable,
        TaxRegime regime,
        Instant calculatedAt,
        List<String> recommendations
) {
}
