package com.settlementservice.common.init;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.settlementservice.common.init.data.SettlementInitializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

	private final SettlementInitializer settlementInitializer;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("========================================");
		log.info("데이터 초기화 시작");
		log.info("========================================");
		settlementInitializer.init();

		log.info("========================================");
		log.info("데이터 초기화 완료!");
		log.info("========================================");
	}
}
