package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SchedulerOutboxId VO 테스트
 *
 * TDD Phase: Red → Green
 * - Long 기반 auto-increment ID
 * - forNew() 정적 팩토리 메서드 검증
 * - isNew() 메서드 검증
 */
class SchedulerOutboxIdTest {

    @Test
    void shouldCreateNewIdWithNullValue() {
        // When
        SchedulerOutboxId id = SchedulerOutboxId.forNew();

        // Then
        assertThat(id).isNotNull();
        assertThat(id.value()).isNull();
    }

    @Test
    void shouldReturnTrueForIsNewWhenValueIsNull() {
        // Given
        SchedulerOutboxId id = SchedulerOutboxId.forNew();

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseForIsNewWhenValueIsNotNull() {
        // Given
        SchedulerOutboxId id = new SchedulerOutboxId(1L);

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldCreateIdWithSpecificValue() {
        // When
        SchedulerOutboxId id = new SchedulerOutboxId(123L);

        // Then
        assertThat(id.value()).isEqualTo(123L);
        assertThat(id.isNew()).isFalse();
    }

    @Test
    void shouldCreateIdFromExistingValue() {
        // Given
        Long value = 456L;

        // When
        SchedulerOutboxId id = new SchedulerOutboxId(value);

        // Then
        assertThat(id.value()).isEqualTo(value);
        assertThat(id.isNew()).isFalse();
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        // Given
        SchedulerOutboxId id1 = new SchedulerOutboxId(789L);
        SchedulerOutboxId id2 = new SchedulerOutboxId(789L);

        // When & Then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
