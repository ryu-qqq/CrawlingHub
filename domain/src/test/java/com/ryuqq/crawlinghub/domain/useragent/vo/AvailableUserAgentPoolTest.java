package com.ryuqq.crawlinghub.domain.useragent.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AvailableUserAgentPool 테스트")
class AvailableUserAgentPoolTest {

    @Nested
    @DisplayName("생성자 검증")
    class Constructor {

        @Test
        @DisplayName("[실패] null 리스트 -> NoAvailableUserAgentException")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> new AvailableUserAgentPool(null))
                    .isInstanceOf(NoAvailableUserAgentException.class);
        }

        @Test
        @DisplayName("[실패] 빈 리스트 -> NoAvailableUserAgentException")
        void shouldThrowWhenEmpty() {
            assertThatThrownBy(() -> new AvailableUserAgentPool(Collections.emptyList()))
                    .isInstanceOf(NoAvailableUserAgentException.class);
        }

        @Test
        @DisplayName("[성공] 유효한 리스트 -> 정상 생성")
        void shouldCreateWithValidList() {
            UserAgent agent = UserAgentFixture.anAvailableUserAgent();

            AvailableUserAgentPool pool = new AvailableUserAgentPool(List.of(agent));

            assertThat(pool.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("selectBest() 테스트")
    class SelectBest {

        @Test
        @DisplayName("[성공] healthScore가 가장 높은 에이전트 반환")
        void shouldSelectHighestHealthScore() {
            UserAgent low = UserAgentFixture.anAvailableUserAgent(1L, 50);
            UserAgent high = UserAgentFixture.anAvailableUserAgent(2L, 90);
            UserAgent mid = UserAgentFixture.anAvailableUserAgent(3L, 70);

            AvailableUserAgentPool pool = new AvailableUserAgentPool(List.of(low, high, mid));
            UserAgent selected = pool.selectBest();

            assertThat(selected.getId().value()).isEqualTo(2L);
            assertThat(selected.getHealthScoreValue()).isEqualTo(90);
        }

        @Test
        @DisplayName("[성공] 동일 healthScore -> 예외 없이 아무거나 반환")
        void shouldReturnAnyWhenSameHealthScore() {
            UserAgent agent1 = UserAgentFixture.anAvailableUserAgent(1L, 80);
            UserAgent agent2 = UserAgentFixture.anAvailableUserAgent(2L, 80);

            AvailableUserAgentPool pool = new AvailableUserAgentPool(List.of(agent1, agent2));
            UserAgent selected = pool.selectBest();

            assertThat(selected.getHealthScoreValue()).isEqualTo(80);
        }

        @Test
        @DisplayName("[성공] 단일 에이전트 -> 해당 에이전트 반환")
        void shouldReturnSingleAgent() {
            UserAgent only = UserAgentFixture.anAvailableUserAgent(1L, 100);

            AvailableUserAgentPool pool = new AvailableUserAgentPool(List.of(only));
            UserAgent selected = pool.selectBest();

            assertThat(selected.getId().value()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("size() 테스트")
    class Size {

        @Test
        @DisplayName("[성공] Pool 크기 반환")
        void shouldReturnCorrectSize() {
            UserAgent a1 = UserAgentFixture.anAvailableUserAgent(1L, 80);
            UserAgent a2 = UserAgentFixture.anAvailableUserAgent(2L, 90);
            UserAgent a3 = UserAgentFixture.anAvailableUserAgent(3L, 70);

            AvailableUserAgentPool pool = new AvailableUserAgentPool(List.of(a1, a2, a3));

            assertThat(pool.size()).isEqualTo(3);
        }
    }
}
