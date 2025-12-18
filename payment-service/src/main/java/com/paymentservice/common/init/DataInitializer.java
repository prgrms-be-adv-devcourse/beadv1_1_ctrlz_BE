package com.paymentservice.common.init;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.paymentservice.common.init.data.DepositInitializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final DepositInitializer depositInitializer;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("========================================");
		log.info("데이터 초기화 시작");
		log.info("========================================");


        depositInitializer.init();


		log.info("========================================");
		log.info("데이터 초기화 완료!");
		log.info("========================================");
	}
}
