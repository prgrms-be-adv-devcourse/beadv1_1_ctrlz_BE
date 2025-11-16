package com.accountapplication.user.infrastructure.jpa.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.user.domain.vo.EventType;
import com.user.infrastructure.jpa.converter.AESUtils;
import com.user.infrastructure.jpa.entity.ExternalEventEntity;
import com.user.infrastructure.jpa.repository.ExternalEventJpaRepository;

@Import({AESUtils.class})
@DataJpaTest
@ActiveProfiles("test")
class ExternalEventJpaRepositoryTest {

    @Autowired
    private ExternalEventJpaRepository externalEventJpaRepository;

    @DisplayName("발행되지 않은 이벤트들을 생성 시간기준으로 20개 조회한다.")
    @Test
    void test1() {
        // given
        String testId = UUID.randomUUID().toString();

        List<ExternalEventEntity> events = new ArrayList<>();

        for (int i = 0; i < 25; i++) {
            events.add(ExternalEventEntity.builder()
                    .userId(testId)
                    .eventType(EventType.CREATED)
                    .content("test " + i)
                    .published(false)
                    .createdAt(LocalDateTime.now().plusSeconds(i))
                    .build());
        }

        for (int i = 25; i < 30; i++) {
            events.add(ExternalEventEntity.builder()
                    .userId(testId)
                    .eventType(EventType.CREATED)
                    .content("payload " + i)
                    .published(true)
                    .createdAt(LocalDateTime.now().plusSeconds(i))
                    .build());
        }
        externalEventJpaRepository.saveAll(events);

        // when
        List<ExternalEventEntity> foundEvents = externalEventJpaRepository.findTop20ByPublishedOrderByCreatedAt(false);

        // then
        assertThat(foundEvents).hasSize(20);
        assertThat(foundEvents).allMatch(event -> !event.isPublished());
        assertThat(foundEvents).isSortedAccordingTo(Comparator.comparing(ExternalEventEntity::getCreatedAt));
        assertThat(foundEvents.get(0).getContent()).isEqualTo("test 0");
        assertThat(foundEvents.get(19).getContent()).isEqualTo("test 19");
    }
}
