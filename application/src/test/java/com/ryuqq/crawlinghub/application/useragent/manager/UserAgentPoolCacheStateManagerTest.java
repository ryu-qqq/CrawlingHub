package com.ryuqq.crawlinghub.application.useragent.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentIdFixture;
import com.ryuqq.crawlinghub.application.useragent.port.out.command.UserAgentPoolCacheStatePort;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAgentPoolCacheStateManager 단위 테스트
 *
 * <p>StatePort 위임 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentPoolCacheStateManager 테스트")
class UserAgentPoolCacheStateManagerTest {

    @Mock private UserAgentPoolCacheStatePort statePort;

    @InjectMocks private UserAgentPoolCacheStateManager sut;

    @Nested
    @DisplayName("applyHealthDelta() 테스트")
    class ApplyHealthDelta {

        @Test
        @DisplayName("[성공] delta 적용 후 SUSPENDED 전환 여부 반환 - true")
        void shouldReturnTrueWhenSuspended() {
            UserAgentId id = UserAgentIdFixture.anAssignedId();
            given(statePort.applyHealthDelta(id, -10)).willReturn(true);

            boolean result = sut.applyHealthDelta(id, -10);

            assertThat(result).isTrue();
            then(statePort).should().applyHealthDelta(id, -10);
        }

        @Test
        @DisplayName("[성공] delta 적용 후 SUSPENDED 전환 없으면 false 반환")
        void shouldReturnFalseWhenNotSuspended() {
            UserAgentId id = UserAgentIdFixture.anAssignedId();
            given(statePort.applyHealthDelta(id, 5)).willReturn(false);

            boolean result = sut.applyHealthDelta(id, 5);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("setHealthScore() 테스트")
    class SetHealthScore {

        @Test
        @DisplayName("[성공] Health Score 직접 설정 위임")
        void shouldDelegateSetHealthScore() {
            UserAgentId id = UserAgentIdFixture.anAssignedId();

            sut.setHealthScore(id, 80);

            then(statePort).should().setHealthScore(id, 80);
        }
    }
}
