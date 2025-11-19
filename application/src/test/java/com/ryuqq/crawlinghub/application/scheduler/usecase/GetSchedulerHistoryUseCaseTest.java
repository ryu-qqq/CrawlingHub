package com.ryuqq.crawlinghub.application.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.application.common.pagination.PageResult;
import com.ryuqq.crawlinghub.application.port.out.query.SchedulerHistoryQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.dto.query.GetSchedulerHistoryQuery;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerHistoryResponse;
import com.ryuqq.crawlinghub.application.scheduler.fixture.assembler.SchedulerAssemblerFixture;
import com.ryuqq.crawlinghub.application.scheduler.fixture.query.GetSchedulerHistoryQueryFixture;
import com.ryuqq.crawlinghub.application.scheduler.service.query.GetSchedulerHistoryService;
import com.ryuqq.crawlinghub.domain.eventbridge.history.SchedulerHistory;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerId;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("GetSchedulerHistoryUseCase")
class GetSchedulerHistoryUseCaseTest {

    private final SchedulerHistoryQueryPort schedulerHistoryQueryPort = Mockito.mock(SchedulerHistoryQueryPort.class);
    private final SchedulerAssembler schedulerAssembler = SchedulerAssemblerFixture.create();

    private GetSchedulerHistoryService useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetSchedulerHistoryService(
            schedulerHistoryQueryPort,
            schedulerAssembler
        );
    }

    @Test
    @DisplayName("should get history with pagination")
    void shouldGetHistoryWithPagination() {
        GetSchedulerHistoryQuery query = GetSchedulerHistoryQueryFixture.create();
        LocalDateTime now = LocalDateTime.now();
        SchedulerHistory history1 = SchedulerHistory.of(
            1L,
            query.schedulerId(),
            "status",
            "ACTIVE",
            "INACTIVE",
            now.minusDays(1)
        );
        SchedulerHistory history2 = SchedulerHistory.of(
            2L,
            query.schedulerId(),
            "cronExpression",
            "cron(0 0 * * ? *)",
            "cron(0 1 * * ? *)",
            now.minusDays(2)
        );
        List<SchedulerHistory> histories = List.of(history1, history2);

        PageResult<SchedulerHistory> pageResult = new PageResult<SchedulerHistory>() {
            @Override
            public List<SchedulerHistory> content() {
                return histories;
            }

            @Override
            public int page() {
                return query.page();
            }

            @Override
            public int size() {
                return query.size();
            }

            @Override
            public long totalElements() {
                return 2L;
            }
        };

        given(schedulerHistoryQueryPort.findBySchedulerId(
            SchedulerId.of(query.schedulerId()),
            query.page(),
            query.size()
        )).willReturn(pageResult);

        PageResult<SchedulerHistoryResponse> result = useCase.execute(query);

        assertThat(result.content()).hasSize(2);
        assertThat(result.page()).isEqualTo(query.page());
        assertThat(result.size()).isEqualTo(query.size());
        assertThat(result.totalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("should order by occurredAt desc")
    void shouldOrderByOccurredAtDesc() {
        GetSchedulerHistoryQuery query = GetSchedulerHistoryQueryFixture.create();
        LocalDateTime now = LocalDateTime.now();
        SchedulerHistory history1 = SchedulerHistory.of(
            1L,
            query.schedulerId(),
            "status",
            "ACTIVE",
            "INACTIVE",
            now.minusDays(1)
        );
        SchedulerHistory history2 = SchedulerHistory.of(
            2L,
            query.schedulerId(),
            "cronExpression",
            "cron(0 0 * * ? *)",
            "cron(0 1 * * ? *)",
            now.minusDays(2)
        );
        // 최신 것이 먼저 오도록 정렬 (desc)
        List<SchedulerHistory> histories = List.of(history1, history2);

        PageResult<SchedulerHistory> pageResult = new PageResult<SchedulerHistory>() {
            @Override
            public List<SchedulerHistory> content() {
                return histories;
            }

            @Override
            public int page() {
                return query.page();
            }

            @Override
            public int size() {
                return query.size();
            }

            @Override
            public long totalElements() {
                return 2L;
            }
        };

        given(schedulerHistoryQueryPort.findBySchedulerId(
            SchedulerId.of(query.schedulerId()),
            query.page(),
            query.size()
        )).willReturn(pageResult);

        PageResult<SchedulerHistoryResponse> result = useCase.execute(query);

        assertThat(result.content()).hasSize(2);
        // 최신 것이 먼저 와야 함
        assertThat(result.content().get(0).occurredAt()).isAfter(result.content().get(1).occurredAt());
    }
}

