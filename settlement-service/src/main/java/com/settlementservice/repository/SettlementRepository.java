package com.settlementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settlementservice.domain.entity.Settlement;

public interface SettlementRepository extends JpaRepository<Settlement, String> {
}
