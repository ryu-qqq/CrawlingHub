package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SchedulerOutboxId VO 테스트
 *
 * TDD Phase: Red → Green
 * - UUID 고유성 검증
 * - forNew() 정적 팩토리 메서드 검증
 * - isNew() 메서드 검증
 */
class SchedulerOutboxIdTest {

    @Test
    void shouldGenerateUniqueSchedulerOutboxId() {
        SchedulerOutboxId id1 = SchedulerOutboxId.generate();
        SchedulerOutboxId id2 = SchedulerOutboxId.generate();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldCreateIdUsingForNew() {
        // When
        SchedulerOutboxId id = SchedulerOutboxId.forNew();

        // Then
        assertThat(id).isNotNull();
        assertThat(id.value()).isNotNull();
    }

    @Test
    void shouldReturnTrueForIsNew() {
        // Given
        SchedulerOutboxId id = SchedulerOutboxId.forNew();

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldCreateIdFromExistingValue() {
        // Given
        SchedulerOutboxId originalId = SchedulerOutboxId.generate();

        // When
        SchedulerOutboxId reconstructedId = new SchedulerOutboxId(originalId.value());

        // Then
        assertThat(reconstructedId).isEqualTo(originalId);
        assertThat(reconstructedId.value()).isEqualTo(originalId.value());
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        // Given
        SchedulerOutboxId id1 = SchedulerOutboxId.generate();
        SchedulerOutboxId id2 = new SchedulerOutboxId(id1.value());

        // When & Then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
