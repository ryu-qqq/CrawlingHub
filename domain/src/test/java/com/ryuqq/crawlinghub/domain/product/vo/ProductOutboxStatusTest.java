package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * ProductOutboxStatus Enum 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ProductOutboxStatus 테스트")
class ProductOutboxStatusTest {

    @ParameterizedTest
    @EnumSource(ProductOutboxStatus.class)
    @DisplayName("모든 상태는 유효한 enum 값")
    void shouldHaveValidEnumValues(ProductOutboxStatus status) {
        // given & when & then
        assertThat(status.name()).isNotBlank();
    }

    @Test
    @DisplayName("PENDING 상태 확인")
    void shouldCheckPending() {
        // given & when & then
        assertThat(ProductOutboxStatus.PENDING.isPending()).isTrue();
        assertThat(ProductOutboxStatus.PROCESSING.isPending()).isFalse();
        assertThat(ProductOutboxStatus.COMPLETED.isPending()).isFalse();
        assertThat(ProductOutboxStatus.FAILED.isPending()).isFalse();
    }

    @Test
    @DisplayName("PROCESSING 상태 확인")
    void shouldCheckProcessing() {
        // given & when & then
        assertThat(ProductOutboxStatus.PENDING.isProcessing()).isFalse();
        assertThat(ProductOutboxStatus.PROCESSING.isProcessing()).isTrue();
        assertThat(ProductOutboxStatus.COMPLETED.isProcessing()).isFalse();
        assertThat(ProductOutboxStatus.FAILED.isProcessing()).isFalse();
    }

    @Test
    @DisplayName("COMPLETED 상태 확인")
    void shouldCheckCompleted() {
        // given & when & then
        assertThat(ProductOutboxStatus.PENDING.isCompleted()).isFalse();
        assertThat(ProductOutboxStatus.PROCESSING.isCompleted()).isFalse();
        assertThat(ProductOutboxStatus.COMPLETED.isCompleted()).isTrue();
        assertThat(ProductOutboxStatus.FAILED.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("FAILED 상태 확인")
    void shouldCheckFailed() {
        // given & when & then
        assertThat(ProductOutboxStatus.PENDING.isFailed()).isFalse();
        assertThat(ProductOutboxStatus.PROCESSING.isFailed()).isFalse();
        assertThat(ProductOutboxStatus.COMPLETED.isFailed()).isFalse();
        assertThat(ProductOutboxStatus.FAILED.isFailed()).isTrue();
    }

    @Test
    @DisplayName("재시도 가능 상태 확인")
    void shouldCheckCanRetry() {
        // given & when & then
        assertThat(ProductOutboxStatus.PENDING.canRetry()).isTrue();
        assertThat(ProductOutboxStatus.PROCESSING.canRetry()).isFalse();
        assertThat(ProductOutboxStatus.COMPLETED.canRetry()).isFalse();
        assertThat(ProductOutboxStatus.FAILED.canRetry()).isTrue();
    }
}
