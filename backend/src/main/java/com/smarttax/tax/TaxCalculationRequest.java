package com.smarttax.tax;

import java.math.BigDecimal;

public record TaxCalculationRequest(
        Long profileId,
        BigDecimal annualIncome,
        BigDecimal standardDeduction,
        BigDecimal section80C,
        BigDecimal hraExemption,
        BigDecimal tdsPaid,
        TaxRegime regime
) {
}
