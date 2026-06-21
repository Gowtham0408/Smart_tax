package com.smarttax.tax;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tax")
public class TaxController {
    private final TaxCalculationService service;

    public TaxController(TaxCalculationService service) {
        this.service = service;
    }

    @PostMapping("/calculate")
    TaxCalculationResponse calculate(@RequestBody TaxCalculationRequest request) {
        return service.calculate(request);
    }

    @GetMapping("/history/{profileId}")
    List<TaxCalculationResponse> history(@PathVariable Long profileId) {
        return service.history(profileId);
    }
}
