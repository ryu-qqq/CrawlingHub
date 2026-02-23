package com.ryuqq.crawlinghub.application.monitoring.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult.OutboxDetail;
import com.ryuqq.crawlinghub.application.monitoring.port.out.query.MonitoringCompositeQueryPort;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GetOutboxSummaryService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: MonitoringCompositeQueryPort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetOutboxSummaryService 테스트")
class GetOutboxSummaryServiceTest {

    @Mock private MonitoringCompositeQueryPort monitoringCompositeQueryPort;

    @InjectMocks private GetOutboxSummaryService service;

    @Nested
    @DisplayName("execute() 아웃박스 요약 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 전체 아웃박스 요약 정보 반환")
        void shouldReturnOutboxSummary() {
            // Given
            OutboxDetail crawlTaskOutbox =
                    new OutboxDetail(Map.of("PENDING", 5L, "COMPLETED", 100L), 105L);
            OutboxDetail schedulerOutbox =
                    new OutboxDetail(Map.of("PENDING", 2L, "COMPLETED", 50L), 52L);
            OutboxDetail productSyncOutbox =
                    new OutboxDetail(Map.of("PENDING", 10L, "COMPLETED", 200L), 210L);
            OutboxSummaryResult expected =
                    new OutboxSummaryResult(crawlTaskOutbox, schedulerOutbox, productSyncOutbox);
            given(monitoringCompositeQueryPort.getOutboxSummary()).willReturn(expected);

            // When
            OutboxSummaryResult result = service.execute();

            // Then
            assertThat(result).isEqualTo(expected);
            assertThat(result.crawlTaskOutbox().total()).isEqualTo(105L);
            assertThat(result.schedulerOutbox().total()).isEqualTo(52L);
            assertThat(result.productSyncOutbox().total()).isEqualTo(210L);
            then(monitoringCompositeQueryPort).should().getOutboxSummary();
        }
    }
}
