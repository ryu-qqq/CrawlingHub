package com.ryuqq.crawlinghub.domain.schedule.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("SchedulerStatus Enum 단위 테스트")
class SchedulerStatusTest {

    @Nested
    @DisplayName("displayName() 테스트")
    class DisplayNameTest {

        @Test
        @DisplayName("ACTIVE의 표시 이름은 '활성'이다")
        void activeDisplayName() {
            assertThat(SchedulerStatus.ACTIVE.displayName()).isEqualTo("활성");
        }

        @Test
        @DisplayName("INACTIVE의 표시 이름은 '비활성'이다")
        void inactiveDisplayName() {
            assertThat(SchedulerStatus.INACTIVE.displayName()).isEqualTo("비활성");
        }
    }

    @Nested
    @DisplayName("enum 값 테스트")
    class EnumValuesTest {

        @Test
        @DisplayName("ACTIVE와 INACTIVE 두 가지 상태가 존재한다")
        void hasTwoValues() {
            assertThat(SchedulerStatus.values()).hasSize(2);
        }
    }
}
