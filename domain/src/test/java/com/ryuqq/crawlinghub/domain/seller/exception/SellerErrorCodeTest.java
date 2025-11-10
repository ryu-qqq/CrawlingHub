package com.ryuqq.crawlinghub.domain.seller.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SellerErrorCode 테스트")
class SellerErrorCodeTest {

    @Nested
    @DisplayName("Enum 값 테스트")
    class EnumValueTests {

        @Test
        @DisplayName("SELLER_NOT_FOUND 상수 존재")
        void shouldHaveSellerNotFound() {
            // Given & When
            SellerErrorCode errorCode = SellerErrorCode.SELLER_NOT_FOUND;

            // Then
            assertThat(errorCode).isNotNull();
            assertThat(errorCode.name()).isEqualTo("SELLER_NOT_FOUND");
        }

        @Test
        @DisplayName("SELLER_INACTIVE 상수 존재")
        void shouldHaveSellerInactive() {
            // Given & When
            SellerErrorCode errorCode = SellerErrorCode.SELLER_INACTIVE;

            // Then
            assertThat(errorCode).isNotNull();
            assertThat(errorCode.name()).isEqualTo("SELLER_INACTIVE");
        }

        @Test
        @DisplayName("DUPLICATE_SELLER_CODE 상수 존재")
        void shouldHaveDuplicateSellerCode() {
            // Given & When
            SellerErrorCode errorCode = SellerErrorCode.DUPLICATE_SELLER_CODE;

            // Then
            assertThat(errorCode).isNotNull();
            assertThat(errorCode.name()).isEqualTo("DUPLICATE_SELLER_CODE");
        }

        @Test
        @DisplayName("Enum 값은 정확히 3개")
        void shouldHaveExactlyThreeValues() {
            // When
            SellerErrorCode[] values = SellerErrorCode.values();

            // Then
            assertThat(values).hasSize(3);
        }

        @ParameterizedTest
        @EnumSource(SellerErrorCode.class)
        @DisplayName("모든 Enum 값이 유효")
        void shouldHaveValidValues(SellerErrorCode errorCode) {
            // Then
            assertThat(errorCode).isNotNull();
            assertThat(errorCode.name()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("getCode() 메서드 테스트")
    class GetCodeTests {

        @Test
        @DisplayName("SELLER_NOT_FOUND는 'SELLER-001' 반환")
        void shouldReturnSellerNotFoundCode() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.SELLER_NOT_FOUND;

            // When
            String code = errorCode.getCode();

            // Then
            assertThat(code).isEqualTo("SELLER-001");
        }

        @Test
        @DisplayName("SELLER_INACTIVE는 'SELLER-010' 반환")
        void shouldReturnSellerInactiveCode() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.SELLER_INACTIVE;

            // When
            String code = errorCode.getCode();

            // Then
            assertThat(code).isEqualTo("SELLER-010");
        }

        @Test
        @DisplayName("DUPLICATE_SELLER_CODE는 'SELLER-011' 반환")
        void shouldReturnDuplicateSellerCodeCode() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.DUPLICATE_SELLER_CODE;

            // When
            String code = errorCode.getCode();

            // Then
            assertThat(code).isEqualTo("SELLER-011");
        }

        @ParameterizedTest
        @EnumSource(SellerErrorCode.class)
        @DisplayName("모든 Error Code는 null이 아니고 비어있지 않음")
        void shouldHaveNonEmptyCode(SellerErrorCode errorCode) {
            // When
            String code = errorCode.getCode();

            // Then
            assertThat(code).isNotNull().isNotBlank();
        }

        @ParameterizedTest
        @EnumSource(SellerErrorCode.class)
        @DisplayName("모든 Error Code는 'SELLER-' 접두사로 시작")
        void shouldStartWithSellerPrefix(SellerErrorCode errorCode) {
            // When
            String code = errorCode.getCode();

            // Then
            assertThat(code).startsWith("SELLER-");
        }
    }

    @Nested
    @DisplayName("getHttpStatus() 메서드 테스트")
    class GetHttpStatusTests {

