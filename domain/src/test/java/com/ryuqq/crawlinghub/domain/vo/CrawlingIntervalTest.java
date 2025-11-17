package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.seller.vo.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CrawlingInterval Value Object 테스트
 *
 * TDD Phase: Red
 * - 유효한 크롤링 주기 생성 검증 (1-30일)
 * - 범위 외 값 검증 (0, 31, -1)
 */
class CrawlingIntervalTest {

    @Test
    void shouldCreateCrawlingIntervalWithValidDays() {
        CrawlingInterval interval = new CrawlingInterval(7);
        assertThat(interval.days()).isEqualTo(7);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 31, -1})
    void shouldThrowExceptionWhenDaysOutOfRange(int invalidDays) {
        assertThatThrownBy(() -> new CrawlingInterval(invalidDays))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("크롤링 주기는 1-30일 사이여야 합니다");
    }
}
