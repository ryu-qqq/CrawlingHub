package com.ryuqq.crawlinghub.domain.seller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SellerStatus 테스트")
class SellerStatusTest {

    @Nested
    @DisplayName("Enum 상수 테스트")
    class EnumConstantsTests {

        @Test
        @DisplayName("3개의 SellerStatus 상태가 존재")
        void shouldHaveThreeStatuses() {
            // Given
            SellerStatus[] statuses = SellerStatus.values();

            // Then
            assertThat(statuses).hasSize(3);
            assertThat(statuses).containsExactlyInAnyOrder(
                SellerStatus.ACTIVE,
                SellerStatus.PAUSED,
                SellerStatus.DISABLED
            );
        }

        @Test
        @DisplayName("ACTIVE 상태는 priority 1")
        void activeShouldHavePriority1() {
            // Then
            assertThat(SellerStatus.ACTIVE.getPriority()).isEqualTo(1);
        }

        @Test
        @DisplayName("PAUSED 상태는 priority 2")
        void pausedShouldHavePriority2() {
            // Then
            assertThat(SellerStatus.PAUSED.getPriority()).isEqualTo(2);
        }

        @Test
        @DisplayName("DISABLED 상태는 priority 3")
        void disabledShouldHavePriority3() {
            // Then
            assertThat(SellerStatus.DISABLED.getPriority()).isEqualTo(3);
        }

        @Test
        @DisplayName("ACTIVE 상태는 description '활성'")
        void activeShouldHaveDescription() {
            // Then
            assertThat(SellerStatus.ACTIVE.getDescription()).isEqualTo("활성");
        }

        @Test
        @DisplayName("PAUSED 상태는 description '일시정지'")
        void pausedShouldHaveDescription() {
            // Then
            assertThat(SellerStatus.PAUSED.getDescription()).isEqualTo("일시정지");
        }

        @Test
        @DisplayName("DISABLED 상태는 description '비활성'")
        void disabledShouldHaveDescription() {
            // Then
            assertThat(SellerStatus.DISABLED.getDescription()).isEqualTo("비활성");
        }
    }

    @Nested
    @DisplayName("getPriority() 메서드 테스트")
    class GetPriorityTests {

        @Test
        @DisplayName("ACTIVE priority는 1")
        void activePriorityShouldBe1() {
            // When
            int priority = SellerStatus.ACTIVE.getPriority();

            // Then
            assertThat(priority).isEqualTo(1);
        }

        @Test
        @DisplayName("PAUSED priority는 2")
        void pausedPriorityShouldBe2() {
            // When
            int priority = SellerStatus.PAUSED.getPriority();

            // Then
            assertThat(priority).isEqualTo(2);
        }

        @Test
        @DisplayName("DISABLED priority는 3")
        void disabledPriorityShouldBe3() {
            // When
            int priority = SellerStatus.DISABLED.getPriority();

            // Then
            assertThat(priority).isEqualTo(3);
        }

        @Test
        @DisplayName("모든 priority는 고유한 값")
        void allPrioritiesShouldBeUnique() {
            // Given
            SellerStatus[] statuses = SellerStatus.values();

            // Then
            assertThat(statuses)
                .extracting(SellerStatus::getPriority)
                .doesNotHaveDuplicates();
        }
    }

    @Nested
    @DisplayName("getDescription() 메서드 테스트")
    class GetDescriptionTests {

        @Test
        @DisplayName("ACTIVE description은 '활성'")
        void activeDescriptionShouldBeActive() {
            // When
            String description = SellerStatus.ACTIVE.getDescription();

            // Then
            assertThat(description).isEqualTo("활성");
        }

        @Test
        @DisplayName("PAUSED description은 '일시정지'")
        void pausedDescriptionShouldBePaused() {
            // When
            String description = SellerStatus.PAUSED.getDescription();

            // Then
            assertThat(description).isEqualTo("일시정지");
        }

        @Test
        @DisplayName("DISABLED description은 '비활성'")
        void disabledDescriptionShouldBeDisabled() {
            // When
            String description = SellerStatus.DISABLED.getDescription();

            // Then
            assertThat(description).isEqualTo("비활성");
        }

        @Test
        @DisplayName("모든 description은 null이 아님")
        void allDescriptionsShouldNotBeNull() {
            // Given
            SellerStatus[] statuses = SellerStatus.values();

            // Then
            assertThat(statuses)
                .extracting(SellerStatus::getDescription)
                .doesNotContainNull();
        }

        @Test
        @DisplayName("모든 description은 비어있지 않음")
        void allDescriptionsShouldNotBeEmpty() {
            // Given
            SellerStatus[] statuses = SellerStatus.values();

            // Then
            assertThat(statuses)
                .extracting(SellerStatus::getDescription)
                .allMatch(desc -> !desc.isBlank());
        }
    }

    @Nested
    @DisplayName("isActive() 메서드 테스트")
    class IsActiveTests {

