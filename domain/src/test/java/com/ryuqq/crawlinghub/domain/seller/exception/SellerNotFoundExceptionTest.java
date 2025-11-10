package com.ryuqq.crawlinghub.domain.seller.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SellerNotFoundException 테스트")
class SellerNotFoundExceptionTest {

    private static final Long SELLER_ID = 123L;

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("sellerId로 예외 생성 성공")
        void shouldCreateWithSellerId() {
            // When
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getSellerId()).isEqualTo(SELLER_ID);
        }

        @Test
        @DisplayName("예외 메시지는 sellerId 포함")
        void shouldIncludeSellerIdInMessage() {
            // When
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

            // Then
            assertThat(exception.getMessage())
                .contains("셀러를 찾을 수 없습니다")
                .contains(SELLER_ID.toString());
        }

        @Test
        @DisplayName("다양한 sellerId 값으로 생성 가능")
        void shouldCreateWithVariousSellerIds() {
            // Given
            Long[] sellerIds = {1L, 999L, 123456L, Long.MAX_VALUE};

            // When & Then
            for (Long sellerId : sellerIds) {
                SellerNotFoundException exception = new SellerNotFoundException(sellerId);
                assertThat(exception.getSellerId()).isEqualTo(sellerId);
                assertThat(exception.getMessage()).contains(sellerId.toString());
            }
        }
    }

    @Nested
    @DisplayName("getSellerId() 메서드 테스트")
    class GetSellerIdTests {

        @Test
        @DisplayName("getSellerId()는 생성 시 전달한 sellerId 반환")
        void shouldReturnSellerId() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

            // When
            Long sellerId = exception.getSellerId();

            // Then
            assertThat(sellerId).isEqualTo(SELLER_ID);
        }

        @Test
        @DisplayName("sellerId는 변경 불가 (불변성)")
        void shouldBeImmutable() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

            // When
            Long sellerId1 = exception.getSellerId();
            Long sellerId2 = exception.getSellerId();

            // Then
            assertThat(sellerId1).isEqualTo(sellerId2);
            assertThat(sellerId1).isSameAs(sellerId2);  // 같은 객체
        }
    }

    @Nested
    @DisplayName("code() 메서드 테스트")
    class CodeTests {

        @Test
        @DisplayName("code()는 'SELLER-001' 반환")
        void shouldReturnSellerNotFoundCode() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

            // When
            String code = exception.code();

            // Then
            assertThat(code).isEqualTo("SELLER-001");
        }

        @Test
        @DisplayName("code()는 SellerErrorCode.SELLER_NOT_FOUND와 동일")
        void shouldMatchErrorCodeEnum() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

            // When
            String code = exception.code();

            // Then
            assertThat(code).isEqualTo(SellerErrorCode.SELLER_NOT_FOUND.getCode());
        }
    }

    @Nested
    @DisplayName("message() 메서드 테스트")
    class MessageTests {

        @Test
        @DisplayName("message()는 getMessage()와 동일")
        void shouldReturnSameAsGetMessage() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

            // When
            String message = exception.message();
            String getMessage = exception.getMessage();

            // Then
            assertThat(message).isEqualTo(getMessage);
        }

        @Test
        @DisplayName("message()는 sellerId 포함")
        void shouldIncludeSellerId() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

            // When
            String message = exception.message();

            // Then
            assertThat(message).contains(SELLER_ID.toString());
        }

        @Test
        @DisplayName("message()는 한글 메시지 포함")
        void shouldContainKoreanMessage() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

            // When
            String message = exception.message();

            // Then
            assertThat(message).contains("셀러를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("args() 메서드 테스트")
    class ArgsTests {

        @Test
        @DisplayName("args()는 sellerId 포함하는 Map 반환")
        void shouldReturnMapWithSellerId() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).isNotNull();
            assertThat(args).containsEntry("sellerId", SELLER_ID);
        }

        @Test
        @DisplayName("args() Map은 정확히 1개 엔트리")
        void shouldHaveExactlyOneEntry() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).hasSize(1);
        }

        @Test
        @DisplayName("args() Map은 불변 (변경 불가)")
        void shouldBeImmutableMap() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);
            Map<String, Object> args = exception.args();

            // When & Then
            assertThatThrownBy(() -> args.put("newKey", "newValue"))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("args()는 매번 새로운 Map 반환")
        void shouldReturnNewMapEachTime() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

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
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

            // Then
            assertThat(exception).isInstanceOf(SellerException.class);
        }

        @Test
        @DisplayName("DomainException을 간접 상속")
        void shouldIndirectlyExtendDomainException() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

            // Then
            assertThat(exception).isInstanceOf(com.ryuqq.crawlinghub.domain.common.DomainException.class);
        }

        @Test
        @DisplayName("RuntimeException을 간접 상속")
        void shouldIndirectlyExtendRuntimeException() {
            // Given
            SellerNotFoundException exception = new SellerNotFoundException(SELLER_ID);

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("final 클래스 (더 이상 상속 불가)")
        void shouldBeFinalClass() {
            // Given
            Class<SellerNotFoundException> clazz = SellerNotFoundException.class;

            // Then
            assertThat(java.lang.reflect.Modifier.isFinal(clazz.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("Repository에서 셀러를 찾지 못한 경우")
        void shouldHandleRepositoryNotFoundScenario() {
            // Given
            Long nonExistentSellerId = 999L;

            // When
            SellerNotFoundException exception = new SellerNotFoundException(nonExistentSellerId);

            // Then
            assertThat(exception.getSellerId()).isEqualTo(nonExistentSellerId);
            assertThat(exception.code()).isEqualTo("SELLER-001");
            assertThat(exception.message()).contains("셀러를 찾을 수 없습니다");
            assertThat(exception.args()).containsEntry("sellerId", nonExistentSellerId);
        }

        @Test
        @DisplayName("예외를 던지고 catch하는 시나리오")
        void shouldHandleThrowAndCatchScenario() {
            // Given
            Long sellerId = 123L;

            // When & Then
            assertThatThrownBy(() -> {
                throw new SellerNotFoundException(sellerId);
            })
                .isInstanceOf(SellerNotFoundException.class)
                .hasMessageContaining("셀러를 찾을 수 없습니다")
                .hasMessageContaining(sellerId.toString())
                .satisfies(ex -> {
                    SellerNotFoundException sellerEx = (SellerNotFoundException) ex;
                    assertThat(sellerEx.getSellerId()).isEqualTo(sellerId);
                    assertThat(sellerEx.code()).isEqualTo("SELLER-001");
                });
        }

        @Test
        @DisplayName("GlobalExceptionHandler에서 args() 사용 시나리오")
        void shouldHandleGlobalExceptionHandlerScenario() {
            // Given
            Long sellerId = 456L;
            SellerNotFoundException exception = new SellerNotFoundException(sellerId);

            // When: GlobalExceptionHandler가 args()를 호출하여 ErrorResponse 생성
            Map<String, Object> args = exception.args();
            String code = exception.code();
            String message = exception.message();

            // Then: ErrorResponse에 필요한 모든 정보 확인
            assertThat(code).isEqualTo("SELLER-001");
            assertThat(message).isNotBlank();
            assertThat(args).containsEntry("sellerId", sellerId);

            // ErrorResponse 시뮬레이션
            assertThat(args.get("sellerId")).isEqualTo(sellerId);
        }

        @Test
        @DisplayName("로깅 시 sellerId 추출 시나리오")
        void shouldHandleLoggingScenario() {
            // Given
            Long sellerId = 789L;
            SellerNotFoundException exception = new SellerNotFoundException(sellerId);

            // When: Logger가 sellerId를 추출하여 로그 메시지 생성
            Long extractedSellerId = exception.getSellerId();
            String logMessage = String.format(
                "SellerNotFoundException occurred for sellerId: %d, code: %s",
                extractedSellerId,
                exception.code()
            );

            // Then
            assertThat(logMessage)
                .contains(sellerId.toString())
                .contains("SELLER-001");
        }
    }
}
