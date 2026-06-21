package com.smarttax.tax;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRecordRepository extends JpaRepository<TaxRecord, Long> {
    List<TaxRecord> findByProfileIdOrderByCalculatedAtDesc(Long profileId);
}