        @Test
        @DisplayName("SELLER_NOT_FOUND는 404 반환")
        void shouldReturn404ForNotFound() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.SELLER_NOT_FOUND;

            // When
            int httpStatus = errorCode.getHttpStatus();

            // Then
            assertThat(httpStatus).isEqualTo(404);
        }

        @Test
        @DisplayName("SELLER_INACTIVE는 409 반환")
        void shouldReturn409ForInactive() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.SELLER_INACTIVE;

            // When
            int httpStatus = errorCode.getHttpStatus();

            // Then
            assertThat(httpStatus).isEqualTo(409);
        }

        @Test
        @DisplayName("DUPLICATE_SELLER_CODE는 409 반환")
        void shouldReturn409ForDuplicate() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.DUPLICATE_SELLER_CODE;

            // When
            int httpStatus = errorCode.getHttpStatus();

            // Then
            assertThat(httpStatus).isEqualTo(409);
        }

        @ParameterizedTest
        @EnumSource(SellerErrorCode.class)
        @DisplayName("모든 HTTP 상태 코드는 유효한 범위 (400-599)")
        void shouldHaveValidHttpStatus(SellerErrorCode errorCode) {
            // When
            int httpStatus = errorCode.getHttpStatus();

            // Then
            assertThat(httpStatus).isBetween(400, 599);
        }
    }

    @Nested
    @DisplayName("getMessage() 메서드 테스트")
    class GetMessageTests {

        @Test
        @DisplayName("SELLER_NOT_FOUND는 한글 메시지 반환")
        void shouldReturnKoreanMessageForNotFound() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.SELLER_NOT_FOUND;

            // When
            String message = errorCode.getMessage();

            // Then
            assertThat(message).isEqualTo("셀러를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("SELLER_INACTIVE는 한글 메시지 반환")
        void shouldReturnKoreanMessageForInactive() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.SELLER_INACTIVE;

            // When
            String message = errorCode.getMessage();

            // Then
            assertThat(message).isEqualTo("셀러가 비활성 상태입니다");
        }

        @Test
        @DisplayName("DUPLICATE_SELLER_CODE는 한글 메시지 반환")
        void shouldReturnKoreanMessageForDuplicate() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.DUPLICATE_SELLER_CODE;

            // When
            String message = errorCode.getMessage();

            // Then
            assertThat(message).isEqualTo("이미 존재하는 셀러 코드입니다");
        }

        @ParameterizedTest
        @EnumSource(SellerErrorCode.class)
        @DisplayName("모든 메시지는 null이 아니고 비어있지 않음")
        void shouldHaveNonEmptyMessage(SellerErrorCode errorCode) {
            // When
            String message = errorCode.getMessage();

            // Then
            assertThat(message).isNotNull().isNotBlank();
        }

        @ParameterizedTest
        @EnumSource(SellerErrorCode.class)
        @DisplayName("모든 메시지는 한글 포함")
        void shouldContainKoreanCharacters(SellerErrorCode errorCode) {
            // When
            String message = errorCode.getMessage();

            // Then
            // 한글 범위: 0xAC00 ~ 0xD7A3
            assertThat(message.chars().anyMatch(c -> c >= 0xAC00 && c <= 0xD7A3)).isTrue();
        }
    }

    @Nested
    @DisplayName("getTitle() 메서드 테스트")
    class GetTitleTests {

        @Test
        @DisplayName("SELLER_NOT_FOUND는 영문 타이틀 반환")
        void shouldReturnEnglishTitleForNotFound() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.SELLER_NOT_FOUND;

            // When
            String title = errorCode.getTitle();

            // Then
            assertThat(title).isEqualTo("Seller Not Found");
        }

        @Test
        @DisplayName("SELLER_INACTIVE는 영문 타이틀 반환")
        void shouldReturnEnglishTitleForInactive() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.SELLER_INACTIVE;

            // When
            String title = errorCode.getTitle();

            // Then
            assertThat(title).isEqualTo("Seller Inactive");
        }

        @Test
        @DisplayName("DUPLICATE_SELLER_CODE는 영문 타이틀 반환")
        void shouldReturnEnglishTitleForDuplicate() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.DUPLICATE_SELLER_CODE;

            // When
            String title = errorCode.getTitle();

            // Then
            assertThat(title).isEqualTo("Duplicate Seller Code");
        }

        @ParameterizedTest
        @EnumSource(SellerErrorCode.class)
        @DisplayName("모든 타이틀은 null이 아니고 비어있지 않음")
        void shouldHaveNonEmptyTitle(SellerErrorCode errorCode) {
            // When
            String title = errorCode.getTitle();

            // Then
            assertThat(title).isNotNull().isNotBlank();
        }

        @ParameterizedTest
        @EnumSource(SellerErrorCode.class)
        @DisplayName("모든 타이틀은 영문만 포함 (한글 없음)")
        void shouldContainOnlyEnglish(SellerErrorCode errorCode) {
            // When
            String title = errorCode.getTitle();

            // Then
            // 한글이 없어야 함
            assertThat(title.chars().anyMatch(c -> c >= 0xAC00 && c <= 0xD7A3)).isFalse();
        }
    }

    @Nested
    @DisplayName("valueOf() 메서드 테스트")
    class ValueOfTests {

        @Test
        @DisplayName("문자열 'SELLER_NOT_FOUND'로 Enum 생성")
        void shouldCreateFromNotFoundString() {
            // When
            SellerErrorCode errorCode = SellerErrorCode.valueOf("SELLER_NOT_FOUND");

            // Then
            assertThat(errorCode).isEqualTo(SellerErrorCode.SELLER_NOT_FOUND);
        }

        @Test
        @DisplayName("문자열 'SELLER_INACTIVE'로 Enum 생성")
        void shouldCreateFromInactiveString() {
            // When
            SellerErrorCode errorCode = SellerErrorCode.valueOf("SELLER_INACTIVE");

            // Then
            assertThat(errorCode).isEqualTo(SellerErrorCode.SELLER_INACTIVE);
        }

        @Test
        @DisplayName("문자열 'DUPLICATE_SELLER_CODE'로 Enum 생성")
        void shouldCreateFromDuplicateString() {
            // When
            SellerErrorCode errorCode = SellerErrorCode.valueOf("DUPLICATE_SELLER_CODE");

            // Then
            assertThat(errorCode).isEqualTo(SellerErrorCode.DUPLICATE_SELLER_CODE);
        }
    }

    @Nested
    @DisplayName("Enum 비교 테스트")
    class ComparisonTests {

        @Test
        @DisplayName("같은 Enum 값은 동일하다")
        void shouldBeEqualForSameEnum() {
            // Given
            SellerErrorCode errorCode1 = SellerErrorCode.SELLER_NOT_FOUND;
            SellerErrorCode errorCode2 = SellerErrorCode.SELLER_NOT_FOUND;

            // Then
            assertThat(errorCode1).isEqualTo(errorCode2);
            assertThat(errorCode1).isSameAs(errorCode2);  // Enum은 싱글톤
        }

        @Test
        @DisplayName("다른 Enum 값은 동일하지 않다")
        void shouldNotBeEqualForDifferentEnum() {
            // Given
            SellerErrorCode errorCode1 = SellerErrorCode.SELLER_NOT_FOUND;
            SellerErrorCode errorCode2 = SellerErrorCode.SELLER_INACTIVE;
            SellerErrorCode errorCode3 = SellerErrorCode.DUPLICATE_SELLER_CODE;

            // Then
            assertThat(errorCode1).isNotEqualTo(errorCode2);
            assertThat(errorCode1).isNotEqualTo(errorCode3);
            assertThat(errorCode2).isNotEqualTo(errorCode3);
        }

        @Test
        @DisplayName("Enum 순서 확인")
        void shouldHaveCorrectOrdinal() {
            // Given
            SellerErrorCode[] values = SellerErrorCode.values();

            // Then
            assertThat(values[0]).isEqualTo(SellerErrorCode.SELLER_NOT_FOUND);
            assertThat(values[1]).isEqualTo(SellerErrorCode.SELLER_INACTIVE);
            assertThat(values[2]).isEqualTo(SellerErrorCode.DUPLICATE_SELLER_CODE);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("SELLER_NOT_FOUND toString은 'SELLER_NOT_FOUND' 반환")
        void shouldReturnNotFoundAsString() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.SELLER_NOT_FOUND;

            // When
            String result = errorCode.toString();

            // Then
            assertThat(result).isEqualTo("SELLER_NOT_FOUND");
        }

        @Test
        @DisplayName("SELLER_INACTIVE toString은 'SELLER_INACTIVE' 반환")
        void shouldReturnInactiveAsString() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.SELLER_INACTIVE;

            // When
            String result = errorCode.toString();

            // Then
            assertThat(result).isEqualTo("SELLER_INACTIVE");
        }

        @Test
        @DisplayName("DUPLICATE_SELLER_CODE toString은 'DUPLICATE_SELLER_CODE' 반환")
        void shouldReturnDuplicateAsString() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.DUPLICATE_SELLER_CODE;

            // When
            String result = errorCode.toString();

            // Then
            assertThat(result).isEqualTo("DUPLICATE_SELLER_CODE");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("404 Not Found 에러 코드 사용 시나리오")
        void shouldHandleNotFoundScenario() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.SELLER_NOT_FOUND;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("SELLER-001");
            assertThat(errorCode.getHttpStatus()).isEqualTo(404);
            assertThat(errorCode.getMessage()).contains("찾을 수 없습니다");
            assertThat(errorCode.getTitle()).isEqualTo("Seller Not Found");
        }

        @Test
        @DisplayName("409 Conflict 에러 코드 사용 시나리오 (비활성)")
        void shouldHandleInactiveConflictScenario() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.SELLER_INACTIVE;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("SELLER-010");
            assertThat(errorCode.getHttpStatus()).isEqualTo(409);
            assertThat(errorCode.getMessage()).contains("비활성");
            assertThat(errorCode.getTitle()).isEqualTo("Seller Inactive");
        }

        @Test
        @DisplayName("409 Conflict 에러 코드 사용 시나리오 (중복)")
        void shouldHandleDuplicateConflictScenario() {
            // Given
            SellerErrorCode errorCode = SellerErrorCode.DUPLICATE_SELLER_CODE;

            // When & Then
            assertThat(errorCode.getCode()).isEqualTo("SELLER-011");
            assertThat(errorCode.getHttpStatus()).isEqualTo(409);
            assertThat(errorCode.getMessage()).contains("이미 존재하는");
            assertThat(errorCode.getTitle()).isEqualTo("Duplicate Seller Code");
        }

        @Test
        @DisplayName("Switch 문에서 모든 에러 코드 처리 가능")
        void shouldHandleAllErrorCodesInSwitch() {
            // Given
            SellerErrorCode[] allErrorCodes = SellerErrorCode.values();

            // When & Then
            for (SellerErrorCode errorCode : allErrorCodes) {
                String result = switch (errorCode) {
                    case SELLER_NOT_FOUND -> "Not Found";
                    case SELLER_INACTIVE -> "Inactive";
                    case DUPLICATE_SELLER_CODE -> "Duplicate";
                };
                assertThat(result).isNotBlank();
            }
        }

        @Test
        @DisplayName("HTTP 상태 코드별 그룹화 가능")
        void shouldGroupByHttpStatus() {
            // Given
            SellerErrorCode[] allErrorCodes = SellerErrorCode.values();

            // When
            long notFoundCount = java.util.Arrays.stream(allErrorCodes)
                .filter(code -> code.getHttpStatus() == 404)
                .count();
            long conflictCount = java.util.Arrays.stream(allErrorCodes)
                .filter(code -> code.getHttpStatus() == 409)
                .count();

            // Then
            assertThat(notFoundCount).isEqualTo(1);  // SELLER_NOT_FOUND
            assertThat(conflictCount).isEqualTo(2);  // SELLER_INACTIVE, DUPLICATE_SELLER_CODE
        }
    }
}
