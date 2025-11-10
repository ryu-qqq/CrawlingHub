package com.ryuqq.crawlinghub.domain.useragent.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TokenExpiredException 테스트")
class TokenExpiredExceptionTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("유효한 userAgentId로 예외 생성")
        void shouldCreateExceptionWithValidUserAgentId() {
            // Given
            Long userAgentId = 12345L;

            // When
            TokenExpiredException exception = new TokenExpiredException(userAgentId);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains("User-Agent의 토큰이 만료되었습니다");
            assertThat(exception.getMessage()).contains(userAgentId.toString());
        }

        @Test
        @DisplayName("0 값의 userAgentId로 예외 생성")
        void shouldCreateExceptionWithZeroUserAgentId() {
            // Given
            Long userAgentId = 0L;

            // When
            TokenExpiredException exception = new TokenExpiredException(userAgentId);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains("0");
        }

        @Test
        @DisplayName("음수 userAgentId로 예외 생성")
        void shouldCreateExceptionWithNegativeUserAgentId() {
            // Given
            Long userAgentId = -1L;

            // When
            TokenExpiredException exception = new TokenExpiredException(userAgentId);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains("-1");
        }

        @Test
        @DisplayName("메시지 포맷이 올바르게 적용됨")
        void shouldFormatMessageCorrectly() {
            // Given
            Long userAgentId = 999L;

            // When
            TokenExpiredException exception = new TokenExpiredException(userAgentId);

            // Then
            assertThat(exception.getMessage()).isEqualTo("User-Agent의 토큰이 만료되었습니다: 999");
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
            TokenExpiredException exception = new TokenExpiredException(userAgentId);

            // When
            Long result = exception.getUserAgentId();

            // Then
            assertThat(result).isEqualTo(userAgentId);
        }

        @Test
        @DisplayName("getUserAgentId()는 0을 반환할 수 있음")
        void shouldReturnZeroIfUserAgentIdIsZero() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(0L);

            // When
            Long result = exception.getUserAgentId();

            // Then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("getUserAgentId()는 음수를 반환할 수 있음")
        void shouldReturnNegativeIfUserAgentIdIsNegative() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(-1L);

            // When
            Long result = exception.getUserAgentId();

            // Then
            assertThat(result).isEqualTo(-1L);
        }

        @Test
        @DisplayName("getUserAgentId()는 큰 값도 반환 가능")
        void shouldReturnLargeUserAgentId() {
            // Given
            Long largeId = Long.MAX_VALUE;
            TokenExpiredException exception = new TokenExpiredException(largeId);

            // When
            Long result = exception.getUserAgentId();

            // Then
            assertThat(result).isEqualTo(largeId);
        }
    }

    @Nested
    @DisplayName("args() 메서드 테스트")
    class ArgsTests {

        @Test
        @DisplayName("args()는 userAgentId를 포함하는 Map 반환")
        void shouldReturnMapWithUserAgentId() {
            // Given
            Long userAgentId = 12345L;
            TokenExpiredException exception = new TokenExpiredException(userAgentId);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).isNotNull();
            assertThat(args).containsKey("userAgentId");
            assertThat(args.get("userAgentId")).isEqualTo(userAgentId);
        }

        @Test
        @DisplayName("args()는 불변 Map 반환 (수정 불가)")
        void shouldReturnImmutableMap() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(12345L);
            Map<String, Object> args = exception.args();

            // When & Then
            assertThatThrownBy(() -> args.put("newKey", "newValue"))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("args()는 정확히 1개의 키를 포함")
        void shouldReturnMapWithExactlyOneKey() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(12345L);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).hasSize(1);
        }

        @Test
        @DisplayName("args()의 값은 Long 타입")
        void shouldReturnLongTypeInArgsMap() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(12345L);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args.get("userAgentId")).isInstanceOf(Long.class);
        }

        @Test
        @DisplayName("args()는 0 값도 포함 가능")
        void shouldReturnZeroInArgs() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(0L);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args.get("userAgentId")).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("메시지 테스트")
    class MessageTests {

        @Test
        @DisplayName("getMessage()는 null이 아님")
        void shouldHaveNonNullMessage() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(12345L);

            // When
            String message = exception.getMessage();

            // Then
            assertThat(message).isNotNull();
        }

        @Test
        @DisplayName("getMessage()는 비어있지 않음")
        void shouldHaveNonEmptyMessage() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(12345L);

            // When
            String message = exception.getMessage();

            // Then
            assertThat(message).isNotEmpty();
        }

        @Test
        @DisplayName("getMessage()는 한국어 메시지 포함")
        void shouldContainKoreanMessage() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(12345L);

            // When
            String message = exception.getMessage();

            // Then
            assertThat(message).contains("User-Agent");
            assertThat(message).contains("토큰");
            assertThat(message).contains("만료");
        }

        @Test
        @DisplayName("getMessage()는 userAgentId를 포함")
        void shouldIncludeUserAgentIdInMessage() {
            // Given
            Long userAgentId = 99999L;
            TokenExpiredException exception = new TokenExpiredException(userAgentId);

            // When
            String message = exception.getMessage();

            // Then
            assertThat(message).contains(userAgentId.toString());
        }
    }

    @Nested
    @DisplayName("상속 및 타입 테스트")
    class InheritanceTests {

        @Test
        @DisplayName("TokenExpiredException은 UserAgentException을 상속")
        void shouldExtendUserAgentException() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(12345L);

            // Then
            assertThat(exception).isInstanceOf(UserAgentException.class);
        }

        @Test
        @DisplayName("TokenExpiredException은 final 클래스")
        void shouldBeFinalClass() {
            // Given
            Class<TokenExpiredException> clazz = TokenExpiredException.class;

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
        @DisplayName("TokenExpiredException은 UserAgentException의 permitted subclass")
        void shouldBePermittedSubclass() {
            // Given
            Class<?>[] permittedSubclasses = UserAgentException.class.getPermittedSubclasses();

            // Then
            assertThat(permittedSubclasses).contains(TokenExpiredException.class);
        }

        @Test
        @DisplayName("TokenExpiredException은 Throwable의 하위 타입")
        void shouldBeThrowable() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(12345L);

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
            TokenExpiredException original = new TokenExpiredException(12345L);

            // When
            byte[] serialized;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(original);
                serialized = bos.toByteArray();
            }

            TokenExpiredException deserialized;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                deserialized = (TokenExpiredException) ois.readObject();
            }

            // Then
            assertThat(deserialized).isNotNull();
            assertThat(deserialized.getMessage()).isEqualTo(original.getMessage());
            assertThat(deserialized.getUserAgentId()).isEqualTo(original.getUserAgentId());
        }

        @Test
        @DisplayName("0 값을 가진 예외도 직렬화 가능")
        void shouldSerializeWithZeroUserAgentId() throws IOException, ClassNotFoundException {
            // Given
            TokenExpiredException original = new TokenExpiredException(0L);

            // When
            byte[] serialized;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(original);
                serialized = bos.toByteArray();
            }

            TokenExpiredException deserialized;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                deserialized = (TokenExpiredException) ois.readObject();
            }

            // Then
            assertThat(deserialized).isNotNull();
            assertThat(deserialized.getUserAgentId()).isEqualTo(0L);
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

            // When & Then
            assertThatThrownBy(() -> {
                throw new TokenExpiredException(userAgentId);
            })
                .isInstanceOf(TokenExpiredException.class)
                .hasMessageContaining("User-Agent의 토큰이 만료되었습니다")
                .hasMessageContaining(userAgentId.toString());
        }

        @Test
        @DisplayName("UserAgentException으로 잡을 수 있음")
        void shouldBeCaughtAsUserAgentException() {
            // Given
            Long userAgentId = 12345L;

            // When & Then
            assertThatThrownBy(() -> {
                throw new TokenExpiredException(userAgentId);
            })
                .isInstanceOf(UserAgentException.class);
        }

        @Test
        @DisplayName("try-catch로 예외 처리 가능")
        void shouldHandleExceptionInTryCatch() {
            // Given
            Long userAgentId = 12345L;
            TokenExpiredException caughtException = null;

            // When
            try {
                throw new TokenExpiredException(userAgentId);
            } catch (TokenExpiredException e) {
                caughtException = e;
            }

            // Then
            assertThat(caughtException).isNotNull();
            assertThat(caughtException.getUserAgentId()).isEqualTo(userAgentId);
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("같은 예외는 자기 자신과 equal")
        void shouldBeEqualToItself() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(12345L);

            // Then
            assertThat(exception).isEqualTo(exception);
        }

        @Test
        @DisplayName("null과는 equal하지 않음")
        void shouldNotBeEqualToNull() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(12345L);

            // Then
            assertThat(exception).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입 객체와는 equal하지 않음")
        void shouldNotBeEqualToDifferentType() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(12345L);
            Long otherId = 12345L;

            // Then
            assertThat(exception).isNotEqualTo(otherId);
        }

        @Test
        @DisplayName("hashCode는 일관성 있게 반환")
        void shouldReturnConsistentHashCode() {
            // Given
            TokenExpiredException exception = new TokenExpiredException(12345L);

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
        @DisplayName("토큰 만료 검증 시나리오")
        void shouldHandleTokenExpirationCheck() {
            // Given
            Long userAgentId = 12345L;

            // When & Then
            assertThatThrownBy(() -> {
                checkTokenExpiration(userAgentId, true);
            })
                .isInstanceOf(TokenExpiredException.class)
                .extracting(ex -> ((TokenExpiredException) ex).getUserAgentId())
                .isEqualTo(userAgentId);
        }

        @Test
        @DisplayName("예외 정보를 로깅하는 시나리오")
        void shouldLogExceptionInformation() {
            // Given
            Long userAgentId = 12345L;
            TokenExpiredException exception = new TokenExpiredException(userAgentId);

            // When
            Map<String, Object> loggingContext = exception.args();

            // Then
            assertThat(loggingContext).containsEntry("userAgentId", userAgentId);
        }

        @Test
        @DisplayName("토큰 갱신 실패 시나리오")
        void shouldHandleTokenRefreshFailure() {
            // Given
            Long userAgentId = 99999L;

            // When & Then
            assertThatThrownBy(() -> {
                refreshToken(userAgentId, false);
            })
                .isInstanceOf(TokenExpiredException.class)
                .hasMessageContaining("토큰이 만료되었습니다");
        }

        private void checkTokenExpiration(Long userAgentId, boolean isExpired) {
            if (isExpired) {
                throw new TokenExpiredException(userAgentId);
            }
        }

        private void refreshToken(Long userAgentId, boolean canRefresh) {
            if (!canRefresh) {
                throw new TokenExpiredException(userAgentId);
            }
        }
    }
}
