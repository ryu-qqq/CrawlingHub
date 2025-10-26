package com.ryuqq.crawlinghub.application.mustit.seller.usecase;

import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.UpdateMustitSellerCommand;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UpdateMustitSellerCommand 단위 테스트
 * <p>
 * Command DTO의 유효성 검증 및 헬퍼 메서드를 테스트합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@DisplayName("UpdateMustitSellerCommand 단위 테스트")
class UpdateMustitSellerCommandTest {

    @Test
    @DisplayName("유효한 정보로 Command를 생성할 수 있다")
    void createCommandWithValidInfo() {
        // given & when
        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "SELLER001",
                false,
                CrawlIntervalType.HOURLY,
                6
        );

        // then
        assertThat(command.sellerId()).isEqualTo("SELLER001");
        assertThat(command.isActive()).isFalse();
        assertThat(command.intervalType()).isEqualTo(CrawlIntervalType.HOURLY);
        assertThat(command.intervalValue()).isEqualTo(6);
    }

    @Test
    @DisplayName("Optional 필드를 null로 Command를 생성할 수 있다")
    void createCommandWithNullOptionalFields() {
        // given & when
        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "SELLER001",
                null,
                null,
                null
        );

        // then
        assertThat(command.sellerId()).isEqualTo("SELLER001");
        assertThat(command.isActive()).isNull();
        assertThat(command.intervalType()).isNull();
        assertThat(command.intervalValue()).isNull();
    }

    @Test
    @DisplayName("sellerId가 null이면 예외가 발생한다")
    void throwExceptionWhenSellerIdIsNull() {
        // when & then
        assertThatThrownBy(() -> new UpdateMustitSellerCommand(
                null,
                false,
                CrawlIntervalType.DAILY,
                1
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sellerId must not be null or blank");
    }

    @Test
    @DisplayName("sellerId가 빈 문자열이면 예외가 발생한다")
    void throwExceptionWhenSellerIdIsBlank() {
        // when & then
        assertThatThrownBy(() -> new UpdateMustitSellerCommand(
                "   ",
                false,
                CrawlIntervalType.DAILY,
                1
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sellerId must not be null or blank");
    }

    @Test
    @DisplayName("intervalType만 있고 intervalValue가 없으면 예외가 발생한다")
    void throwExceptionWhenIntervalTypeWithoutValue() {
        // when & then
        assertThatThrownBy(() -> new UpdateMustitSellerCommand(
                "SELLER001",
                null,
                CrawlIntervalType.DAILY,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("intervalType and intervalValue must be both present or both absent");
    }

    @Test
    @DisplayName("intervalValue만 있고 intervalType이 없으면 예외가 발생한다")
    void throwExceptionWhenIntervalValueWithoutType() {
        // when & then
        assertThatThrownBy(() -> new UpdateMustitSellerCommand(
                "SELLER001",
                null,
                null,
                6
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("intervalType and intervalValue must be both present or both absent");
    }

    @Test
    @DisplayName("hasCrawlIntervalUpdate()는 intervalType과 intervalValue가 모두 있을 때 true를 반환한다")
    void hasCrawlIntervalUpdateReturnsTrueWhenBothPresent() {
        // given
        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "SELLER001",
                null,
                CrawlIntervalType.HOURLY,
                6
        );

        // when & then
        assertThat(command.hasCrawlIntervalUpdate()).isTrue();
    }

    @Test
    @DisplayName("hasCrawlIntervalUpdate()는 intervalType과 intervalValue가 모두 없을 때 false를 반환한다")
    void hasCrawlIntervalUpdateReturnsFalseWhenBothAbsent() {
        // given
        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "SELLER001",
                false,
                null,
                null
        );

        // when & then
        assertThat(command.hasCrawlIntervalUpdate()).isFalse();
    }

    @Test
    @DisplayName("hasActiveUpdate()는 isActive가 있을 때 true를 반환한다")
    void hasActiveUpdateReturnsTrueWhenPresent() {
        // given
        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "SELLER001",
                false,
                null,
                null
        );

        // when & then
        assertThat(command.hasActiveUpdate()).isTrue();
    }

    @Test
    @DisplayName("hasActiveUpdate()는 isActive가 null일 때 false를 반환한다")
    void hasActiveUpdateReturnsFalseWhenAbsent() {
        // given
        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "SELLER001",
                null,
                CrawlIntervalType.DAILY,
                1
        );

        // when & then
        assertThat(command.hasActiveUpdate()).isFalse();
    }

    @Test
    @DisplayName("활성화 상태만 업데이트하는 Command를 생성할 수 있다")
    void createCommandForActiveStatusOnly() {
        // given & when
        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "SELLER001",
                false,
                null,
                null
        );

        // then
        assertThat(command.hasActiveUpdate()).isTrue();
        assertThat(command.hasCrawlIntervalUpdate()).isFalse();
    }

    @Test
    @DisplayName("크롤링 주기만 업데이트하는 Command를 생성할 수 있다")
    void createCommandForCrawlIntervalOnly() {
        // given & when
        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "SELLER001",
                null,
                CrawlIntervalType.WEEKLY,
                2
        );

        // then
        assertThat(command.hasActiveUpdate()).isFalse();
        assertThat(command.hasCrawlIntervalUpdate()).isTrue();
    }

    @Test
    @DisplayName("모든 필드를 업데이트하는 Command를 생성할 수 있다")
    void createCommandForAllFields() {
        // given & when
        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "SELLER001",
                true,
                CrawlIntervalType.DAILY,
                1
        );

        // then
        assertThat(command.hasActiveUpdate()).isTrue();
        assertThat(command.hasCrawlIntervalUpdate()).isTrue();
    }
}
