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
import com.settlement.dto.SettlementResponse;
import com.settlement.service.SettlementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/**
 * 특정 ip만 허용합니다.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/{id}")
    public BaseResponse<SettlementResponse> getSettlement(@PathVariable String id) {
        log.info("정산 조회 요청 id={}", id);
        SettlementResponse settlement = settlementService.getSettlement(id);
        return new BaseResponse<>(settlement, "정산 조회 성공");
    }

    @GetMapping
    public BaseResponse<Page<SettlementResponse>> getAllSettlements(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("정산 목록 조회 요청 page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<SettlementResponse> settlements = settlementService.getAllSettlements(pageable);
        return new BaseResponse<>(settlements, "정산 목록 조회 성공");
    }

    @GetMapping("/user")
    public BaseResponse<List<SettlementResponse>> getMySettlements(
            @RequestHeader(value = "X-REQUEST-ID") String userId) {
        log.info("사용자 정산 내역 조회 요청 userId={}", userId);
        List<SettlementResponse> settlements = settlementService.getSettlementsByUserId(userId);
        return new BaseResponse<>(settlements, "사용자 정산 내역 조회 성공");
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> deleteSettlement(@PathVariable String id) {
        log.info("정산 삭제 요청 id={}", id);
        settlementService.deleteSettlement(id);
        return new BaseResponse<>(null, "정산 삭제 성공");
    }
}
