package com.settlement.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.model.web.BaseResponse;
import com.settlement.dto.SettlementDto;
import com.settlement.service.SettlementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    /**
     * 정산 ID로 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<SettlementDto>> getSettlement(@PathVariable String id) {
        log.info("정산 조회 요청 id={}", id);
        SettlementDto settlement = settlementService.getSettlement(id);
        return ResponseEntity.ok(new BaseResponse<>(settlement, "정산 조회 성공"));
    }

    /**
     * 모든 정산 내역 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<BaseResponse<Page<SettlementDto>>> getAllSettlements(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("정산 목록 조회 요청 page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<SettlementDto> settlements = settlementService.getAllSettlements(pageable);
        return ResponseEntity.ok(new BaseResponse<>(settlements, "정산 목록 조회 성공"));
    }

    /**
     * 사용자별 정산 내역 조회
     */
    @GetMapping("/user")
    public ResponseEntity<BaseResponse<List<SettlementDto>>> getMySettlements(
            @RequestHeader(value = "X-REQUEST-ID") String userId) {
        log.info("사용자 정산 내역 조회 요청 userId={}", userId);
        List<SettlementDto> settlements = settlementService.getSettlementsByUserId(userId);
        return ResponseEntity.ok(new BaseResponse<>(settlements, "사용자 정산 내역 조회 성공"));
    }

    /**
     * 정산 삭제 (논리적 삭제)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteSettlement(@PathVariable String id) {
        log.info("정산 삭제 요청 id={}", id);
        settlementService.deleteSettlement(id);
        return ResponseEntity.ok(new BaseResponse<>(null, "정산 삭제 성공"));
    }
}
