package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ScheduleId VO 테스트
 *
 * TDD Phase: Red → Green
 * - UUID 고유성 검증
 * - forNew() 정적 팩토리 메서드 검증
 * - isNew() 메서드 검증
 */
class ScheduleIdTest {

    @Test
    void shouldGenerateUniqueScheduleId() {
        ScheduleId id1 = ScheduleId.generate();
        ScheduleId id2 = ScheduleId.generate();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldCreateIdUsingForNew() {
        // When
        ScheduleId id = ScheduleId.forNew();

        // Then
        assertThat(id).isNotNull();
        assertThat(id.value()).isNotNull();
    }

    @Test
    void shouldReturnTrueForIsNew() {
        // Given
        ScheduleId id = ScheduleId.forNew();

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldCreateIdFromExistingValue() {
        // Given
        ScheduleId originalId = ScheduleId.generate();

        // When
        ScheduleId reconstructedId = new ScheduleId(originalId.value());

        // Then
        assertThat(reconstructedId).isEqualTo(originalId);
        assertThat(reconstructedId.value()).isEqualTo(originalId.value());
    }

    @Test
    void shouldBeEqualWhenSameValue() {
        // Given
        ScheduleId id1 = ScheduleId.generate();
        ScheduleId id2 = new ScheduleId(id1.value());

        // When & Then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
