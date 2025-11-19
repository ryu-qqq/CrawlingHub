package com.ryuqq.crawlinghub.application.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.application.common.pagination.PageResult;
import com.ryuqq.crawlinghub.application.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.dto.query.ListSchedulersQuery;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerSummaryResponse;
import com.ryuqq.crawlinghub.application.scheduler.fixture.assembler.SchedulerAssemblerFixture;
import com.ryuqq.crawlinghub.application.scheduler.fixture.query.ListSchedulersQueryFixture;
import com.ryuqq.crawlinghub.application.scheduler.service.query.ListSchedulersService;
import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.CrawlingSchedulerFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("ListSchedulersUseCase")
class ListSchedulersUseCaseTest {

    private final SchedulerQueryPort schedulerQueryPort = Mockito.mock(SchedulerQueryPort.class);
    private final SchedulerAssembler schedulerAssembler = SchedulerAssemblerFixture.create();

    private ListSchedulersService useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListSchedulersService(
            schedulerQueryPort,
            schedulerAssembler
        );
    }

    @Test
    @DisplayName("should list schedulers with pagination")
    void shouldListSchedulersWithPagination() {
        ListSchedulersQuery query = ListSchedulersQueryFixture.create();
        CrawlingScheduler scheduler1 = CrawlingSchedulerFixture.aReconstitutedScheduler();
        CrawlingScheduler scheduler2 = CrawlingSchedulerFixture.aReconstitutedScheduler();
        List<CrawlingScheduler> schedulers = List.of(scheduler1, scheduler2);

        PageResult<CrawlingScheduler> pageResult = new PageResult<CrawlingScheduler>() {
            @Override
            public List<CrawlingScheduler> content() {
                return schedulers;
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

        given(schedulerQueryPort.findAllBySellerIdAndStatus(
            SellerId.of(query.sellerId()),
            query.status(),
            query.page(),
            query.size()
        )).willReturn(pageResult);

        PageResult<SchedulerSummaryResponse> result = useCase.execute(query);

        assertThat(result.content()).hasSize(2);
        assertThat(result.page()).isEqualTo(query.page());
        assertThat(result.size()).isEqualTo(query.size());
        assertThat(result.totalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("should filter by seller id and status")
    void shouldFilterBySellerIdAndStatus() {
        ListSchedulersQuery query = new ListSchedulersQuery(100L, SchedulerStatus.INACTIVE, 0, 10);
        CrawlingScheduler scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();

        PageResult<CrawlingScheduler> pageResult = new PageResult<CrawlingScheduler>() {
            @Override
            public List<CrawlingScheduler> content() {
                return List.of(scheduler);
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
                return 1L;
            }
        };

        given(schedulerQueryPort.findAllBySellerIdAndStatus(
            SellerId.of(query.sellerId()),
            query.status(),
            query.page(),
            query.size()
        )).willReturn(pageResult);

        PageResult<SchedulerSummaryResponse> result = useCase.execute(query);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).schedulerId()).isEqualTo(scheduler.getSchedulerId());
        assertThat(result.content().get(0).status()).isEqualTo(SchedulerStatus.INACTIVE);
    }
}

