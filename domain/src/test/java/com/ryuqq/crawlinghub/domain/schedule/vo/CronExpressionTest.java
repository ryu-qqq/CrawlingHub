package com.ryuqq.crawlinghub.domain.schedule.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.domain.schedule.exception.InvalidCronExpressionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("CronExpression Value Object 단위 테스트")
class CronExpressionTest {

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryTest {

        @Test
        @DisplayName("유효한 AWS EventBridge 형식 cron 표현식으로 생성한다")
        void createWithValidExpression() {
            CronExpression cron = CronExpression.of("cron(0 9 * * ? *)");
            assertThat(cron.value()).isEqualTo("cron(0 9 * * ? *)");
        }

        @Test
        @DisplayName("앞뒤 공백이 trim된다")
        void trimsWhitespace() {
            CronExpression cron = CronExpression.of("  cron(0 9 * * ? *)  ");
            assertThat(cron.value()).isEqualTo("cron(0 9 * * ? *)");
        }

        @Test
        @DisplayName("매 시 정각(0분) cron 표현식이 생성된다")
        void createHourlyExpression() {
            CronExpression cron = CronExpression.of("cron(0 * * * ? *)");
            assertThat(cron.value()).isNotNull();
        }
    }

    @Nested
    @DisplayName("생성 실패 테스트 - 형식 검증")
    class FormatValidationTest {

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void nullThrowsException() {
            assertThatThrownBy(() -> CronExpression.of(null)).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("빈 문자열이면 예외가 발생한다")
        void emptyThrowsException() {
            assertThatThrownBy(() -> CronExpression.of(""))
                    .isInstanceOf(InvalidCronExpressionException.class);
        }

        @Test
        @DisplayName("cron() 형식이 아니면 예외가 발생한다")
        void invalidFormatThrowsException() {
            assertThatThrownBy(() -> CronExpression.of("0 9 * * *"))
                    .isInstanceOf(InvalidCronExpressionException.class);
        }

        @Test
        @DisplayName("필드 개수가 6개가 아니면 예외가 발생한다")
        void wrongFieldCountThrowsException() {
            assertThatThrownBy(() -> CronExpression.of("cron(0 9 * *)"))
                    .isInstanceOf(InvalidCronExpressionException.class);
        }

        @Test
        @DisplayName("잘못된 필드 문자가 포함되면 예외가 발생한다")
        void invalidFieldCharactersThrowsException() {
            assertThatThrownBy(() -> CronExpression.of("cron(@ 9 * * ? *)"))
                    .isInstanceOf(InvalidCronExpressionException.class);
        }
    }

    @Nested
    @DisplayName("최소 실행 간격 검증 테스트")
    class MinimumIntervalValidationTest {

        @Test
        @DisplayName("분 필드가 와일드카드(*)이면 예외가 발생한다 - 최소 간격 1시간 위반")
        void wildcardMinuteThrowsException() {
            assertThatThrownBy(() -> CronExpression.of("cron(* 9 * * ? *)"))
                    .isInstanceOf(InvalidCronExpressionException.class);
        }

        @Test
        @DisplayName("분 필드가 범위(0-30)이면 예외가 발생한다")
        void rangeMinuteThrowsException() {
            assertThatThrownBy(() -> CronExpression.of("cron(0-30 9 * * ? *)"))
                    .isInstanceOf(InvalidCronExpressionException.class);
        }

        @Test
        @DisplayName("분 필드가 0이면 허용된다")
        void zeroMinuteIsAllowed() {
            CronExpression cron = CronExpression.of("cron(0 9 * * ? *)");
            assertThat(cron.value()).isNotNull();
        }

        @Test
        @DisplayName("분 필드가 59이면 허용된다")
        void fiftyNineMinuteIsAllowed() {
            CronExpression cron = CronExpression.of("cron(59 9 * * ? *)");
            assertThat(cron.value()).isNotNull();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 표현식이면 동일하다")
        void sameExpressionAreEqual() {
            CronExpression cron1 = CronExpression.of("cron(0 9 * * ? *)");
            CronExpression cron2 = CronExpression.of("cron(0 9 * * ? *)");
            assertThat(cron1).isEqualTo(cron2);
            assertThat(cron1.hashCode()).isEqualTo(cron2.hashCode());
        }

        @Test
        @DisplayName("다른 표현식이면 다르다")
        void differentExpressionsAreNotEqual() {
            CronExpression cron1 = CronExpression.of("cron(0 9 * * ? *)");
            CronExpression cron2 = CronExpression.of("cron(0 18 * * ? *)");
            assertThat(cron1).isNotEqualTo(cron2);
        }
    }
}
