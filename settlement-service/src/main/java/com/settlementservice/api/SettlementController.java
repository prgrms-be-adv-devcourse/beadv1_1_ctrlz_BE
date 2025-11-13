package com.settlementservice.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.settlementservice.domain.dto.addSettlementRequest;
import com.settlementservice.service.SettlementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlements")
public class SettlementController {
	private final SettlementService settlementService;

	@PostMapping
	public ResponseEntity addSettlement(
		@RequestBody addSettlementRequest request
	) {
		settlementService.createSettlements(request);
		return ResponseEntity.ok().build();
	}
}
