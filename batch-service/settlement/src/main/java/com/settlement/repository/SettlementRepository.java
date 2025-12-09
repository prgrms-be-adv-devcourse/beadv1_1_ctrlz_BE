package com.settlement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settlement.domain.entity.Settlement;
import com.settlement.domain.entity.SettlementStatus;

public interface SettlementRepository extends JpaRepository<Settlement, String> {

	List<Settlement> findTop100BySettlementStatusOrderByCreatedAtAsc(SettlementStatus status);
}
