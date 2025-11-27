package com.ryuqq.crawlinghub.application.task.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.dto.query.GetCrawlTaskQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskDetailResponse;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskNotFoundException;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GetCrawlTaskService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetCrawlTaskService 테스트")
class GetCrawlTaskServiceTest {

    @Mock private CrawlTaskQueryPort crawlTaskQueryPort;

    @Mock private CrawlTaskAssembler assembler;

    @InjectMocks private GetCrawlTaskService service;

    @Nested
    @DisplayName("execute() 크롤 태스크 단건 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 존재하는 태스크 조회 시 CrawlTaskDetailResponse 반환")
        void shouldReturnDetailResponseWhenTaskExists() {
            // Given
            Long crawlTaskId = 1L;
            GetCrawlTaskQuery query = new GetCrawlTaskQuery(crawlTaskId);
            CrawlTask crawlTask = CrawlTaskFixture.aWaitingTask();
            CrawlTaskDetailResponse expectedResponse =
                    new CrawlTaskDetailResponse(
                            crawlTaskId,
                            1L,
                            1L,
                            CrawlTaskStatus.WAITING,
                            CrawlTaskType.META,
                            0,
                            "https://example.com",
                            "/api/products",
                            Map.of("page", "1"),
                            "https://example.com/api/products?page=1",
                            LocalDateTime.now(),
                            LocalDateTime.now());

            given(crawlTaskQueryPort.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.of(crawlTask));
            given(assembler.toDetailResponse(crawlTask)).willReturn(expectedResponse);

            // When
            CrawlTaskDetailResponse result = service.execute(query);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(crawlTaskQueryPort).should().findById(CrawlTaskId.of(crawlTaskId));
            then(assembler).should().toDetailResponse(crawlTask);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 태스크 조회 시 CrawlTaskNotFoundException 발생")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            Long crawlTaskId = 999L;
            GetCrawlTaskQuery query = new GetCrawlTaskQuery(crawlTaskId);

            given(crawlTaskQueryPort.findById(any(CrawlTaskId.class))).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(query))
                    .isInstanceOf(CrawlTaskNotFoundException.class);

            then(assembler).should(never()).toDetailResponse(any());
        }

        @Test
        @DisplayName("[성공] 다양한 상태의 태스크 조회")
        void shouldReturnTaskWithVariousStatuses() {
            // Given
            Long crawlTaskId = 2L;
            GetCrawlTaskQuery query = new GetCrawlTaskQuery(crawlTaskId);
            CrawlTask runningTask = CrawlTaskFixture.aRunningTask();
            CrawlTaskDetailResponse expectedResponse =
                    new CrawlTaskDetailResponse(
                            crawlTaskId,
                            1L,
                            1L,
                            CrawlTaskStatus.RUNNING,
                            CrawlTaskType.META,
                            0,
                            "https://example.com",
                            "/api/products",
                            Map.of("page", "1"),
                            "https://example.com/api/products?page=1",
                            LocalDateTime.now(),
                            LocalDateTime.now());

            given(crawlTaskQueryPort.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.of(runningTask));
            given(assembler.toDetailResponse(runningTask)).willReturn(expectedResponse);

            // When
            CrawlTaskDetailResponse result = service.execute(query);

            // Then
            assertThat(result.status()).isEqualTo(CrawlTaskStatus.RUNNING);
        }
    }
}
