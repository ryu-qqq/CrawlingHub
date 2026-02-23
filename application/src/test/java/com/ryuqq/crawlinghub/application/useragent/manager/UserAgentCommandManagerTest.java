package com.ryuqq.crawlinghub.application.useragent.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentIdFixture;
import com.ryuqq.crawlinghub.application.useragent.port.out.command.UserAgentPersistencePort;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAgentCommandManager 단위 테스트
 *
 * <p>PersistencePort 위임 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentCommandManager 테스트")
class UserAgentCommandManagerTest {

    @Mock private UserAgentPersistencePort persistencePort;

    @InjectMocks private UserAgentCommandManager sut;

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] UserAgent 저장 후 ID 반환")
        void shouldPersistAndReturnId() {
            UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
            UserAgentId expectedId = UserAgentIdFixture.anAssignedId();
            given(persistencePort.persist(userAgent)).willReturn(expectedId);

            UserAgentId result = sut.persist(userAgent);

            assertThat(result).isEqualTo(expectedId);
            then(persistencePort).should().persist(userAgent);
        }
    }

    @Nested
    @DisplayName("persistAll() 테스트")
    class PersistAll {

        @Test
        @DisplayName("[성공] 복수 UserAgent 저장 위임")
        void shouldPersistAllUserAgents() {
            UserAgent ua1 = UserAgentFixture.anUserAgentWithId(1L);
            UserAgent ua2 = UserAgentFixture.anUserAgentWithId(2L);
            List<UserAgent> userAgents = List.of(ua1, ua2);

            sut.persistAll(userAgents);

            then(persistencePort).should().persistAll(userAgents);
        }
    }
}
