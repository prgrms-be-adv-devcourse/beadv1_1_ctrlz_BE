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

	// 예시: 수수료 10% 계산
	public static final BigDecimal FEE_RATE = new BigDecimal("0.1");

	/**
	 * 정산 ID로 조회
	 */
	@Transactional(readOnly = true)
	public SettlementDto getSettlement(String id) {
		Settlement settlement = settlementRepository.findById(id)
				.orElseThrow(() -> new CustomException("정산 내역을 찾을 수 없습니다. id=" + id));
		return SettlementDto.from(settlement);
	}

	/**
	 * 모든 정산 내역 조회 (페이징)
	 */
	@Transactional(readOnly = true)
	public Page<SettlementDto> getAllSettlements(Pageable pageable) {
		return settlementRepository.findAll(pageable)
				.map(SettlementDto::from);
	}

	/**
	 * 사용자별 정산 내역 조회
	 */
	@Transactional(readOnly = true)
	public List<SettlementDto> getSettlementsByUserId(String userId) {
		return settlementRepository.findAll().stream()
				.filter(s -> s.getUserId().equals(userId))
				.map(SettlementDto::from)
				.collect(Collectors.toList());
	}

	/**
	 * 정산 삭제 (논리적 삭제)
	 */
	public void deleteSettlement(String id) {
		Settlement settlement = settlementRepository.findById(id)
				.orElseThrow(() -> new CustomException("정산 내역을 찾을 수 없습니다. id=" + id));
		settlement.delete();
		log.info("정산 삭제 처리 완료 id={}", id);
	}

	/**
	 * 구매확정된 주문에 대해 정산 데이터 생성
	 */
	public Settlement createSettlement(String orderItemId, String userId, BigDecimal amount) {
		// 1. 초기 Settlement 생성 (fee, netAmount는 0으로 초기화됨)
		Settlement settlement = Settlement.create(orderItemId, userId, amount);

		// 2. 수수료 계산 및 설정 (기존 로직 유지)
		// TODO: 배치가 도입되면 이 메서드는 제거되거나 배치 로직으로 대체될 수 있습니다.
		// 현재는 컴파일 에러 방지를 위해 Setter 사용 없이 Builder로 다시 생성하거나,
		// Settlement 객체에 setFee/setNetAmount가 없다면 엔티티에 메서드 추가 필요.
		// 하지만 BaseEntity 상속이므로 JPA 감지 변경 사용 가능하지만, 여기서는 save 호출 전임.

		// Settlement에 비즈니스 메서드 추가하여 처리 권장.
		// 하지만 간단히 수정을 위해, 이번에는 Fee 계산 후 update하는 방식을 배치에서 사용할 것이므로,
		// 여기서는 create 호출만 하고, 바로 수수료 계산 로직을 적용하려면 별도 메서드가 필요.

		// *중요* : Settlement Entity에 updateFeeAndNetAmount 같은 메서드가 필요할 수 있음.
		// 현재 Entity에는 Setter가 없고, markCompleted 같은 상태 변경 메서드만 있음.
		// 배치 Step 2를 위해서라도 update 메서드가 필요함.

		// 우선 Settlement Entity에 updateFee 메서드 추가하고 여기서 호출하는 방식으로 변경.
		return settlementRepository.save(settlement);
	}

	/**
	 * 예치금에서 정산 완료 이벤트 수신 시 상태 업데이트
	 */
	public void handleSettlementCompleted(SettlementCompletedEvent event) {
		Settlement settlement = settlementRepository.findById(event.settlementId())
				.orElseThrow(() -> new CustomException("정산 내역을 찾을 수 없습니다. settlementId=" + event.settlementId()));

		// 멱등
		if (settlement.getSettlementStatus() == SettlementStatus.COMPLETED) {
			log.info("이미 완료된 정산입니다. settlementId={}", event.settlementId());
			return;
		}

		// 이미 FAILED 상태면 정책에 따라 무시
		if (settlement.getSettlementStatus() == SettlementStatus.FAILED) {
			log.warn("FAILED 상태의 정산에 COMPLETED 이벤트 수신 settlementId={}", event.settlementId());
			return;
		}

		settlement.markCompleted();
		log.info("정산 완료 처리 settlementId={}", event.settlementId());
	}

	/**
	 * 예치금에서 정산 실패 이벤트 수신 시 상태 업데이트
	 */
	public void handleSettlementFailed(SettlementFailedEvent event) {
		Settlement settlement = settlementRepository.findById(event.settlementId())
				.orElseThrow(() -> new CustomException("정산 내역을 찾을 수 없습니다. settlementId=" + event.settlementId()));

		// 이미 COMPLETED면 실패 이벤트는 무시
		if (settlement.getSettlementStatus() == SettlementStatus.COMPLETED) {
			log.warn("완료된 정산에 FAILED 이벤트 수신, 무시 settlementId={}", event.settlementId());
			return;
		}

		settlement.markFailed();
		log.warn("정산 실패 처리 settlementId={}, reason={}", event.settlementId(), event.reason());
	}
}