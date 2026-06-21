package com.smarttax.tax;

import static org.assertj.core.api.Assertions.assertThat;

import com.smarttax.profile.UserProfile;
import com.smarttax.profile.UserProfileRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TaxCalculationServiceTest {
    @Autowired
    private TaxCalculationService service;
    @Autowired
    private UserProfileRepository profileRepository;

    @Test
    void calculatesAndPersistsTaxRecord() {
        UserProfile profile = new UserProfile();
        profile.setFullName("Demo User");
        profile.setEmail("demo@example.com");
        UserProfile saved = profileRepository.save(profile);

        TaxCalculationResponse response = service.calculate(new TaxCalculationRequest(
                saved.getId(),
                new BigDecimal("1200000"),
                new BigDecimal("50000"),
                new BigDecimal("150000"),
                new BigDecimal("100000"),
                new BigDecimal("90000"),
                TaxRegime.OLD
        ));

        assertThat(response.recordId()).isNotNull();
        assertThat(response.taxableIncome()).isEqualByComparingTo("900000");
        assertThat(response.totalTax()).isPositive();
        assertThat(service.history(saved.getId())).hasSize(1);
    }
}
