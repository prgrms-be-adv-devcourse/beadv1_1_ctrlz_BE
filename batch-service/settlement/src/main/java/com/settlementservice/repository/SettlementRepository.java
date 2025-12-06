package com.settlementservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settlementservice.domain.entity.Settlement;
import com.settlementservice.domain.entity.SettlementStatus;

public interface SettlementRepository extends JpaRepository<Settlement, String> {

	List<Settlement> findTop100BySettlementStatusOrderByCreatedAtAsc(SettlementStatus status);
}
