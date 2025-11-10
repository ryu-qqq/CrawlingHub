package com.ryuqq.crawlinghub.domain.change;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * NotificationStatus 테스트
 */
@DisplayName("NotificationStatus 테스트")
class NotificationStatusTest {

    @Nested
    @DisplayName("Enum 상수 값 테스트")
    class EnumConstantTests {

        @Test
        @DisplayName("PENDING 상수의 priority와 description 검증")
        void shouldHaveCorrectPendingValues() {
            NotificationStatus status = NotificationStatus.PENDING;
            assertThat(status.getPriority()).isEqualTo(1);
            assertThat(status.getDescription()).isEqualTo("대기");
        }

        @Test
        @DisplayName("SENT 상수의 priority와 description 검증")
        void shouldHaveCorrectSentValues() {
            NotificationStatus status = NotificationStatus.SENT;
            assertThat(status.getPriority()).isEqualTo(2);
            assertThat(status.getDescription()).isEqualTo("전송됨");
        }

        @Test
        @DisplayName("FAILED 상수의 priority와 description 검증")
        void shouldHaveCorrectFailedValues() {
            NotificationStatus status = NotificationStatus.FAILED;
            assertThat(status.getPriority()).isEqualTo(3);
            assertThat(status.getDescription()).isEqualTo("실패");
        }

        @Test
        @DisplayName("모든 Enum 상수는 정확히 3개")
        void shouldHaveExactlyThreeValues() {
            NotificationStatus[] values = NotificationStatus.values();
            assertThat(values).hasSize(3);
            assertThat(values).containsExactly(
                NotificationStatus.PENDING,
                NotificationStatus.SENT,
                NotificationStatus.FAILED
            );
        }

