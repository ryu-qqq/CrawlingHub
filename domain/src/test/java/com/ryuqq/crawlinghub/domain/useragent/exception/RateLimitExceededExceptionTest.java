package com.ryuqq.crawlinghub.domain.useragent.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RateLimitExceededException 테스트")
class RateLimitExceededExceptionTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("유효한 파라미터로 예외 생성")
        void shouldCreateExceptionWithValidParameters() {
            // Given
            Long userAgentId = 12345L;
            Integer remainingRequests = 5;

            // When
            RateLimitExceededException exception = new RateLimitExceededException(userAgentId, remainingRequests);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains("User-Agent의 Rate Limit을 초과했습니다");
            assertThat(exception.getMessage()).contains(userAgentId.toString());
            assertThat(exception.getMessage()).contains(remainingRequests.toString());
        }

        @Test
        @DisplayName("0 remainingRequests로 예외 생성")
        void shouldCreateExceptionWithZeroRemainingRequests() {
            // Given
            Long userAgentId = 12345L;
            Integer remainingRequests = 0;

            // When
            RateLimitExceededException exception = new RateLimitExceededException(userAgentId, remainingRequests);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains("0");
        }

        @Test
        @DisplayName("음수 remainingRequests로 예외 생성")
        void shouldCreateExceptionWithNegativeRemainingRequests() {
            // Given
            Long userAgentId = 12345L;
            Integer remainingRequests = -1;

            // When
            RateLimitExceededException exception = new RateLimitExceededException(userAgentId, remainingRequests);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains("-1");
        }

        @Test
        @DisplayName("메시지 포맷이 올바르게 적용됨")
        void shouldFormatMessageCorrectly() {
            // Given
            Long userAgentId = 999L;
            Integer remainingRequests = 10;

            // When
            RateLimitExceededException exception = new RateLimitExceededException(userAgentId, remainingRequests);

            // Then
            assertThat(exception.getMessage())
                .isEqualTo("User-Agent의 Rate Limit을 초과했습니다: 999 (남은 요청: 10)");
        }

        @Test
        @DisplayName("큰 값으로 예외 생성")
        void shouldCreateExceptionWithLargeValues() {
            // Given
            Long userAgentId = Long.MAX_VALUE;
            Integer remainingRequests = Integer.MAX_VALUE;

            // When
            RateLimitExceededException exception = new RateLimitExceededException(userAgentId, remainingRequests);

            // Then
            assertThat(exception).isNotNull();
        }
    }

    @Nested
    @DisplayName("Getter 메서드 테스트")
    class GetterTests {

        @Test
        @DisplayName("getUserAgentId()는 저장된 값을 반환")
        void shouldReturnStoredUserAgentId() {
            // Given
            Long userAgentId = 12345L;
            RateLimitExceededException exception = new RateLimitExceededException(userAgentId, 5);

            // When
            Long result = exception.getUserAgentId();

            // Then
            assertThat(result).isEqualTo(userAgentId);
        }

        @Test
        @DisplayName("getRemainingRequests()는 저장된 값을 반환")
        void shouldReturnStoredRemainingRequests() {
            // Given
            Integer remainingRequests = 10;
            RateLimitExceededException exception = new RateLimitExceededException(12345L, remainingRequests);

            // When
            Integer result = exception.getRemainingRequests();

            // Then
            assertThat(result).isEqualTo(remainingRequests);
        }

        @Test
        @DisplayName("getUserAgentId()는 0을 반환할 수 있음")
        void shouldReturnZeroUserAgentId() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(0L, 5);

            // When
            Long result = exception.getUserAgentId();

            // Then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("getRemainingRequests()는 0을 반환할 수 있음")
        void shouldReturnZeroRemainingRequests() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, 0);

            // When
            Integer result = exception.getRemainingRequests();

            // Then
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("getRemainingRequests()는 음수를 반환할 수 있음")
        void shouldReturnNegativeRemainingRequests() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, -1);

            // When
            Integer result = exception.getRemainingRequests();

            // Then
            assertThat(result).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("args() 메서드 테스트")
    class ArgsTests {

        @Test
        @DisplayName("args()는 userAgentId와 remainingRequests를 포함하는 Map 반환")
        void shouldReturnMapWithBothFields() {
            // Given
            Long userAgentId = 12345L;
            Integer remainingRequests = 10;
            RateLimitExceededException exception = new RateLimitExceededException(userAgentId, remainingRequests);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).isNotNull();
            assertThat(args).containsKey("userAgentId");
            assertThat(args).containsKey("remainingRequests");
            assertThat(args.get("userAgentId")).isEqualTo(userAgentId);
            assertThat(args.get("remainingRequests")).isEqualTo(remainingRequests);
        }

        @Test
        @DisplayName("args()는 불변 Map 반환 (수정 불가)")
        void shouldReturnImmutableMap() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, 10);
            Map<String, Object> args = exception.args();

            // When & Then
            assertThatThrownBy(() -> args.put("newKey", "newValue"))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("args()는 정확히 2개의 키를 포함")
        void shouldReturnMapWithExactlyTwoKeys() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, 10);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).hasSize(2);
        }

        @Test
        @DisplayName("args()의 userAgentId는 Long 타입")
        void shouldReturnLongTypeForUserAgentId() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, 10);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args.get("userAgentId")).isInstanceOf(Long.class);
        }

        @Test
        @DisplayName("args()의 remainingRequests는 Integer 타입")
        void shouldReturnIntegerTypeForRemainingRequests() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, 10);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args.get("remainingRequests")).isInstanceOf(Integer.class);
        }

        @Test
        @DisplayName("args()는 0 값들도 포함 가능")
        void shouldReturnZeroValuesInArgs() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(0L, 0);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args.get("userAgentId")).isEqualTo(0L);
            assertThat(args.get("remainingRequests")).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("메시지 테스트")
    class MessageTests {

        @Test
        @DisplayName("getMessage()는 null이 아님")
        void shouldHaveNonNullMessage() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, 10);

            // When
            String message = exception.getMessage();

            // Then
            assertThat(message).isNotNull();
        }

        @Test
        @DisplayName("getMessage()는 비어있지 않음")
        void shouldHaveNonEmptyMessage() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, 10);

            // When
            String message = exception.getMessage();

            // Then
            assertThat(message).isNotEmpty();
        }

        @Test
        @DisplayName("getMessage()는 한국어 메시지 포함")
        void shouldContainKoreanMessage() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, 10);

            // When
            String message = exception.getMessage();

            // Then
            assertThat(message).contains("User-Agent");
            assertThat(message).contains("Rate Limit");
            assertThat(message).contains("초과");
            assertThat(message).contains("남은 요청");
        }

        @Test
        @DisplayName("getMessage()는 userAgentId를 포함")
        void shouldIncludeUserAgentIdInMessage() {
            // Given
            Long userAgentId = 99999L;
            RateLimitExceededException exception = new RateLimitExceededException(userAgentId, 10);

            // When
            String message = exception.getMessage();

            // Then
            assertThat(message).contains(userAgentId.toString());
        }

        @Test
        @DisplayName("getMessage()는 remainingRequests를 포함")
        void shouldIncludeRemainingRequestsInMessage() {
            // Given
            Integer remainingRequests = 25;
            RateLimitExceededException exception = new RateLimitExceededException(12345L, remainingRequests);

            // When
            String message = exception.getMessage();

            // Then
            assertThat(message).contains(remainingRequests.toString());
        }
    }

    @Nested
    @DisplayName("상속 및 타입 테스트")
    class InheritanceTests {

        @Test
        @DisplayName("RateLimitExceededException은 UserAgentException을 상속")
        void shouldExtendUserAgentException() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, 10);

            // Then
            assertThat(exception).isInstanceOf(UserAgentException.class);
        }

        @Test
        @DisplayName("RateLimitExceededException은 final 클래스")
        void shouldBeFinalClass() {
            // Given
            Class<RateLimitExceededException> clazz = RateLimitExceededException.class;

            // Then
            assertThat(java.lang.reflect.Modifier.isFinal(clazz.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("UserAgentException은 sealed 클래스")
        void shouldHaveSealedParent() {
            // Given
            Class<UserAgentException> parentClass = UserAgentException.class;

            // Then
            assertThat(parentClass.isSealed()).isTrue();
        }

        @Test
        @DisplayName("RateLimitExceededException은 UserAgentException의 permitted subclass")
        void shouldBePermittedSubclass() {
            // Given
            Class<?>[] permittedSubclasses = UserAgentException.class.getPermittedSubclasses();

            // Then
            assertThat(permittedSubclasses).contains(RateLimitExceededException.class);
        }

        @Test
        @DisplayName("RateLimitExceededException은 Throwable의 하위 타입")
        void shouldBeThrowable() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, 10);

            // Then
            assertThat(exception).isInstanceOf(Throwable.class);
        }
    }

    @Nested
    @DisplayName("직렬화 테스트")
    class SerializationTests {

        @Test
        @DisplayName("예외를 직렬화하고 역직렬화할 수 있음")
        void shouldBeSerializable() throws IOException, ClassNotFoundException {
            // Given
            RateLimitExceededException original = new RateLimitExceededException(12345L, 10);

            // When
            byte[] serialized;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(original);
                serialized = bos.toByteArray();
            }

            RateLimitExceededException deserialized;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                deserialized = (RateLimitExceededException) ois.readObject();
            }

            // Then
            assertThat(deserialized).isNotNull();
            assertThat(deserialized.getMessage()).isEqualTo(original.getMessage());
            assertThat(deserialized.getUserAgentId()).isEqualTo(original.getUserAgentId());
            assertThat(deserialized.getRemainingRequests()).isEqualTo(original.getRemainingRequests());
        }

        @Test
        @DisplayName("0 값들을 가진 예외도 직렬화 가능")
        void shouldSerializeWithZeroValues() throws IOException, ClassNotFoundException {
            // Given
            RateLimitExceededException original = new RateLimitExceededException(0L, 0);

            // When
            byte[] serialized;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(original);
                serialized = bos.toByteArray();
            }

            RateLimitExceededException deserialized;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                deserialized = (RateLimitExceededException) ois.readObject();
            }

            // Then
            assertThat(deserialized).isNotNull();
            assertThat(deserialized.getUserAgentId()).isEqualTo(0L);
            assertThat(deserialized.getRemainingRequests()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("예외 던지기 및 잡기 테스트")
    class ThrowAndCatchTests {

        @Test
        @DisplayName("예외를 던지고 잡을 수 있음")
        void shouldThrowAndCatchException() {
            // Given
            Long userAgentId = 12345L;
            Integer remainingRequests = 10;

            // When & Then
            assertThatThrownBy(() -> {
                throw new RateLimitExceededException(userAgentId, remainingRequests);
            })
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("User-Agent의 Rate Limit을 초과했습니다")
                .hasMessageContaining(userAgentId.toString())
                .hasMessageContaining(remainingRequests.toString());
        }

        @Test
        @DisplayName("UserAgentException으로 잡을 수 있음")
        void shouldBeCaughtAsUserAgentException() {
            // Given
            Long userAgentId = 12345L;
            Integer remainingRequests = 10;

            // When & Then
            assertThatThrownBy(() -> {
                throw new RateLimitExceededException(userAgentId, remainingRequests);
            })
                .isInstanceOf(UserAgentException.class);
        }

        @Test
        @DisplayName("try-catch로 예외 처리 가능")
        void shouldHandleExceptionInTryCatch() {
            // Given
            Long userAgentId = 12345L;
            Integer remainingRequests = 10;
            RateLimitExceededException caughtException = null;

            // When
            try {
                throw new RateLimitExceededException(userAgentId, remainingRequests);
            } catch (RateLimitExceededException e) {
                caughtException = e;
            }

            // Then
            assertThat(caughtException).isNotNull();
            assertThat(caughtException.getUserAgentId()).isEqualTo(userAgentId);
            assertThat(caughtException.getRemainingRequests()).isEqualTo(remainingRequests);
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("같은 예외는 자기 자신과 equal")
        void shouldBeEqualToItself() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, 10);

            // Then
            assertThat(exception).isEqualTo(exception);
        }

        @Test
        @DisplayName("null과는 equal하지 않음")
        void shouldNotBeEqualToNull() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, 10);

            // Then
            assertThat(exception).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입 객체와는 equal하지 않음")
        void shouldNotBeEqualToDifferentType() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, 10);
            String otherObject = "Rate Limit Exceeded";

            // Then
            assertThat(exception).isNotEqualTo(otherObject);
        }

        @Test
        @DisplayName("hashCode는 일관성 있게 반환")
        void shouldReturnConsistentHashCode() {
            // Given
            RateLimitExceededException exception = new RateLimitExceededException(12345L, 10);

            // When
            int hashCode1 = exception.hashCode();
            int hashCode2 = exception.hashCode();

            // Then
            assertThat(hashCode1).isEqualTo(hashCode2);
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("Rate Limit 초과 검증 시나리오")
        void shouldHandleRateLimitExceeded() {
            // Given
            Long userAgentId = 12345L;
            Integer remainingRequests = 0;

            // When & Then
            assertThatThrownBy(() -> {
                checkRateLimit(userAgentId, remainingRequests);
            })
                .isInstanceOf(RateLimitExceededException.class)
                .satisfies(ex -> {
                    RateLimitExceededException rateLimitEx = (RateLimitExceededException) ex;
                    assertThat(rateLimitEx.getUserAgentId()).isEqualTo(userAgentId);
                    assertThat(rateLimitEx.getRemainingRequests()).isEqualTo(remainingRequests);
                });
        }

        @Test
        @DisplayName("예외 정보를 로깅하는 시나리오")
        void shouldLogExceptionInformation() {
            // Given
            Long userAgentId = 12345L;
            Integer remainingRequests = 5;
            RateLimitExceededException exception = new RateLimitExceededException(userAgentId, remainingRequests);

            // When
            Map<String, Object> loggingContext = exception.args();

            // Then
            assertThat(loggingContext)
                .containsEntry("userAgentId", userAgentId)
                .containsEntry("remainingRequests", remainingRequests);
        }

        @Test
        @DisplayName("Rate Limit 회복 대기 시나리오")
        void shouldHandleRateLimitRecovery() {
            // Given
            Long userAgentId = 99999L;
            Integer remainingRequests = -1;  // 음수는 회복 대기 중

            // When & Then
            assertThatThrownBy(() -> {
                if (remainingRequests < 0) {
                    throw new RateLimitExceededException(userAgentId, remainingRequests);
                }
            })
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Rate Limit을 초과했습니다");
        }

        @Test
        @DisplayName("동시 요청 처리 시나리오")
        void shouldHandleConcurrentRequests() {
            // Given
            Long userAgentId = 12345L;
            Integer initialRequests = 100;

            // When & Then - 100개 요청 후 0이 되면 예외 발생
            assertThatThrownBy(() -> {
                processRequests(userAgentId, initialRequests, 100);
            })
                .isInstanceOf(RateLimitExceededException.class)
                .extracting(ex -> ((RateLimitExceededException) ex).getRemainingRequests())
                .isEqualTo(0);
        }

        private void checkRateLimit(Long userAgentId, Integer remainingRequests) {
            if (remainingRequests <= 0) {
                throw new RateLimitExceededException(userAgentId, remainingRequests);
            }
        }

        private void processRequests(Long userAgentId, Integer limit, Integer requests) {
            int remaining = limit - requests;
            if (remaining <= 0) {
                throw new RateLimitExceededException(userAgentId, remaining);
            }
        }
    }
}
