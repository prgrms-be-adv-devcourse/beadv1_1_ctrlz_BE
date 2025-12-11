package com.settlement.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.event.SettlementCompletedEvent;
import com.common.event.SettlementFailedEvent;
import com.common.exception.CustomException;
import com.settlement.domain.entity.Settlement;
import com.settlement.domain.entity.SettlementStatus;
import com.settlement.dto.SettlementDto;
import com.settlement.repository.SettlementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SettlementService {
	private final SettlementRepository settlementRepository;

	public static final BigDecimal FEE_RATE = new BigDecimal("0.1");

	@Transactional(readOnly = true)
	public SettlementDto getSettlement(String id) {
		Settlement settlement = settlementRepository.findById(id)
				.orElseThrow(() -> new CustomException("정산 내역을 찾을 수 없습니다. id=" + id));
		return SettlementDto.from(settlement);
	}

	@Transactional(readOnly = true)
	public Page<SettlementDto> getAllSettlements(Pageable pageable) {
		return settlementRepository.findAll(pageable)
				.map(SettlementDto::from);
	}

	@Transactional(readOnly = true)
	public List<SettlementDto> getSettlementsByUserId(String userId) {
		return settlementRepository.findAll().stream()
				.filter(s -> s.getUserId().equals(userId))
				.map(SettlementDto::from)
				.collect(Collectors.toList());
	}

	public void deleteSettlement(String id) {
		Settlement settlement = settlementRepository.findById(id)
				.orElseThrow(() -> new CustomException("정산 내역을 찾을 수 없습니다. id=" + id));
		settlement.delete();
		log.info("정산 삭제 처리 완료 id={}", id);
	}

}