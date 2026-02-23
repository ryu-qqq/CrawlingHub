package com.ryuqq.crawlinghub.application.schedule.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.crawlinghub.application.schedule.port.out.command.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerTransactionManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerTransactionManager 테스트")
class CrawlSchedulerCommandManagerTest {

    @Mock private CrawlScheduleCommandPort crawlSchedulerPersistencePort;

    @InjectMocks private CrawlSchedulerCommandManager manager;

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] CrawlScheduler 저장 → CrawlSchedulerId 반환")
        void shouldPersistSchedulerAndReturnId() {
            // Given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerId expectedId = CrawlSchedulerIdFixture.anAssignedId();

            given(crawlSchedulerPersistencePort.persist(scheduler)).willReturn(expectedId);

            // When
            CrawlSchedulerId result = manager.persist(scheduler);

            // Then
            assertThat(result).isEqualTo(expectedId);
            verify(crawlSchedulerPersistencePort).persist(scheduler);
        }
    }
}
