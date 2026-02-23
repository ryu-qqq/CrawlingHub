package com.ryuqq.crawlinghub.application.execution.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionIdFixture;
import com.ryuqq.crawlinghub.application.execution.port.out.command.CrawlExecutionPersistencePort;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.id.CrawlExecutionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlExecutionCommandManager 단위 테스트
 *
 * <p>persist-only 패턴 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlExecutionCommandManager 테스트")
class CrawlExecutionCommandManagerTest {

    @Mock private CrawlExecutionPersistencePort crawlExecutionPersistencePort;

    @InjectMocks private CrawlExecutionCommandManager manager;

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] CrawlExecution 저장 → ID 반환")
        void shouldPersistExecution() {
            // Given
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            CrawlExecutionId expectedId = CrawlExecutionIdFixture.anAssignedId();

            given(crawlExecutionPersistencePort.persist(execution)).willReturn(expectedId);

            // When
            CrawlExecutionId result = manager.persist(execution);

            // Then
            assertThat(result).isEqualTo(expectedId);
            then(crawlExecutionPersistencePort).should().persist(execution);
        }

        @Test
        @DisplayName("[성공] 신규 CrawlExecution 저장")
        void shouldPersistNewExecution() {
            // Given
            CrawlExecution newExecution = CrawlExecutionFixture.forNew();
            CrawlExecutionId expectedId = CrawlExecutionIdFixture.anAssignedId();

            given(crawlExecutionPersistencePort.persist(newExecution)).willReturn(expectedId);

            // When
            CrawlExecutionId result = manager.persist(newExecution);

            // Then
            assertThat(result).isEqualTo(expectedId);
            then(crawlExecutionPersistencePort).should().persist(newExecution);
        }
    }
}
