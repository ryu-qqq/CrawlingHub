package com.ryuqq.crawlinghub.domain.useragent.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InvalidUserAgentException 테스트")
class InvalidUserAgentExceptionTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("유효한 userAgentString으로 예외 생성")
        void shouldCreateExceptionWithValidUserAgentString() {
            // Given
            String userAgentString = "invalid-agent-string";

            // When
            InvalidUserAgentException exception = new InvalidUserAgentException(userAgentString);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains("유효하지 않은 User-Agent 문자열입니다");
            assertThat(exception.getMessage()).contains(userAgentString);
        }

        @Test
        @DisplayName("null userAgentString으로 예외 생성")
        void shouldCreateExceptionWithNullUserAgentString() {
            // Given
            String userAgentString = null;

            // When
            InvalidUserAgentException exception = new InvalidUserAgentException(userAgentString);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains("유효하지 않은 User-Agent 문자열입니다");
        }

        @Test
        @DisplayName("빈 문자열 userAgentString으로 예외 생성")
        void shouldCreateExceptionWithEmptyUserAgentString() {
            // Given
            String userAgentString = "";

            // When
            InvalidUserAgentException exception = new InvalidUserAgentException(userAgentString);

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains("유효하지 않은 User-Agent 문자열입니다");
            assertThat(exception.getMessage()).contains(userAgentString);
        }

        @Test
        @DisplayName("메시지 포맷이 올바르게 적용됨")
        void shouldFormatMessageCorrectly() {
            // Given
            String userAgentString = "test-agent";

            // When
            InvalidUserAgentException exception = new InvalidUserAgentException(userAgentString);

            // Then
            assertThat(exception.getMessage()).isEqualTo("유효하지 않은 User-Agent 문자열입니다: test-agent");
        }
    }

    @Nested
    @DisplayName("Getter 메서드 테스트")
    class GetterTests {

        @Test
        @DisplayName("getUserAgentString()은 저장된 값을 반환")
        void shouldReturnStoredUserAgentString() {
            // Given
            String userAgentString = "invalid-agent";
            InvalidUserAgentException exception = new InvalidUserAgentException(userAgentString);

            // When
            String result = exception.getUserAgentString();

            // Then
            assertThat(result).isEqualTo(userAgentString);
        }

        @Test
        @DisplayName("getUserAgentString()은 null을 반환할 수 있음")
        void shouldReturnNullIfUserAgentStringIsNull() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException(null);

            // When
            String result = exception.getUserAgentString();

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getUserAgentString()은 빈 문자열을 반환할 수 있음")
        void shouldReturnEmptyStringIfUserAgentStringIsEmpty() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException("");

            // When
            String result = exception.getUserAgentString();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("args() 메서드 테스트")
    class ArgsTests {

        @Test
        @DisplayName("args()는 userAgentString을 포함하는 Map 반환")
        void shouldReturnMapWithUserAgentString() {
            // Given
            String userAgentString = "invalid-agent";
            InvalidUserAgentException exception = new InvalidUserAgentException(userAgentString);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).isNotNull();
            assertThat(args).containsKey("userAgentString");
            assertThat(args.get("userAgentString")).isEqualTo(userAgentString);
        }

        @Test
        @DisplayName("args()는 null을 'null' 문자열로 변환하여 반환")
        void shouldReturnNullAsStringInArgs() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException(null);

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).isNotNull();
            assertThat(args).containsKey("userAgentString");
            assertThat(args.get("userAgentString")).isEqualTo("null");
        }

        @Test
        @DisplayName("args()는 불변 Map 반환 (수정 불가)")
        void shouldReturnImmutableMap() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException("invalid-agent");
            Map<String, Object> args = exception.args();

            // When & Then
            assertThatThrownBy(() -> args.put("newKey", "newValue"))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("args()는 정확히 1개의 키를 포함")
        void shouldReturnMapWithExactlyOneKey() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException("invalid-agent");

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).hasSize(1);
        }

        @Test
        @DisplayName("args()의 값은 문자열 타입")
        void shouldReturnStringTypeInArgsMap() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException("invalid-agent");

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args.get("userAgentString")).isInstanceOf(String.class);
        }
    }

    @Nested
    @DisplayName("메시지 테스트")
    class MessageTests {

        @Test
        @DisplayName("getMessage()는 null이 아님")
        void shouldHaveNonNullMessage() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException("invalid-agent");

            // When
            String message = exception.getMessage();

            // Then
            assertThat(message).isNotNull();
        }

        @Test
        @DisplayName("getMessage()는 비어있지 않음")
        void shouldHaveNonEmptyMessage() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException("invalid-agent");

            // When
            String message = exception.getMessage();

            // Then
            assertThat(message).isNotEmpty();
        }

        @Test
        @DisplayName("getMessage()는 한국어 메시지 포함")
        void shouldContainKoreanMessage() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException("invalid-agent");

            // When
            String message = exception.getMessage();

            // Then
            assertThat(message).contains("유효하지 않은");
            assertThat(message).contains("User-Agent");
            assertThat(message).contains("문자열");
        }

        @Test
        @DisplayName("getMessage()는 userAgentString을 포함")
        void shouldIncludeUserAgentStringInMessage() {
            // Given
            String userAgentString = "test-invalid-agent";
            InvalidUserAgentException exception = new InvalidUserAgentException(userAgentString);

            // When
            String message = exception.getMessage();

            // Then
            assertThat(message).contains(userAgentString);
        }
    }

    @Nested
    @DisplayName("상속 및 타입 테스트")
    class InheritanceTests {

        @Test
        @DisplayName("InvalidUserAgentException은 UserAgentException을 상속")
        void shouldExtendUserAgentException() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException("invalid-agent");

            // Then
            assertThat(exception).isInstanceOf(UserAgentException.class);
        }

        @Test
        @DisplayName("InvalidUserAgentException은 final 클래스")
        void shouldBeFinalClass() {
            // Given
            Class<InvalidUserAgentException> clazz = InvalidUserAgentException.class;

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
        @DisplayName("InvalidUserAgentException은 UserAgentException의 permitted subclass")
        void shouldBePermittedSubclass() {
            // Given
            Class<?>[] permittedSubclasses = UserAgentException.class.getPermittedSubclasses();

            // Then
            assertThat(permittedSubclasses).contains(InvalidUserAgentException.class);
        }

        @Test
        @DisplayName("InvalidUserAgentException은 Throwable의 하위 타입")
        void shouldBeThrowable() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException("invalid-agent");

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
            InvalidUserAgentException original = new InvalidUserAgentException("invalid-agent");

            // When
            byte[] serialized;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(original);
                serialized = bos.toByteArray();
            }

            InvalidUserAgentException deserialized;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                deserialized = (InvalidUserAgentException) ois.readObject();
            }

            // Then
            assertThat(deserialized).isNotNull();
            assertThat(deserialized.getMessage()).isEqualTo(original.getMessage());
            assertThat(deserialized.getUserAgentString()).isEqualTo(original.getUserAgentString());
        }

        @Test
        @DisplayName("null userAgentString을 가진 예외도 직렬화 가능")
        void shouldSerializeWithNullUserAgentString() throws IOException, ClassNotFoundException {
            // Given
            InvalidUserAgentException original = new InvalidUserAgentException(null);

            // When
            byte[] serialized;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(original);
                serialized = bos.toByteArray();
            }

            InvalidUserAgentException deserialized;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                deserialized = (InvalidUserAgentException) ois.readObject();
            }

            // Then
            assertThat(deserialized).isNotNull();
            assertThat(deserialized.getUserAgentString()).isNull();
        }
    }

    @Nested
    @DisplayName("예외 던지기 및 잡기 테스트")
    class ThrowAndCatchTests {

        @Test
        @DisplayName("예외를 던지고 잡을 수 있음")
        void shouldThrowAndCatchException() {
            // Given
            String userAgentString = "invalid-agent";

            // When & Then
            assertThatThrownBy(() -> {
                throw new InvalidUserAgentException(userAgentString);
            })
                .isInstanceOf(InvalidUserAgentException.class)
                .hasMessageContaining("유효하지 않은 User-Agent 문자열입니다")
                .hasMessageContaining(userAgentString);
        }

        @Test
        @DisplayName("UserAgentException으로 잡을 수 있음")
        void shouldBeCaughtAsUserAgentException() {
            // Given
            String userAgentString = "invalid-agent";

            // When & Then
            assertThatThrownBy(() -> {
                throw new InvalidUserAgentException(userAgentString);
            })
                .isInstanceOf(UserAgentException.class);
        }

        @Test
        @DisplayName("try-catch로 예외 처리 가능")
        void shouldHandleExceptionInTryCatch() {
            // Given
            String userAgentString = "invalid-agent";
            InvalidUserAgentException caughtException = null;

            // When
            try {
                throw new InvalidUserAgentException(userAgentString);
            } catch (InvalidUserAgentException e) {
                caughtException = e;
            }

            // Then
            assertThat(caughtException).isNotNull();
            assertThat(caughtException.getUserAgentString()).isEqualTo(userAgentString);
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("같은 예외는 자기 자신과 equal")
        void shouldBeEqualToItself() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException("invalid-agent");

            // Then
            assertThat(exception).isEqualTo(exception);
        }

        @Test
        @DisplayName("null과는 equal하지 않음")
        void shouldNotBeEqualToNull() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException("invalid-agent");

            // Then
            assertThat(exception).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입 객체와는 equal하지 않음")
        void shouldNotBeEqualToDifferentType() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException("invalid-agent");
            String otherObject = "invalid-agent";

            // Then
            assertThat(exception).isNotEqualTo(otherObject);
        }

        @Test
        @DisplayName("hashCode는 일관성 있게 반환")
        void shouldReturnConsistentHashCode() {
            // Given
            InvalidUserAgentException exception = new InvalidUserAgentException("invalid-agent");

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
        @DisplayName("유효하지 않은 User-Agent 검증 실패 시나리오")
        void shouldHandleInvalidUserAgentValidation() {
            // Given
            String invalidAgent = "Mozilla/5.0";  // 너무 짧거나 형식이 잘못된 경우

            // When & Then
            assertThatThrownBy(() -> {
                validateUserAgent(invalidAgent);
            })
                .isInstanceOf(InvalidUserAgentException.class)
                .extracting(ex -> ((InvalidUserAgentException) ex).getUserAgentString())
                .isEqualTo(invalidAgent);
        }

        @Test
        @DisplayName("빈 User-Agent 검증 실패 시나리오")
        void shouldHandleEmptyUserAgentValidation() {
            // Given
            String emptyAgent = "";

            // When & Then
            assertThatThrownBy(() -> {
                validateUserAgent(emptyAgent);
            })
                .isInstanceOf(InvalidUserAgentException.class)
                .hasMessageContaining("유효하지 않은 User-Agent 문자열입니다");
        }

        @Test
        @DisplayName("예외 정보를 로깅하는 시나리오")
        void shouldLogExceptionInformation() {
            // Given
            String invalidAgent = "bad-agent";
            InvalidUserAgentException exception = new InvalidUserAgentException(invalidAgent);

            // When
            Map<String, Object> loggingContext = exception.args();

            // Then
            assertThat(loggingContext).containsEntry("userAgentString", invalidAgent);
        }

        private void validateUserAgent(String userAgentString) {
            if (userAgentString == null || userAgentString.length() < 20) {
                throw new InvalidUserAgentException(userAgentString);
            }
        }
    }
}
