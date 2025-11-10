package com.ryuqq.crawlinghub.domain.seller.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("InactiveSellerException 테스트")
class InactiveSellerExceptionTest {

    private static final Long SELLER_ID = 456L;
    private static final String SELLER_NAME = "테스트 셀러";

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("sellerId와 sellerName으로 예외 생성 성공")
        void shouldCreateWithSellerIdAndName() {
            // When
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getSellerId()).isEqualTo(SELLER_ID);
            assertThat(exception.getSellerName()).isEqualTo(SELLER_NAME);
        }

        @Test
        @DisplayName("예외 메시지는 sellerId와 sellerName 모두 포함")
        void shouldIncludeSellerIdAndNameInMessage() {
            // When
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // Then
            assertThat(exception.getMessage())
                .contains("셀러가 비활성 상태입니다")
                .contains(SELLER_ID.toString())
                .contains(SELLER_NAME);
        }

        @Test
        @DisplayName("다양한 sellerId와 sellerName 조합으로 생성 가능")
        void shouldCreateWithVariousCombinations() {
            // Given
            Object[][] combinations = {
                {1L, "셀러A"},
                {999L, "Seller B"},
                {12345L, "가나다라마바사"},
                {Long.MAX_VALUE, "ABC-XYZ-123"}
            };

            // When & Then
            for (Object[] combo : combinations) {
                Long sellerId = (Long) combo[0];
                String sellerName = (String) combo[1];

                InactiveSellerException exception = new InactiveSellerException(sellerId, sellerName);
                assertThat(exception.getSellerId()).isEqualTo(sellerId);
                assertThat(exception.getSellerName()).isEqualTo(sellerName);
                assertThat(exception.getMessage())
                    .contains(sellerId.toString())
                    .contains(sellerName);
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
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // When
            Long sellerId = exception.getSellerId();

            // Then
            assertThat(sellerId).isEqualTo(SELLER_ID);
        }

        @Test
        @DisplayName("sellerId는 변경 불가 (불변성)")
        void shouldBeImmutableSellerId() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // When
            Long sellerId1 = exception.getSellerId();
            Long sellerId2 = exception.getSellerId();

            // Then
            assertThat(sellerId1).isEqualTo(sellerId2);
            assertThat(sellerId1).isSameAs(sellerId2);  // 같은 객체
        }
    }

    @Nested
    @DisplayName("getSellerName() 메서드 테스트")
    class GetSellerNameTests {

        @Test
        @DisplayName("getSellerName()은 생성 시 전달한 sellerName 반환")
        void shouldReturnSellerName() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // When
            String sellerName = exception.getSellerName();

            // Then
            assertThat(sellerName).isEqualTo(SELLER_NAME);
        }

        @Test
        @DisplayName("sellerName은 변경 불가 (불변성)")
        void shouldBeImmutableSellerName() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // When
            String sellerName1 = exception.getSellerName();
            String sellerName2 = exception.getSellerName();

            // Then
            assertThat(sellerName1).isEqualTo(sellerName2);
            assertThat(sellerName1).isSameAs(sellerName2);  // 같은 객체
        }
    }

    @Nested
    @DisplayName("code() 메서드 테스트")
    class CodeTests {

        @Test
        @DisplayName("code()는 'SELLER-010' 반환")
        void shouldReturnSellerInactiveCode() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // When
            String code = exception.code();

            // Then
            assertThat(code).isEqualTo("SELLER-010");
        }

        @Test
        @DisplayName("code()는 SellerErrorCode.SELLER_INACTIVE와 동일")
        void shouldMatchErrorCodeEnum() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // When
            String code = exception.code();

            // Then
            assertThat(code).isEqualTo(SellerErrorCode.SELLER_INACTIVE.getCode());
        }
    }

    @Nested
    @DisplayName("message() 메서드 테스트")
    class MessageTests {

        @Test
        @DisplayName("message()는 getMessage()와 동일")
        void shouldReturnSameAsGetMessage() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // When
            String message = exception.message();
            String getMessage = exception.getMessage();

            // Then
            assertThat(message).isEqualTo(getMessage);
        }

        @Test
        @DisplayName("message()는 sellerId와 sellerName 모두 포함")
        void shouldIncludeSellerIdAndName() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // When
            String message = exception.message();

            // Then
            assertThat(message)
                .contains(SELLER_ID.toString())
                .contains(SELLER_NAME);
        }

        @Test
        @DisplayName("message()는 한글 메시지 포함")
        void shouldContainKoreanMessage() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // When
            String message = exception.message();

            // Then
            assertThat(message).contains("셀러가 비활성 상태입니다");
        }
    }

    @Nested
    @DisplayName("args() 메서드 테스트")
    class ArgsTests {

        @Test
        @DisplayName("args()는 sellerId와 sellerName 포함하는 Map 반환")
        void shouldReturnMapWithSellerIdAndName() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).isNotNull();
            assertThat(args).containsEntry("sellerId", SELLER_ID);
            assertThat(args).containsEntry("sellerName", SELLER_NAME);
        }

        @Test
        @DisplayName("args() Map은 정확히 2개 엔트리")
        void shouldHaveExactlyTwoEntries() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).hasSize(2);
        }

        @Test
        @DisplayName("args() Map은 불변 (변경 불가)")
        void shouldBeImmutableMap() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);
            Map<String, Object> args = exception.args();

            // When & Then
            assertThatThrownBy(() -> args.put("newKey", "newValue"))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("args()는 매번 새로운 Map 반환")
        void shouldReturnNewMapEachTime() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // When
            Map<String, Object> args1 = exception.args();
            Map<String, Object> args2 = exception.args();

            // Then
            assertThat(args1).isEqualTo(args2);  // 내용 동일
            assertThat(args1).isNotSameAs(args2);  // 다른 인스턴스
        }

        @Test
        @DisplayName("args() Map의 키는 sellerId와 sellerName만 존재")
        void shouldHaveOnlySellerIdAndNameKeys() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args.keySet()).containsExactlyInAnyOrder("sellerId", "sellerName");
        }
    }

    @Nested
    @DisplayName("Exception 상속 구조 테스트")
    class InheritanceTests {

        @Test
        @DisplayName("SellerException을 상속")
        void shouldExtendSellerException() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // Then
            assertThat(exception).isInstanceOf(SellerException.class);
        }

        @Test
        @DisplayName("DomainException을 간접 상속")
        void shouldIndirectlyExtendDomainException() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // Then
            assertThat(exception).isInstanceOf(com.ryuqq.crawlinghub.domain.common.DomainException.class);
        }

        @Test
        @DisplayName("RuntimeException을 간접 상속")
        void shouldIndirectlyExtendRuntimeException() {
            // Given
            InactiveSellerException exception = new InactiveSellerException(SELLER_ID, SELLER_NAME);

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("final 클래스 (더 이상 상속 불가)")
        void shouldBeFinalClass() {
            // Given
            Class<InactiveSellerException> clazz = InactiveSellerException.class;

            // Then
            assertThat(java.lang.reflect.Modifier.isFinal(clazz.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("비활성 셀러로 작업 시도 시나리오")
        void shouldHandleInactiveSellerScenario() {
            // Given
            Long inactiveSellerId = 789L;
            String inactiveSellerName = "비활성 셀러";

            // When
            InactiveSellerException exception = new InactiveSellerException(inactiveSellerId, inactiveSellerName);

            // Then
            assertThat(exception.getSellerId()).isEqualTo(inactiveSellerId);
            assertThat(exception.getSellerName()).isEqualTo(inactiveSellerName);
            assertThat(exception.code()).isEqualTo("SELLER-010");
            assertThat(exception.message()).contains("셀러가 비활성 상태입니다");
            assertThat(exception.args())
                .containsEntry("sellerId", inactiveSellerId)
                .containsEntry("sellerName", inactiveSellerName);
        }

        @Test
        @DisplayName("예외를 던지고 catch하는 시나리오")
        void shouldHandleThrowAndCatchScenario() {
            // Given
            Long sellerId = 123L;
            String sellerName = "테스트 비활성 셀러";

            // When & Then
            assertThatThrownBy(() -> {
                throw new InactiveSellerException(sellerId, sellerName);
            })
                .isInstanceOf(InactiveSellerException.class)
                .hasMessageContaining("셀러가 비활성 상태입니다")
                .hasMessageContaining(sellerId.toString())
                .hasMessageContaining(sellerName)
                .satisfies(ex -> {
                    InactiveSellerException sellerEx = (InactiveSellerException) ex;
                    assertThat(sellerEx.getSellerId()).isEqualTo(sellerId);
                    assertThat(sellerEx.getSellerName()).isEqualTo(sellerName);
                    assertThat(sellerEx.code()).isEqualTo("SELLER-010");
                });
        }

        @Test
        @DisplayName("GlobalExceptionHandler에서 409 Conflict 응답 시나리오")
        void shouldHandleConflictResponseScenario() {
            // Given
            Long sellerId = 456L;
            String sellerName = "Inactive Seller Corp";
            InactiveSellerException exception = new InactiveSellerException(sellerId, sellerName);

            // When: GlobalExceptionHandler가 409 Conflict 응답 생성
            String code = exception.code();
            String message = exception.message();
            Map<String, Object> args = exception.args();
            int httpStatus = SellerErrorCode.SELLER_INACTIVE.getHttpStatus();

            // Then: 409 Conflict ErrorResponse 검증
            assertThat(httpStatus).isEqualTo(409);
            assertThat(code).isEqualTo("SELLER-010");
            assertThat(message).isNotBlank();
            assertThat(args)
                .containsEntry("sellerId", sellerId)
                .containsEntry("sellerName", sellerName);
        }

        @Test
        @DisplayName("Domain Service에서 활성 상태 검증 시나리오")
        void shouldHandleDomainValidationScenario() {
            // Given: Domain Service가 Seller 활성 상태 검증
            Long sellerId = 999L;
            String sellerName = "Suspended Seller";
            boolean isActive = false;  // DB에서 조회한 활성 상태

            // When: 비활성 셀러인 경우 예외 발생
            if (!isActive) {
                InactiveSellerException exception = new InactiveSellerException(sellerId, sellerName);

                // Then
                assertThat(exception.getSellerId()).isEqualTo(sellerId);
                assertThat(exception.getSellerName()).isEqualTo(sellerName);
                assertThat(exception.code()).isEqualTo("SELLER-010");
            }
        }

        @Test
        @DisplayName("로깅 시 sellerId와 sellerName 추출 시나리오")
        void shouldHandleLoggingScenario() {
            // Given
            Long sellerId = 111L;
            String sellerName = "Log Test Seller";
            InactiveSellerException exception = new InactiveSellerException(sellerId, sellerName);

            // When: Logger가 sellerId와 sellerName을 추출하여 로그 메시지 생성
            Long extractedId = exception.getSellerId();
            String extractedName = exception.getSellerName();
            String logMessage = String.format(
                "InactiveSellerException occurred for sellerId: %d, sellerName: %s, code: %s",
                extractedId,
                extractedName,
                exception.code()
            );

            // Then
            assertThat(logMessage)
                .contains(sellerId.toString())
                .contains(sellerName)
                .contains("SELLER-010");
        }

        @Test
        @DisplayName("ErrorResponse args()에서 모든 컨텍스트 정보 전달 시나리오")
        void shouldProvideAllContextInErrorResponse() {
            // Given
            Long sellerId = 777L;
            String sellerName = "Context Test Seller";
            InactiveSellerException exception = new InactiveSellerException(sellerId, sellerName);

            // When: ErrorResponse가 args()로 모든 컨텍스트 정보 수집
            Map<String, Object> contextArgs = exception.args();

            // Then: ErrorResponse에 sellerId와 sellerName이 모두 포함됨
            assertThat(contextArgs).hasSize(2);
            assertThat(contextArgs.get("sellerId")).isEqualTo(sellerId);
            assertThat(contextArgs.get("sellerName")).isEqualTo(sellerName);

            // API 응답 JSON 시뮬레이션
            // {
            //   "code": "SELLER-010",
            //   "message": "셀러가 비활성 상태입니다: ID=777, Name=Context Test Seller",
            //   "args": {
            //     "sellerId": 777,
            //     "sellerName": "Context Test Seller"
            //   }
            // }
        }
    }
}
