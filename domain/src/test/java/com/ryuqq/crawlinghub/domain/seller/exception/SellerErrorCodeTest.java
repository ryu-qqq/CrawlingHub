package com.ryuqq.crawlinghub.domain.seller.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.domain.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Tag("unit")
@Tag("domain")
@Tag("exception")
@DisplayName("SellerErrorCode 단위 테스트")
class SellerErrorCodeTest {

    @Nested
    @DisplayName("ErrorCode 구현 검증")
    class ErrorCodeImplementation {

        @ParameterizedTest
        @EnumSource(SellerErrorCode.class)
        @DisplayName("모든 ErrorCode는 code를 가진다")
        void shouldHaveCodeForAllEnums(SellerErrorCode errorCode) {
            // When & Then
            assertThat(errorCode.getCode()).isNotBlank();
            assertThat(errorCode.getCode()).startsWith("SELLER-");
        }

        @ParameterizedTest
        @EnumSource(SellerErrorCode.class)
        @DisplayName("모든 ErrorCode는 유효한 HTTP 상태 코드를 가진다")
        void shouldHaveValidHttpStatusForAllEnums(SellerErrorCode errorCode) {
            // When & Then
            assertThat(errorCode.getHttpStatus()).isBetween(400, 599);
        }

        @ParameterizedTest
        @EnumSource(SellerErrorCode.class)
        @DisplayName("모든 ErrorCode는 메시지를 가진다")
        void shouldHaveMessageForAllEnums(SellerErrorCode errorCode) {
            // When & Then
            assertThat(errorCode.getMessage()).isNotBlank();
        }

        @ParameterizedTest
        @EnumSource(SellerErrorCode.class)
        @DisplayName("모든 SellerErrorCode는 ErrorCode 인터페이스를 구현한다")
        void shouldImplementErrorCodeInterface(SellerErrorCode errorCode) {
            // When & Then
            assertThat(errorCode).isInstanceOf(ErrorCode.class);
        }
    }

    @Nested
    @DisplayName("개별 ErrorCode 값 검증")
    class IndividualErrorCodeValues {

        @Test
        @DisplayName("DUPLICATE_MUST_IT_SELLER_ID는 올바른 값을 가진다")
        void shouldHaveCorrectValuesForDuplicateMustItSellerId() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.DUPLICATE_MUST_IT_SELLER_ID;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("SELLER-001");
            assertThat(errorCode.getHttpStatus()).isEqualTo(409);
            assertThat(errorCode.getMessage()).contains("머스트잇");
        }

        @Test
        @DisplayName("DUPLICATE_SELLER_NAME은 올바른 값을 가진다")
        void shouldHaveCorrectValuesForDuplicateSellerName() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.DUPLICATE_SELLER_NAME;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("SELLER-002");
            assertThat(errorCode.getHttpStatus()).isEqualTo(409);
            assertThat(errorCode.getMessage()).contains("이미 등록된");
        }

        @Test
        @DisplayName("SELLER_HAS_ACTIVE_SCHEDULERS는 올바른 값을 가진다")
        void shouldHaveCorrectValuesForActiveSchedulers() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.SELLER_HAS_ACTIVE_SCHEDULERS;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("SELLER-003");
            assertThat(errorCode.getHttpStatus()).isEqualTo(400);
            assertThat(errorCode.getMessage()).contains("스케줄러");
        }

        @Test
        @DisplayName("SELLER_NOT_FOUND는 올바른 값을 가진다")
        void shouldHaveCorrectValuesForNotFound() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.SELLER_NOT_FOUND;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("SELLER-004");
            assertThat(errorCode.getHttpStatus()).isEqualTo(404);
            assertThat(errorCode.getMessage()).contains("존재하지 않는");
        }
    }

    @Nested
    @DisplayName("ErrorCode 고유성 검증")
    class ErrorCodeUniqueness {

        @Test
        @DisplayName("모든 ErrorCode는 고유한 code를 가진다")
        void shouldHaveUniqueCodeForAllEnums() {
            // Given
            SellerErrorCode[] errorCodes = SellerErrorCode.values();

            // When & Then
            long uniqueCount =
                    java.util.Arrays.stream(errorCodes)
                            .map(SellerErrorCode::getCode)
                            .distinct()
                            .count();

            assertThat(uniqueCount).isEqualTo(errorCodes.length);
        }
    }
}