        @Test
        @DisplayName("ACTIVE 상태는 true 반환")
        void activeShouldReturnTrue() {
            // When
            boolean isActive = SellerStatus.ACTIVE.isActive();

            // Then
            assertThat(isActive).isTrue();
        }

        @Test
        @DisplayName("PAUSED 상태는 false 반환")
        void pausedShouldReturnFalse() {
            // When
            boolean isActive = SellerStatus.PAUSED.isActive();

            // Then
            assertThat(isActive).isFalse();
        }

        @Test
        @DisplayName("DISABLED 상태는 false 반환")
        void disabledShouldReturnFalse() {
            // When
            boolean isActive = SellerStatus.DISABLED.isActive();

            // Then
            assertThat(isActive).isFalse();
        }

        @Test
        @DisplayName("활성 상태는 ACTIVE만 해당")
        void onlyActiveShouldBeActive() {
            // Given
            SellerStatus[] statuses = SellerStatus.values();

            // Then
            long activeCount = java.util.Arrays.stream(statuses)
                .filter(SellerStatus::isActive)
                .count();

            assertThat(activeCount).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("canCrawl() 메서드 테스트")
    class CanCrawlTests {

        @Test
        @DisplayName("ACTIVE 상태는 크롤링 가능 (true)")
        void activeShouldAllowCrawl() {
            // When
            boolean canCrawl = SellerStatus.ACTIVE.canCrawl();

            // Then
            assertThat(canCrawl).isTrue();
        }

        @Test
        @DisplayName("PAUSED 상태는 크롤링 가능 (true)")
        void pausedShouldAllowCrawl() {
            // When
            boolean canCrawl = SellerStatus.PAUSED.canCrawl();

            // Then
            assertThat(canCrawl).isTrue();
        }

        @Test
        @DisplayName("DISABLED 상태는 크롤링 불가 (false)")
        void disabledShouldNotAllowCrawl() {
            // When
            boolean canCrawl = SellerStatus.DISABLED.canCrawl();

            // Then
            assertThat(canCrawl).isFalse();
        }

        @Test
        @DisplayName("크롤링 불가 상태는 DISABLED만 해당")
        void onlyDisabledShouldNotAllowCrawl() {
            // Given
            SellerStatus[] statuses = SellerStatus.values();

            // Then
            long cannotCrawlCount = java.util.Arrays.stream(statuses)
                .filter(status -> !status.canCrawl())
                .count();

            assertThat(cannotCrawlCount).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("fromString() 팩토리 메서드 테스트")
    class FromStringTests {

        @Test
        @DisplayName("'ACTIVE' 문자열로 ACTIVE 상태 생성")
        void shouldCreateActiveFromString() {
            // When
            SellerStatus status = SellerStatus.fromString("ACTIVE");

            // Then
            assertThat(status).isEqualTo(SellerStatus.ACTIVE);
        }

        @Test
        @DisplayName("'PAUSED' 문자열로 PAUSED 상태 생성")
        void shouldCreatePausedFromString() {
            // When
            SellerStatus status = SellerStatus.fromString("PAUSED");

            // Then
            assertThat(status).isEqualTo(SellerStatus.PAUSED);
        }

        @Test
        @DisplayName("'DISABLED' 문자열로 DISABLED 상태 생성")
        void shouldCreateDisabledFromString() {
            // When
            SellerStatus status = SellerStatus.fromString("DISABLED");

            // Then
            assertThat(status).isEqualTo(SellerStatus.DISABLED);
        }

        @ParameterizedTest
        @ValueSource(strings = {"active", "paused", "disabled"})
        @DisplayName("소문자 문자열도 변환 가능 (대소문자 무시)")
        void shouldConvertLowercaseStrings(String statusStr) {
            // When
            SellerStatus status = SellerStatus.fromString(statusStr);

            // Then
            assertThat(status).isNotNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"  ACTIVE  ", "\tPAUSED\t", "\nDISABLED\n"})
        @DisplayName("앞뒤 공백은 trim 처리")
        void shouldTrimWhitespace(String statusStr) {
            // When
            SellerStatus status = SellerStatus.fromString(statusStr);

            // Then
            assertThat(status).isNotNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 또는 빈 문자열은 IllegalArgumentException 발생")
        void shouldThrowExceptionForNullOrEmpty(String statusStr) {
            // When & Then
            assertThatThrownBy(() -> SellerStatus.fromString(statusStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SellerStatus는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("공백 문자만 있는 문자열은 IllegalArgumentException 발생")
        void shouldThrowExceptionForBlankString(String statusStr) {
            // When & Then
            assertThatThrownBy(() -> SellerStatus.fromString(statusStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SellerStatus는 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "UNKNOWN", "RUNNING", "STOPPED"})
        @DisplayName("유효하지 않은 문자열은 IllegalArgumentException 발생")
        void shouldThrowExceptionForInvalidString(String statusStr) {
            // When & Then
            assertThatThrownBy(() -> SellerStatus.fromString(statusStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 SellerStatus입니다");
        }

        @Test
        @DisplayName("예외 메시지에 잘못된 문자열 포함")
        void exceptionShouldContainInvalidString() {
            // Given
            String invalidStatus = "INVALID_STATUS";

            // When & Then
            assertThatThrownBy(() -> SellerStatus.fromString(invalidStatus))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(invalidStatus);
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("활성 셀러 확인 시나리오")
        void shouldIdentifyActiveSeller() {
            // Given
            SellerStatus status = SellerStatus.ACTIVE;

            // When & Then
            assertThat(status.isActive()).isTrue();
            assertThat(status.canCrawl()).isTrue();
            assertThat(status.getDescription()).isEqualTo("활성");
        }

        @Test
        @DisplayName("일시정지 셀러 확인 시나리오")
        void shouldIdentifyPausedSeller() {
            // Given
            SellerStatus status = SellerStatus.PAUSED;

            // When & Then
            assertThat(status.isActive()).isFalse();
            assertThat(status.canCrawl()).isTrue();  // PAUSED는 크롤링 가능
            assertThat(status.getDescription()).isEqualTo("일시정지");
        }

        @Test
        @DisplayName("비활성 셀러 확인 시나리오")
        void shouldIdentifyDisabledSeller() {
            // Given
            SellerStatus status = SellerStatus.DISABLED;

            // When & Then
            assertThat(status.isActive()).isFalse();
            assertThat(status.canCrawl()).isFalse();  // DISABLED는 크롤링 불가
            assertThat(status.getDescription()).isEqualTo("비활성");
        }

        @Test
        @DisplayName("크롤링 가능 셀러 필터링 시나리오")
        void shouldFilterCrawlableSellersByStatus() {
            // Given
            SellerStatus[] allStatuses = SellerStatus.values();

            // When
            long crawlableCount = java.util.Arrays.stream(allStatuses)
                .filter(SellerStatus::canCrawl)
                .count();

            // Then
            assertThat(crawlableCount).isEqualTo(2);  // ACTIVE, PAUSED
        }

        @Test
        @DisplayName("상태 전환 시나리오 (문자열 기반)")
        void shouldTransitionStatusFromString() {
            // Given
            SellerStatus currentStatus = SellerStatus.ACTIVE;

            // When
            SellerStatus newStatus = SellerStatus.fromString("PAUSED");

            // Then
            assertThat(currentStatus).isNotEqualTo(newStatus);
            assertThat(currentStatus.isActive()).isTrue();
            assertThat(newStatus.isActive()).isFalse();
        }

        @Test
        @DisplayName("우선순위 정렬 시나리오")
        void shouldSortByPriority() {
            // Given
            SellerStatus[] statuses = SellerStatus.values();

            // When
            java.util.List<SellerStatus> sorted = java.util.Arrays.stream(statuses)
                .sorted(java.util.Comparator.comparingInt(SellerStatus::getPriority))
                .toList();

            // Then
            assertThat(sorted).containsExactly(
                SellerStatus.ACTIVE,
                SellerStatus.PAUSED,
                SellerStatus.DISABLED
            );
        }

        @Test
        @DisplayName("Map 키로 사용 가능")
        void shouldBeUsableAsMapKey() {
            // Given
            java.util.Map<SellerStatus, String> statusMap = new java.util.HashMap<>();

            // When
            statusMap.put(SellerStatus.ACTIVE, "활성 셀러");
            statusMap.put(SellerStatus.PAUSED, "일시정지 셀러");
            statusMap.put(SellerStatus.DISABLED, "비활성 셀러");

            // Then
            assertThat(statusMap.get(SellerStatus.ACTIVE)).isEqualTo("활성 셀러");
            assertThat(statusMap.get(SellerStatus.PAUSED)).isEqualTo("일시정지 셀러");
            assertThat(statusMap.get(SellerStatus.DISABLED)).isEqualTo("비활성 셀러");
        }

        @Test
        @DisplayName("Set에서 중복 제거 가능")
        void shouldBeUsableInSet() {
            // Given
            java.util.Set<SellerStatus> set = new java.util.HashSet<>();

            // When
            set.add(SellerStatus.ACTIVE);
            set.add(SellerStatus.ACTIVE);  // 중복
            set.add(SellerStatus.PAUSED);

            // Then
            assertThat(set).hasSize(2);
            assertThat(set).containsExactlyInAnyOrder(SellerStatus.ACTIVE, SellerStatus.PAUSED);
        }

        @Test
        @DisplayName("잘못된 상태 문자열 검증 시나리오")
        void shouldValidateInvalidStatusString() {
            // When & Then
            assertThatThrownBy(() -> SellerStatus.fromString(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SellerStatus는 필수입니다");

            assertThatThrownBy(() -> SellerStatus.fromString(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SellerStatus는 필수입니다");

            assertThatThrownBy(() -> SellerStatus.fromString("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 SellerStatus입니다");
        }
    }
}
