package com.ryuqq.crawlinghub.domain.seller.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DuplicateSellerCodeException 테스트")
class DuplicateSellerCodeExceptionTest {

    private static final String SELLER_CODE = "SELLER-ABC-123";

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("sellerCode로 예외 생성 성공")
        void shouldCreateWithSellerCode() {
            // When
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getSellerCode()).isEqualTo(SELLER_CODE);
        }

        @Test
        @DisplayName("예외 메시지는 sellerCode 포함")
        void shouldIncludeSellerCodeInMessage() {
            // When
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // Then
            assertThat(exception.getMessage())
                .contains("이미 존재하는 셀러 코드입니다")
                .contains(SELLER_CODE);
        }

        @Test
        @DisplayName("다양한 sellerCode 값으로 생성 가능")
        void shouldCreateWithVariousSellerCodes() {
            // Given
            String[] sellerCodes = {
                "SELLER-001",
                "SELLER-ABC-999",
                "TEST-SELLER",
                "가나다라마바사"
            };

            // When & Then
            for (String sellerCode : sellerCodes) {
                DuplicateSellerCodeException exception = new DuplicateSellerCodeException(sellerCode);
                assertThat(exception.getSellerCode()).isEqualTo(sellerCode);
                assertThat(exception.getMessage()).contains(sellerCode);
            }
        }
    }

    @Nested
    @DisplayName("getSellerCode() 메서드 테스트")
    class GetSellerCodeTests {

        @Test
        @DisplayName("getSellerCode()는 생성 시 전달한 sellerCode 반환")
        void shouldReturnSellerCode() {
            // Given
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // When
            String sellerCode = exception.getSellerCode();

            // Then
            assertThat(sellerCode).isEqualTo(SELLER_CODE);
        }

        @Test
        @DisplayName("sellerCode는 변경 불가 (불변성)")
        void shouldBeImmutable() {
            // Given
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // When
            String sellerCode1 = exception.getSellerCode();
            String sellerCode2 = exception.getSellerCode();

            // Then
            assertThat(sellerCode1).isEqualTo(sellerCode2);
            assertThat(sellerCode1).isSameAs(sellerCode2);  // 같은 객체
        }
    }

    @Nested
    @DisplayName("code() 메서드 테스트")
    class CodeTests {

        @Test
        @DisplayName("code()는 'SELLER-011' 반환")
        void shouldReturnDuplicateSellerCodeCode() {
            // Given
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // When
            String code = exception.code();

            // Then
            assertThat(code).isEqualTo("SELLER-011");
        }

        @Test
        @DisplayName("code()는 SellerErrorCode.DUPLICATE_SELLER_CODE와 동일")
        void shouldMatchErrorCodeEnum() {
            // Given
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // When
            String code = exception.code();

            // Then
            assertThat(code).isEqualTo(SellerErrorCode.DUPLICATE_SELLER_CODE.getCode());
        }
    }

    @Nested
    @DisplayName("message() 메서드 테스트")
    class MessageTests {

        @Test
        @DisplayName("message()는 getMessage()와 동일")
        void shouldReturnSameAsGetMessage() {
            // Given
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // When
            String message = exception.message();
            String getMessage = exception.getMessage();

            // Then
            assertThat(message).isEqualTo(getMessage);
        }

        @Test
        @DisplayName("message()는 sellerCode 포함")
        void shouldIncludeSellerCode() {
            // Given
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // When
            String message = exception.message();

            // Then
            assertThat(message).contains(SELLER_CODE);
        }

        @Test
        @DisplayName("message()는 한글 메시지 포함")
        void shouldContainKoreanMessage() {
            // Given
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // When
            String message = exception.message();

            // Then
            assertThat(message).contains("이미 존재하는 셀러 코드입니다");
        }
    }

    @Nested
    @DisplayName("args() 메서드 테스트")
    class ArgsTests {

        @Test
        @DisplayName("args()는 sellerCode 포함하는 Map 반환")
        void shouldReturnMapWithSellerCode() {
            // Given
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).isNotNull();
            assertThat(args).containsEntry("sellerCode", SELLER_CODE);
        }

        @Test
        @DisplayName("args() Map은 정확히 1개 엔트리")
        void shouldHaveExactlyOneEntry() {
            // Given
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).hasSize(1);
        }

        @Test
        @DisplayName("args() Map은 불변 (변경 불가)")
        void shouldBeImmutableMap() {
            // Given
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);
            Map<String, Object> args = exception.args();

            // When & Then
            assertThatThrownBy(() -> args.put("newKey", "newValue"))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("args()는 매번 새로운 Map 반환")
        void shouldReturnNewMapEachTime() {
            // Given
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // When
            Map<String, Object> args1 = exception.args();
            Map<String, Object> args2 = exception.args();

            // Then
            assertThat(args1).isEqualTo(args2);  // 내용 동일
            assertThat(args1).isNotSameAs(args2);  // 다른 인스턴스
        }
    }

    @Nested
    @DisplayName("Exception 상속 구조 테스트")
    class InheritanceTests {

        @Test
        @DisplayName("SellerException을 상속")
        void shouldExtendSellerException() {
            // Given
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // Then
            assertThat(exception).isInstanceOf(SellerException.class);
        }

        @Test
        @DisplayName("DomainException을 간접 상속")
        void shouldIndirectlyExtendDomainException() {
            // Given
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // Then
            assertThat(exception).isInstanceOf(com.ryuqq.crawlinghub.domain.common.DomainException.class);
        }

        @Test
        @DisplayName("RuntimeException을 간접 상속")
        void shouldIndirectlyExtendRuntimeException() {
            // Given
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(SELLER_CODE);

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("final 클래스 (더 이상 상속 불가)")
        void shouldBeFinalClass() {
            // Given
            Class<DuplicateSellerCodeException> clazz = DuplicateSellerCodeException.class;

            // Then
            assertThat(java.lang.reflect.Modifier.isFinal(clazz.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("중복된 셀러 코드 등록 시도 시나리오")
        void shouldHandleDuplicateRegistrationScenario() {
            // Given
            String existingSellerCode = "SELLER-XYZ-789";

            // When
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(existingSellerCode);

            // Then
            assertThat(exception.getSellerCode()).isEqualTo(existingSellerCode);
            assertThat(exception.code()).isEqualTo("SELLER-011");
            assertThat(exception.message()).contains("이미 존재하는 셀러 코드입니다");
            assertThat(exception.args()).containsEntry("sellerCode", existingSellerCode);
        }

        @Test
        @DisplayName("예외를 던지고 catch하는 시나리오")
        void shouldHandleThrowAndCatchScenario() {
            // Given
            String sellerCode = "DUPLICATE-CODE-123";

            // When & Then
            assertThatThrownBy(() -> {
                throw new DuplicateSellerCodeException(sellerCode);
            })
                .isInstanceOf(DuplicateSellerCodeException.class)
                .hasMessageContaining("이미 존재하는 셀러 코드입니다")
                .hasMessageContaining(sellerCode)
                .satisfies(ex -> {
                    DuplicateSellerCodeException sellerEx = (DuplicateSellerCodeException) ex;
                    assertThat(sellerEx.getSellerCode()).isEqualTo(sellerCode);
                    assertThat(sellerEx.code()).isEqualTo("SELLER-011");
                });
        }

        @Test
        @DisplayName("GlobalExceptionHandler에서 409 Conflict 응답 시나리오")
        void shouldHandleConflictResponseScenario() {
            // Given
            String duplicateCode = "CONFLICT-SELLER-456";
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(duplicateCode);

            // When: GlobalExceptionHandler가 409 Conflict 응답 생성
            String code = exception.code();
            String message = exception.message();
            Map<String, Object> args = exception.args();
            int httpStatus = SellerErrorCode.DUPLICATE_SELLER_CODE.getHttpStatus();

            // Then: 409 Conflict ErrorResponse 검증
            assertThat(httpStatus).isEqualTo(409);
            assertThat(code).isEqualTo("SELLER-011");
            assertThat(message).isNotBlank();
            assertThat(args).containsEntry("sellerCode", duplicateCode);
        }

        @Test
        @DisplayName("Unique Constraint 위반 처리 시나리오")
        void shouldHandleUniqueConstraintViolationScenario() {
            // Given: DB Unique Constraint 위반으로 인한 예외
            String violatedSellerCode = "UK-VIOLATION-CODE";

            // When: DataIntegrityViolationException → DuplicateSellerCodeException 변환
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(violatedSellerCode);

            // Then
            assertThat(exception.getSellerCode()).isEqualTo(violatedSellerCode);
            assertThat(exception.code()).isEqualTo("SELLER-011");
            assertThat(exception.message()).contains("이미 존재하는");
        }

        @Test
        @DisplayName("로깅 시 sellerCode 추출 시나리오")
        void shouldHandleLoggingScenario() {
            // Given
            String sellerCode = "LOG-SELLER-999";
            DuplicateSellerCodeException exception = new DuplicateSellerCodeException(sellerCode);

            // When: Logger가 sellerCode를 추출하여 로그 메시지 생성
            String extractedCode = exception.getSellerCode();
            String logMessage = String.format(
                "DuplicateSellerCodeException occurred for sellerCode: %s, code: %s",
                extractedCode,
                exception.code()
            );

            // Then
            assertThat(logMessage)
                .contains(sellerCode)
                .contains("SELLER-011");
        }
    }
}
