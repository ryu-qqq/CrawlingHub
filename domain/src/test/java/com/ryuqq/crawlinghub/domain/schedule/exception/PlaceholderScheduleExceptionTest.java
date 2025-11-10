package com.ryuqq.crawlinghub.domain.schedule.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PlaceholderScheduleException 테스트")
class PlaceholderScheduleExceptionTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("기본 생성자로 예외 생성")
        void shouldCreateExceptionWithDefaultConstructor() {
            // When
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // Then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo("Placeholder exception");
        }

        @Test
        @DisplayName("생성된 예외는 ScheduleException을 상속")
        void shouldExtendScheduleException() {
            // When
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // Then
            assertThat(exception).isInstanceOf(ScheduleException.class);
        }

        @Test
        @DisplayName("생성된 예외는 DomainException을 상속 (간접)")
        void shouldExtendDomainException() {
            // When
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // Then
            assertThat(exception).isInstanceOf(com.ryuqq.crawlinghub.domain.common.DomainException.class);
        }

        @Test
        @DisplayName("생성된 예외는 RuntimeException을 상속 (간접)")
        void shouldExtendRuntimeException() {
            // When
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("cause는 null")
        void shouldHaveNullCause() {
            // When
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // Then
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("code() 메서드 테스트")
    class CodeTests {

        @Test
        @DisplayName("code()는 'SCHEDULE-001' 반환")
        void shouldReturnSchedulePlaceholderCode() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            String code = exception.code();

            // Then
            assertThat(code).isEqualTo("SCHEDULE-001");
        }

        @Test
        @DisplayName("code()는 null이 아님")
        void shouldReturnNonNullCode() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            String code = exception.code();

            // Then
            assertThat(code).isNotNull();
        }

        @Test
        @DisplayName("code()는 빈 문자열이 아님")
        void shouldReturnNonEmptyCode() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            String code = exception.code();

            // Then
            assertThat(code).isNotBlank();
        }

        @Test
        @DisplayName("code()는 'SCHEDULE-' 접두사로 시작")
        void shouldStartWithSchedulePrefix() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            String code = exception.code();

            // Then
            assertThat(code).startsWith("SCHEDULE-");
        }

        @Test
        @DisplayName("code()는 항상 동일한 값 반환 (Idempotent)")
        void shouldReturnSameCodeOnMultipleCalls() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            String code1 = exception.code();
            String code2 = exception.code();
            String code3 = exception.code();

            // Then
            assertThat(code1).isEqualTo(code2).isEqualTo(code3);
        }
    }

    @Nested
    @DisplayName("message() 메서드 테스트")
    class MessageTests {

        @Test
        @DisplayName("message()는 'Placeholder exception' 반환")
        void shouldReturnPlaceholderMessage() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            String message = exception.message();

            // Then
            assertThat(message).isEqualTo("Placeholder exception");
        }

        @Test
        @DisplayName("message()는 getMessage()와 동일")
        void shouldReturnSameValueAsGetMessage() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            String message = exception.message();
            String getMessage = exception.getMessage();

            // Then
            assertThat(message).isEqualTo(getMessage);
        }

        @Test
        @DisplayName("message()는 null이 아님")
        void shouldReturnNonNullMessage() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            String message = exception.message();

            // Then
            assertThat(message).isNotNull();
        }

        @Test
        @DisplayName("message()는 빈 문자열이 아님")
        void shouldReturnNonEmptyMessage() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            String message = exception.message();

            // Then
            assertThat(message).isNotBlank();
        }

        @Test
        @DisplayName("message()는 항상 동일한 값 반환 (Idempotent)")
        void shouldReturnSameMessageOnMultipleCalls() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            String message1 = exception.message();
            String message2 = exception.message();
            String message3 = exception.message();

            // Then
            assertThat(message1).isEqualTo(message2).isEqualTo(message3);
        }
    }

    @Nested
    @DisplayName("args() 메서드 테스트")
    class ArgsTests {

        @Test
        @DisplayName("args()는 빈 Map 반환")
        void shouldReturnEmptyMap() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).isEmpty();
        }

        @Test
        @DisplayName("args()는 null이 아님")
        void shouldReturnNonNullMap() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).isNotNull();
        }

        @Test
        @DisplayName("args()는 항상 크기가 0")
        void shouldAlwaysReturnMapWithSizeZero() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).hasSize(0);
        }

        @Test
        @DisplayName("args()는 불변 Map 반환 (UnsupportedOperationException)")
        void shouldReturnImmutableMap() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();
            Map<String, Object> args = exception.args();

            // When & Then
            assertThatThrownBy(() -> args.put("key", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("args()는 항상 동일한 인스턴스 반환 (Idempotent)")
        void shouldReturnSameMapInstanceOnMultipleCalls() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            Map<String, Object> args1 = exception.args();
            Map<String, Object> args2 = exception.args();

            // Then
            assertThat(args1).isEqualTo(args2);
        }

        @Test
        @DisplayName("args()는 Map.of()의 결과와 동일")
        void shouldReturnSameAsMapOf() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();
            Map<String, Object> expected = Map.of();

            // When
            Map<String, Object> args = exception.args();

            // Then
            assertThat(args).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Serialization 테스트")
    class SerializationTests {

        @Test
        @DisplayName("예외는 직렬화 가능")
        void shouldBeSerializable() throws IOException {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(exception);
            oos.close();

            // Then
            assertThat(baos.toByteArray()).isNotEmpty();
        }

        @Test
        @DisplayName("직렬화/역직렬화 후에도 동일한 속성 유지")
        void shouldMaintainPropertiesAfterSerialization() throws IOException, ClassNotFoundException {
            // Given
            PlaceholderScheduleException original = new PlaceholderScheduleException();

            // When
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            PlaceholderScheduleException deserialized = (PlaceholderScheduleException) ois.readObject();
            ois.close();

            // Then
            assertThat(deserialized.code()).isEqualTo(original.code());
            assertThat(deserialized.message()).isEqualTo(original.message());
            assertThat(deserialized.getMessage()).isEqualTo(original.getMessage());
        }

        @Test
        @DisplayName("역직렬화된 예외도 빈 args() 반환")
        void shouldReturnEmptyArgsAfterDeserialization() throws IOException, ClassNotFoundException {
            // Given
            PlaceholderScheduleException original = new PlaceholderScheduleException();

            // When
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            PlaceholderScheduleException deserialized = (PlaceholderScheduleException) ois.readObject();
            ois.close();

            // Then
            assertThat(deserialized.args()).isEmpty();
        }
    }

    @Nested
    @DisplayName("상속 및 타입 테스트")
    class InheritanceTests {

        @Test
        @DisplayName("PlaceholderScheduleException은 final 클래스")
        void shouldBeFinalClass() {
            // When
            Class<PlaceholderScheduleException> clazz = PlaceholderScheduleException.class;

            // Then
            assertThat(java.lang.reflect.Modifier.isFinal(clazz.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("ScheduleException은 sealed 클래스")
        void shouldHaveSealedParent() {
            // When
            Class<ScheduleException> parentClass = ScheduleException.class;

            // Then
            assertThat(parentClass.isSealed()).isTrue();
        }

        @Test
        @DisplayName("ScheduleException의 유일한 permitted subclass")
        void shouldBeOnlyPermittedSubclass() {
            // When
            Class<?>[] permittedSubclasses = ScheduleException.class.getPermittedSubclasses();

            // Then
            assertThat(permittedSubclasses).containsExactly(PlaceholderScheduleException.class);
        }

        @Test
        @DisplayName("예외는 Throwable을 상속 (간접)")
        void shouldExtendThrowable() {
            // When
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // Then
            assertThat(exception).isInstanceOf(Throwable.class);
        }

        @Test
        @DisplayName("예외는 Exception을 상속 (간접)")
        void shouldExtendException() {
            // When
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // Then
            assertThat(exception).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Immutability 테스트")
    class ImmutabilityTests {

        @Test
        @DisplayName("code()는 호출마다 동일한 참조 반환")
        void shouldReturnSameCodeReference() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            String code1 = exception.code();
            String code2 = exception.code();

            // Then
            assertThat(code1).isSameAs(code2);  // 동일 인스턴스
        }

        @Test
        @DisplayName("message()는 호출마다 동일한 참조 반환")
        void shouldReturnSameMessageReference() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            String message1 = exception.message();
            String message2 = exception.message();

            // Then
            assertThat(message1).isSameAs(message2);  // 동일 인스턴스
        }

        @Test
        @DisplayName("args() Map 수정 시도는 UnsupportedOperationException 발생")
        void shouldPreventArgsModification() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();
            Map<String, Object> args = exception.args();

            // When & Then - put
            assertThatThrownBy(() -> args.put("newKey", "newValue"))
                .isInstanceOf(UnsupportedOperationException.class);

            // When & Then - remove
            assertThatThrownBy(() -> args.remove("anyKey"))
                .isInstanceOf(UnsupportedOperationException.class);

            // When & Then - clear
            assertThatThrownBy(() -> args.clear())
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("throw 키워드로 예외 던지기")
        void shouldBeThrowable() {
            // When & Then
            assertThatThrownBy(() -> {
                throw new PlaceholderScheduleException();
            }).isInstanceOf(PlaceholderScheduleException.class)
              .hasMessage("Placeholder exception");
        }

        @Test
        @DisplayName("catch 블록에서 예외 처리")
        void shouldBeCatchableAsPlaceholderScheduleException() {
            // Given
            PlaceholderScheduleException caughtException = null;

            // When
            try {
                throw new PlaceholderScheduleException();
            } catch (PlaceholderScheduleException e) {
                caughtException = e;
            }

            // Then
            assertThat(caughtException).isNotNull();
            assertThat(caughtException.code()).isEqualTo("SCHEDULE-001");
        }

        @Test
        @DisplayName("ScheduleException 타입으로 catch 가능")
        void shouldBeCatchableAsScheduleException() {
            // Given
            ScheduleException caughtException = null;

            // When
            try {
                throw new PlaceholderScheduleException();
            } catch (ScheduleException e) {
                caughtException = e;
            }

            // Then
            assertThat(caughtException).isNotNull();
            assertThat(caughtException).isInstanceOf(PlaceholderScheduleException.class);
        }

        @Test
        @DisplayName("RuntimeException 타입으로 catch 가능")
        void shouldBeCatchableAsRuntimeException() {
            // Given
            RuntimeException caughtException = null;

            // When
            try {
                throw new PlaceholderScheduleException();
            } catch (RuntimeException e) {
                caughtException = e;
            }

            // Then
            assertThat(caughtException).isNotNull();
            assertThat(caughtException).isInstanceOf(PlaceholderScheduleException.class);
        }

        @Test
        @DisplayName("Switch expression에서 패턴 매칭 (Java 21)")
        void shouldWorkWithSwitchExpression() {
            // Given
            ScheduleException exception = new PlaceholderScheduleException();

            // When
            String result = switch (exception) {
                case PlaceholderScheduleException e -> "Placeholder: " + e.code();
            };

            // Then
            assertThat(result).isEqualTo("Placeholder: SCHEDULE-001");
        }

        @Test
        @DisplayName("로깅 시나리오 - 예외 정보 추출")
        void shouldProvideLoggingInformation() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            String logMessage = String.format("Exception occurred: code=%s, message=%s, args=%s",
                exception.code(),
                exception.message(),
                exception.args());

            // Then
            assertThat(logMessage).contains("SCHEDULE-001")
                                  .contains("Placeholder exception")
                                  .contains("{}");
        }
    }

    @Nested
    @DisplayName("equals() 및 hashCode() 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("같은 인스턴스는 equals()로 동일")
        void shouldBeEqualToItself() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When & Then
            assertThat(exception).isEqualTo(exception);
        }

        @Test
        @DisplayName("다른 인스턴스는 equals()로 동일하지 않음 (기본 Object 동작)")
        void shouldNotBeEqualToDifferentInstance() {
            // Given
            PlaceholderScheduleException exception1 = new PlaceholderScheduleException();
            PlaceholderScheduleException exception2 = new PlaceholderScheduleException();

            // When & Then
            assertThat(exception1).isNotEqualTo(exception2);  // Object.equals() 사용
        }

        @Test
        @DisplayName("null과는 equals()로 동일하지 않음")
        void shouldNotBeEqualToNull() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When & Then
            assertThat(exception).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입과는 equals()로 동일하지 않음")
        void shouldNotBeEqualToDifferentType() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();
            Object other = new Object();

            // When & Then
            assertThat(exception).isNotEqualTo(other);
        }

        @Test
        @DisplayName("hashCode()는 일관성 있게 반환 (Consistent)")
        void shouldReturnConsistentHashCode() {
            // Given
            PlaceholderScheduleException exception = new PlaceholderScheduleException();

            // When
            int hashCode1 = exception.hashCode();
            int hashCode2 = exception.hashCode();
            int hashCode3 = exception.hashCode();

            // Then
            assertThat(hashCode1).isEqualTo(hashCode2).isEqualTo(hashCode3);
        }
    }
}
