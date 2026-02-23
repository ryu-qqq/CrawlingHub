package com.ryuqq.crawlinghub.application.schedule.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerOutBoxFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerOutBoxIdFixture;
import com.ryuqq.crawlinghub.application.schedule.port.out.command.CrawlScheduleOutBoxCommandPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerOutBoxId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerOutBoxCommandManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerOutBoxCommandManager 테스트")
class CrawlSchedulerOutBoxCommandManagerTest {

    @Mock private CrawlScheduleOutBoxCommandPort crawlScheduleOutBoxCommandPort;

    @InjectMocks private CrawlSchedulerOutBoxCommandManager manager;

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] CrawlSchedulerOutBox 저장 → ID 반환")
        void shouldPersistOutBoxAndReturnId() {
            // Given
            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            CrawlSchedulerOutBoxId expectedId = CrawlSchedulerOutBoxIdFixture.anAssignedId();

            given(crawlScheduleOutBoxCommandPort.persist(outBox)).willReturn(expectedId);

            // When
            CrawlSchedulerOutBoxId result = manager.persist(outBox);

            // Then
            assertThat(result).isEqualTo(expectedId);
            verify(crawlScheduleOutBoxCommandPort).persist(outBox);
        }
    }
}
