package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.product.vo.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ItemNo VO 테스트
 *
 * TDD Phase: Red
 * - 양수 검증
 */
class ItemNoTest {

    @Test
    void shouldCreateItemNoWithValidValue() {
        Long validValue = 123456L;
        ItemNo itemNo = new ItemNo(validValue);
        assertThat(itemNo.value()).isEqualTo(validValue);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -100})
    void shouldThrowExceptionWhenItemNoIsNotPositive(Long invalidValue) {
        assertThatThrownBy(() -> new ItemNo(invalidValue))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ItemNo는 양수여야 합니다");
    }

    @Test
    void shouldThrowExceptionWhenItemNoIsNull() {
        assertThatThrownBy(() -> new ItemNo(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ItemNo는 양수여야 합니다");
    }
}
