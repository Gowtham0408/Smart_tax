package com.smarttax.document;

import java.math.BigDecimal;
import java.util.Map;

public record DocumentUploadResponse(
        String fileName,
        String documentType,
        BigDecimal annualIncome,
        BigDecimal section80C,
        BigDecimal hraExemption,
        BigDecimal tdsPaid,
        Map<String, String> rawFields
) {
}
