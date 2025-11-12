package com.domainservice.common.init;

import com.domainservice.common.init.data.CategoryInitializer;
import com.domainservice.common.init.data.ProductPostInitializer;
import com.domainservice.common.init.data.TagInitializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CategoryInitializer categoryInitializer;
    private final TagInitializer tagInitializer;
    private final ProductPostInitializer productPostInitializer;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("========================================");
        log.info("데이터 초기화 시작");
        log.info("========================================");

        categoryInitializer.init();
        tagInitializer.init();
        // productPostInitializer.init(); 유저 더미데이터는 없음, 임시 주석처리함

        log.info("========================================");
        log.info("데이터 초기화 완료!");
        log.info("========================================");
    }
}
