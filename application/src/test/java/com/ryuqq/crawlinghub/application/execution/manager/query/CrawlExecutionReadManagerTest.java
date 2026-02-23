package com.ryuqq.crawlinghub.application.execution.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.execution.manager.CrawlExecutionReadManager;
import com.ryuqq.crawlinghub.application.execution.port.out.query.CrawlExecutionQueryPort;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.id.CrawlExecutionId;
import com.ryuqq.crawlinghub.domain.execution.query.CrawlExecutionCriteria;
import com.ryuqq.crawlinghub.domain.execution.query.CrawlExecutionStatisticsCriteria;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlExecutionReadManager 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlExecutionReadManager 테스트")
class CrawlExecutionReadManagerTest {

    @Mock private CrawlExecutionQueryPort crawlExecutionQueryPort;
    @Mock private CrawlExecution crawlExecution;
    @Mock private CrawlExecutionCriteria criteria;
    @Mock private CrawlExecutionStatisticsCriteria statisticsCriteria;
    @Mock private CrawlExecutionQueryPort.ErrorCount errorCount;

    private CrawlExecutionReadManager manager;

    @BeforeEach
    void setUp() {
        manager = new CrawlExecutionReadManager(crawlExecutionQueryPort);
    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindById {

        @Test
        @DisplayName("[성공] ID로 CrawlExecution 조회")
        void shouldDelegateToQueryPort() {
            // Given
            CrawlExecutionId crawlExecutionId = CrawlExecutionId.of(1L);
            given(crawlExecutionQueryPort.findById(crawlExecutionId))
                    .willReturn(Optional.of(crawlExecution));

            // When
            Optional<CrawlExecution> result = manager.findById(crawlExecutionId);

            // Then
            assertThat(result).isPresent().contains(crawlExecution);
            verify(crawlExecutionQueryPort).findById(crawlExecutionId);
        }

        @Test
        @DisplayName("[성공] 존재하지 않는 경우 empty 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            CrawlExecutionId crawlExecutionId = CrawlExecutionId.of(999L);
            given(crawlExecutionQueryPort.findById(crawlExecutionId)).willReturn(Optional.empty());

            // When
            Optional<CrawlExecution> result = manager.findById(crawlExecutionId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCriteria() 테스트")
    class FindByCriteria {

        @Test
        @DisplayName("[성공] 조건으로 CrawlExecution 목록 조회")
        void shouldDelegateToQueryPort() {
            // Given
            given(crawlExecutionQueryPort.findByCriteria(criteria))
                    .willReturn(List.of(crawlExecution));

            // When
            List<CrawlExecution> result = manager.findByCriteria(criteria);

            // Then
            assertThat(result).hasSize(1).contains(crawlExecution);
            verify(crawlExecutionQueryPort).findByCriteria(criteria);
        }

        @Test
        @DisplayName("[성공] 결과 없으면 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoResults() {
            // Given
            given(crawlExecutionQueryPort.findByCriteria(criteria)).willReturn(List.of());

            // When
            List<CrawlExecution> result = manager.findByCriteria(criteria);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByCriteria() 테스트")
    class CountByCriteria {

        @Test
        @DisplayName("[성공] 조건으로 CrawlExecution 개수 조회")
        void shouldDelegateToQueryPort() {
            // Given
            given(crawlExecutionQueryPort.countByCriteria(criteria)).willReturn(25L);

            // When
            long result = manager.countByCriteria(criteria);

            // Then
            assertThat(result).isEqualTo(25L);
            verify(crawlExecutionQueryPort).countByCriteria(criteria);
        }
    }

    @Nested
    @DisplayName("getTopErrors() 테스트")
    class GetTopErrors {

        @Test
        @DisplayName("[성공] 상위 에러 메시지 통계 조회")
        void shouldDelegateToQueryPort() {
            // Given
            int limit = 10;
            given(crawlExecutionQueryPort.getTopErrors(statisticsCriteria, limit))
                    .willReturn(List.of(errorCount));

            // When
            List<CrawlExecutionQueryPort.ErrorCount> result =
                    manager.getTopErrors(statisticsCriteria, limit);

            // Then
            assertThat(result).hasSize(1).contains(errorCount);
            verify(crawlExecutionQueryPort).getTopErrors(statisticsCriteria, limit);
        }

        @Test
        @DisplayName("[성공] 에러 없으면 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoErrors() {
            // Given
            int limit = 10;
            given(crawlExecutionQueryPort.getTopErrors(statisticsCriteria, limit))
                    .willReturn(List.of());

            // When
            List<CrawlExecutionQueryPort.ErrorCount> result =
                    manager.getTopErrors(statisticsCriteria, limit);

            // Then
            assertThat(result).isEmpty();
        }
    }
}