        @Test
        @DisplayName("priority 값은 1, 2, 3 순서로 할당")
        void shouldHavePrioritiesInOrder() {
            assertThat(NotificationStatus.PENDING.getPriority()).isEqualTo(1);
            assertThat(NotificationStatus.SENT.getPriority()).isEqualTo(2);
            assertThat(NotificationStatus.FAILED.getPriority()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("isSent() 메서드 테스트")
    class IsSentTests {

        @Test
        @DisplayName("SENT 상태는 true 반환")
        void shouldReturnTrueForSent() {
            assertThat(NotificationStatus.SENT.isSent()).isTrue();
        }

        @Test
        @DisplayName("PENDING 상태는 false 반환")
        void shouldReturnFalseForPending() {
            assertThat(NotificationStatus.PENDING.isSent()).isFalse();
        }

        @Test
        @DisplayName("FAILED 상태는 false 반환")
        void shouldReturnFalseForFailed() {
            assertThat(NotificationStatus.FAILED.isSent()).isFalse();
        }
    }

    @Nested
    @DisplayName("isFailed() 메서드 테스트")
    class IsFailedTests {

        @Test
        @DisplayName("FAILED 상태는 true 반환")
        void shouldReturnTrueForFailed() {
            assertThat(NotificationStatus.FAILED.isFailed()).isTrue();
        }

        @Test
        @DisplayName("PENDING 상태는 false 반환")
        void shouldReturnFalseForPending() {
            assertThat(NotificationStatus.PENDING.isFailed()).isFalse();
        }

        @Test
        @DisplayName("SENT 상태는 false 반환")
        void shouldReturnFalseForSent() {
            assertThat(NotificationStatus.SENT.isFailed()).isFalse();
        }
    }

    @Nested
    @DisplayName("fromString() 팩토리 메서드 테스트")
    class FromStringTests {

        @Test
        @DisplayName("PENDING 문자열로 PENDING 상수 생성")
        void shouldCreatePendingFromString() {
            NotificationStatus status = NotificationStatus.fromString("PENDING");
            assertThat(status).isEqualTo(NotificationStatus.PENDING);
        }

        @Test
        @DisplayName("SENT 문자열로 SENT 상수 생성")
        void shouldCreateSentFromString() {
            NotificationStatus status = NotificationStatus.fromString("SENT");
            assertThat(status).isEqualTo(NotificationStatus.SENT);
        }

        @Test
        @DisplayName("FAILED 문자열로 FAILED 상수 생성")
        void shouldCreateFailedFromString() {
            NotificationStatus status = NotificationStatus.fromString("FAILED");
            assertThat(status).isEqualTo(NotificationStatus.FAILED);
        }

        @Test
        @DisplayName("소문자 입력도 대문자로 변환하여 처리")
        void shouldHandleLowercaseInput() {
            NotificationStatus pending = NotificationStatus.fromString("pending");
            NotificationStatus sent = NotificationStatus.fromString("sent");
            NotificationStatus failed = NotificationStatus.fromString("failed");

            assertThat(pending).isEqualTo(NotificationStatus.PENDING);
            assertThat(sent).isEqualTo(NotificationStatus.SENT);
            assertThat(failed).isEqualTo(NotificationStatus.FAILED);
        }

        @Test
        @DisplayName("대소문자 혼합 입력도 처리")
        void shouldHandleMixedCaseInput() {
            NotificationStatus pending = NotificationStatus.fromString("Pending");
            NotificationStatus sent = NotificationStatus.fromString("SeNt");
            NotificationStatus failed = NotificationStatus.fromString("FaIlEd");

            assertThat(pending).isEqualTo(NotificationStatus.PENDING);
            assertThat(sent).isEqualTo(NotificationStatus.SENT);
            assertThat(failed).isEqualTo(NotificationStatus.FAILED);
        }

        @Test
        @DisplayName("앞뒤 공백은 trim 처리")
        void shouldTrimWhitespace() {
            NotificationStatus pending = NotificationStatus.fromString("  PENDING  ");
            NotificationStatus sent = NotificationStatus.fromString("\tSENT\t");
            NotificationStatus failed = NotificationStatus.fromString("\nFAILED\n");

            assertThat(pending).isEqualTo(NotificationStatus.PENDING);
            assertThat(sent).isEqualTo(NotificationStatus.SENT);
            assertThat(failed).isEqualTo(NotificationStatus.FAILED);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("null 또는 빈 문자열은 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullOrEmpty(String input) {
            assertThatThrownBy(() -> NotificationStatus.fromString(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NotificationStatus는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"UNKNOWN", "INVALID", "NOT_A_STATUS", "123", ""})
        @DisplayName("유효하지 않은 문자열은 IllegalArgumentException 발생")
        void shouldThrowExceptionForInvalidString(String input) {
            // 빈 문자열은 필수 검증 메시지, 나머지는 유효하지 않은 상태 메시지
            if (input.isBlank()) {
                assertThatThrownBy(() -> NotificationStatus.fromString(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("NotificationStatus는 필수입니다");
            } else {
                assertThatThrownBy(() -> NotificationStatus.fromString(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 NotificationStatus입니다");
            }
        }
    }

    @Nested
    @DisplayName("Enum 기본 메서드 테스트")
    class EnumBasicMethodsTests {

        @Test
        @DisplayName("valueOf()로 Enum 상수 가져오기")
        void shouldGetEnumByValueOf() {
            NotificationStatus pending = NotificationStatus.valueOf("PENDING");
            NotificationStatus sent = NotificationStatus.valueOf("SENT");
            NotificationStatus failed = NotificationStatus.valueOf("FAILED");

            assertThat(pending).isEqualTo(NotificationStatus.PENDING);
            assertThat(sent).isEqualTo(NotificationStatus.SENT);
            assertThat(failed).isEqualTo(NotificationStatus.FAILED);
        }

        @Test
        @DisplayName("name() 메서드는 Enum 상수 이름 반환")
        void shouldReturnNameByNameMethod() {
            assertThat(NotificationStatus.PENDING.name()).isEqualTo("PENDING");
            assertThat(NotificationStatus.SENT.name()).isEqualTo("SENT");
            assertThat(NotificationStatus.FAILED.name()).isEqualTo("FAILED");
        }

        @Test
        @DisplayName("ordinal() 메서드는 선언 순서 반환")
        void shouldReturnOrdinalByOrdinalMethod() {
            assertThat(NotificationStatus.PENDING.ordinal()).isEqualTo(0);
            assertThat(NotificationStatus.SENT.ordinal()).isEqualTo(1);
            assertThat(NotificationStatus.FAILED.ordinal()).isEqualTo(2);
        }

        @Test
        @DisplayName("toString() 메서드는 Enum 상수 이름 반환")
        void shouldReturnNameByToStringMethod() {
            assertThat(NotificationStatus.PENDING.toString()).isEqualTo("PENDING");
            assertThat(NotificationStatus.SENT.toString()).isEqualTo("SENT");
            assertThat(NotificationStatus.FAILED.toString()).isEqualTo("FAILED");
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("같은 Enum 상수는 equals로 동일")
        void shouldBeEqualForSameConstant() {
            NotificationStatus pending1 = NotificationStatus.PENDING;
            NotificationStatus pending2 = NotificationStatus.PENDING;

            assertThat(pending1).isEqualTo(pending2);
            assertThat(pending1 == pending2).isTrue();
        }

        @Test
        @DisplayName("다른 Enum 상수는 equals로 다름")
        void shouldNotBeEqualForDifferentConstant() {
            NotificationStatus pending = NotificationStatus.PENDING;
            NotificationStatus sent = NotificationStatus.SENT;

            assertThat(pending).isNotEqualTo(sent);
        }

        @Test
        @DisplayName("같은 Enum 상수는 같은 hashCode")
        void shouldHaveSameHashCodeForSameConstant() {
            NotificationStatus pending1 = NotificationStatus.PENDING;
            NotificationStatus pending2 = NotificationStatus.PENDING;

            assertThat(pending1.hashCode()).isEqualTo(pending2.hashCode());
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("Switch 문에서 사용")
        void shouldWorkInSwitchStatement() {
            NotificationStatus status = NotificationStatus.SENT;

            String result = switch (status) {
                case PENDING -> "대기 중";
                case SENT -> "전송 완료";
                case FAILED -> "전송 실패";
            };

            assertThat(result).isEqualTo("전송 완료");
        }

        @Test
        @DisplayName("Map의 키로 사용")
        void shouldBeUsableAsMapKey() {
            java.util.Map<NotificationStatus, String> statusMap = new java.util.HashMap<>();
            statusMap.put(NotificationStatus.PENDING, "대기 중");
            statusMap.put(NotificationStatus.SENT, "전송됨");
            statusMap.put(NotificationStatus.FAILED, "실패");

            assertThat(statusMap.get(NotificationStatus.SENT)).isEqualTo("전송됨");
        }

        @Test
        @DisplayName("EnumSet에서 사용")
        void shouldWorkInEnumSet() {
            java.util.EnumSet<NotificationStatus> activeStatuses =
                java.util.EnumSet.of(NotificationStatus.PENDING, NotificationStatus.SENT);

            assertThat(activeStatuses).containsExactlyInAnyOrder(
                NotificationStatus.PENDING,
                NotificationStatus.SENT
            );
            assertThat(activeStatuses).doesNotContain(NotificationStatus.FAILED);
        }

        @Test
        @DisplayName("상태 판별 로직 시나리오")
        void shouldHandleStatusCheckScenario() {
            NotificationStatus pending = NotificationStatus.PENDING;
            NotificationStatus sent = NotificationStatus.SENT;
            NotificationStatus failed = NotificationStatus.FAILED;

            // PENDING: 전송도 실패도 아님
            assertThat(pending.isSent()).isFalse();
            assertThat(pending.isFailed()).isFalse();

            // SENT: 전송됨
            assertThat(sent.isSent()).isTrue();
            assertThat(sent.isFailed()).isFalse();

            // FAILED: 실패
            assertThat(failed.isSent()).isFalse();
            assertThat(failed.isFailed()).isTrue();
        }

        @Test
        @DisplayName("외부 입력 처리 시나리오")
        void shouldHandleExternalInputScenario() {
            // 사용자 입력: 소문자, 공백 포함
            String userInput = "  sent  ";
            NotificationStatus status = NotificationStatus.fromString(userInput);

            // 올바른 상수로 변환
            assertThat(status).isEqualTo(NotificationStatus.SENT);
            assertThat(status.getDescription()).isEqualTo("전송됨");
            assertThat(status.getPriority()).isEqualTo(2);
        }

        @Test
        @DisplayName("우선순위 기반 정렬 시나리오")
        void shouldSortByPriorityScenario() {
            java.util.List<NotificationStatus> statuses = java.util.Arrays.asList(
                NotificationStatus.FAILED,
                NotificationStatus.SENT,
                NotificationStatus.PENDING
            );

            statuses.sort(java.util.Comparator.comparingInt(NotificationStatus::getPriority));

            assertThat(statuses).containsExactly(
                NotificationStatus.PENDING,   // priority 1
                NotificationStatus.SENT,      // priority 2
                NotificationStatus.FAILED     // priority 3
            );
        }
    }
}
