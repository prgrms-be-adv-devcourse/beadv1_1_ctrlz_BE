package com.settlement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settlement.domain.entity.Settlement;

public interface SettlementRepository extends JpaRepository<Settlement, String> {
}
