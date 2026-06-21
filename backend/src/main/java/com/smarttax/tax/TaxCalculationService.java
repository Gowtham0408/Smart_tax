package com.smarttax.tax;

import com.smarttax.profile.UserProfile;
import com.smarttax.profile.UserProfileRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TaxCalculationService {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal CESS_RATE = new BigDecimal("0.04");

    private final UserProfileRepository profileRepository;
    private final TaxRecordRepository taxRecordRepository;

    public TaxCalculationService(UserProfileRepository profileRepository, TaxRecordRepository taxRecordRepository) {
        this.profileRepository = profileRepository;
        this.taxRecordRepository = taxRecordRepository;
    }

    public TaxCalculationResponse calculate(TaxCalculationRequest request) {
        BigDecimal annualIncome = value(request.annualIncome());
        TaxRegime regime = request.regime() == null ? TaxRegime.NEW : request.regime();
        BigDecimal taxableIncome = taxableIncome(request, annualIncome, regime);
        BigDecimal taxBeforeCess = regime == TaxRegime.OLD ? oldRegimeTax(taxableIncome) : newRegimeTax(taxableIncome);
        BigDecimal cess = taxBeforeCess.multiply(CESS_RATE).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalTax = taxBeforeCess.add(cess);
        BigDecimal refundOrPayable = totalTax.subtract(value(request.tdsPaid()));

        TaxRecord record = new TaxRecord();
        UserProfile profile = profileRepository.findById(request.profileId()).orElseThrow();
        record.setProfile(profile);
        record.setAnnualIncome(annualIncome);
        record.setTaxableIncome(taxableIncome);
        record.setTotalTax(totalTax);
        record.setRefundOrPayable(refundOrPayable);
        record.setRegime(regime);
        record.setCalculatedAt(Instant.now());
        TaxRecord saved = taxRecordRepository.save(record);

        return new TaxCalculationResponse(
                saved.getId(),
                taxableIncome,
                taxBeforeCess,
                cess,
                totalTax,
                refundOrPayable,
                regime,
                saved.getCalculatedAt(),
                recommendations(request, taxableIncome, totalTax)
        );
    }

    public List<TaxCalculationResponse> history(Long profileId) {
        return taxRecordRepository.findByProfileIdOrderByCalculatedAtDesc(profileId).stream()
                .map(record -> new TaxCalculationResponse(
                        record.getId(),
                        record.getTaxableIncome(),
                        record.getTotalTax(),
                        ZERO,
                        record.getTotalTax(),
                        record.getRefundOrPayable(),
                        record.getRegime(),
                        record.getCalculatedAt(),
                        List.of()
                ))
                .toList();
    }

    private BigDecimal taxableIncome(TaxCalculationRequest request, BigDecimal annualIncome, TaxRegime regime) {
        BigDecimal deduction = value(request.standardDeduction());
        if (regime == TaxRegime.OLD) {
            deduction = deduction.add(value(request.section80C())).add(value(request.hraExemption()));
        }
        return annualIncome.subtract(deduction).max(ZERO).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal oldRegimeTax(BigDecimal income) {
        BigDecimal tax = ZERO;
        tax = tax.add(taxForSlab(income, 250000, 500000, "0.05"));
        tax = tax.add(taxForSlab(income, 500000, 1000000, "0.20"));
        tax = tax.add(taxForSlab(income, 1000000, Integer.MAX_VALUE, "0.30"));
        return tax.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal newRegimeTax(BigDecimal income) {
        BigDecimal tax = ZERO;
        tax = tax.add(taxForSlab(income, 300000, 600000, "0.05"));
        tax = tax.add(taxForSlab(income, 600000, 900000, "0.10"));
        tax = tax.add(taxForSlab(income, 900000, 1200000, "0.15"));
        tax = tax.add(taxForSlab(income, 1200000, 1500000, "0.20"));
        tax = tax.add(taxForSlab(income, 1500000, Integer.MAX_VALUE, "0.30"));
        return tax.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal taxForSlab(BigDecimal income, int from, int to, String rate) {
        BigDecimal lower = BigDecimal.valueOf(from);
        BigDecimal upper = BigDecimal.valueOf(to);
        if (income.compareTo(lower) <= 0) {
            return ZERO;
        }
        return income.min(upper).subtract(lower).multiply(new BigDecimal(rate));
    }

    private List<String> recommendations(TaxCalculationRequest request, BigDecimal taxableIncome, BigDecimal tax) {
        List<String> items = new ArrayList<>();
        if (request.regime() == TaxRegime.OLD && value(request.section80C()).compareTo(new BigDecimal("150000")) < 0) {
            items.add("Increase eligible Section 80C investments up to the statutory limit to reduce taxable income.");
        }
        if (value(request.tdsPaid()).compareTo(tax) < 0) {
            items.add("TDS paid is lower than estimated tax. Plan advance tax or salary TDS adjustment.");
        } else {
            items.add("TDS paid covers the estimated liability. Review refund timing and keep Form 16 ready.");
        }
        if (taxableIncome.compareTo(new BigDecimal("700000")) > 0) {
            items.add("Compare old and new regimes before filing because deductions can change the best option.");
        }
        return items;
    }

    private BigDecimal value(BigDecimal input) {
        return input == null ? ZERO : input;
    }
}
