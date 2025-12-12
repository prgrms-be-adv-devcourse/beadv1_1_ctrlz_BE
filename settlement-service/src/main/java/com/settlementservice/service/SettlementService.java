package com.settlement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.exception.CustomException;
import com.settlement.domain.entity.Settlement;
import com.settlement.dto.SettlementResponse;
import com.settlement.repository.SettlementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

	private final SettlementRepository settlementRepository;

	@Transactional(readOnly = true)
	public SettlementResponse getSettlement(String id) {
		Settlement settlement = settlementRepository.findById(id)
				.orElseThrow(() -> new CustomException("정산 내역을 찾을 수 없습니다. id=" + id));
		return SettlementResponse.from(settlement);
	}

	@Transactional(readOnly = true)
	public Page<SettlementResponse> getAllSettlements(Pageable pageable) {
		return settlementRepository.findAll(pageable)
				.map(SettlementResponse::from);
	}

	@Transactional(readOnly = true)
	public List<SettlementResponse> getSettlementsByUserId(String userId) {
		return settlementRepository.findAll().stream()
				.filter(s -> s.getUserId().equals(userId))
				.map(SettlementResponse::from)
				.collect(Collectors.toList());
	}

	@Transactional
	public void deleteSettlement(String id) {
		Settlement settlement = settlementRepository.findById(id)
				.orElseThrow(() -> new CustomException("정산 내역을 찾을 수 없습니다. id=" + id));
		settlement.delete();
		log.info("정산 삭제 처리 완료 id={}", id);
	}

}