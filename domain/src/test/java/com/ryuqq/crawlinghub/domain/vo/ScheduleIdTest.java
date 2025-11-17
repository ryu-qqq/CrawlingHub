package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ScheduleId VO 테스트
 *
 * TDD Phase: Red → Green
 * - Long 기반 auto-increment ID
 * - forNew() 정적 팩토리 메서드 검증
 * - isNew() 메서드 검증
 */
class ScheduleIdTest {

    @Test
    void shouldCreateNewIdWithNullValue() {
        // When
        ScheduleId id = ScheduleId.forNew();

        // Then
        assertThat(id).isNotNull();
        assertThat(id.value()).isNull();
    }

    @Test
    void shouldReturnTrueForIsNewWhenValueIsNull() {
        // Given
        ScheduleId id = ScheduleId.forNew();

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseForIsNewWhenValueIsNotNull() {
        // Given
        ScheduleId id = new ScheduleId(1L);

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldCreateIdWithSpecificValue() {
        // When
        ScheduleId id = new ScheduleId(123L);

        // Then
        assertThat(id.value()).isEqualTo(123L);
        assertThat(id.isNew()).isFalse();
    }

    @Test
    void shouldCreateIdFromExistingValue() {
        // Given
        Long value = 456L;

        // When
        ScheduleId id = new ScheduleId(value);

        // Then
        assertThat(id.value()).isEqualTo(value);
        assertThat(id.isNew()).isFalse();
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        // Given
        ScheduleId id1 = new ScheduleId(789L);
        ScheduleId id2 = new ScheduleId(789L);

        // When & Then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
