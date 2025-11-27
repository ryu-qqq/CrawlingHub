package com.ryuqq.crawlinghub.application.execution.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.crawlinghub.application.execution.assembler.CrawlExecutionAssembler;
import com.ryuqq.crawlinghub.application.execution.dto.query.GetCrawlExecutionQuery;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionDetailResponse;
import com.ryuqq.crawlinghub.application.execution.port.out.query.CrawlExecutionQueryPort;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.exception.CrawlExecutionNotFoundException;
import com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GetCrawlExecutionService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetCrawlExecutionService 테스트")
class GetCrawlExecutionServiceTest {

    @Mock
    private CrawlExecutionQueryPort crawlExecutionQueryPort;

    @Mock
    private CrawlExecutionAssembler assembler;

    @InjectMocks
    private GetCrawlExecutionService service;

    @Nested
    @DisplayName("execute() 크롤 실행 단건 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 존재하는 실행 조회 시 CrawlExecutionDetailResponse 반환")
        void shouldReturnDetailResponseWhenExecutionExists() {
            // Given
            Long crawlExecutionId = 1L;
            GetCrawlExecutionQuery query = new GetCrawlExecutionQuery(crawlExecutionId);
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            CrawlExecutionDetailResponse expectedResponse = new CrawlExecutionDetailResponse(
                    crawlExecutionId, 1L, 1L, 1L, CrawlExecutionStatus.RUNNING,
                    null, null, null, null,
                    LocalDateTime.now(), null);

            given(crawlExecutionQueryPort.findById(any(CrawlExecutionId.class)))
                    .willReturn(Optional.of(execution));
            given(assembler.toDetailResponse(execution))
                    .willReturn(expectedResponse);

            // When
            CrawlExecutionDetailResponse result = service.execute(query);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(crawlExecutionQueryPort).should().findById(CrawlExecutionId.of(crawlExecutionId));
            then(assembler).should().toDetailResponse(execution);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 실행 조회 시 CrawlExecutionNotFoundException 발생")
        void shouldThrowExceptionWhenExecutionNotFound() {
            // Given
            Long crawlExecutionId = 999L;
            GetCrawlExecutionQuery query = new GetCrawlExecutionQuery(crawlExecutionId);

            given(crawlExecutionQueryPort.findById(any(CrawlExecutionId.class)))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(query))
                    .isInstanceOf(CrawlExecutionNotFoundException.class);

            then(assembler).should(never()).toDetailResponse(any());
        }

        @Test
        @DisplayName("[성공] 성공 상태의 실행 조회 시 결과 데이터 포함")
        void shouldReturnSuccessExecutionWithResult() {
            // Given
            Long crawlExecutionId = 2L;
            GetCrawlExecutionQuery query = new GetCrawlExecutionQuery(crawlExecutionId);
            CrawlExecution successExecution = CrawlExecutionFixture.aSuccessExecution();
            CrawlExecutionDetailResponse expectedResponse = new CrawlExecutionDetailResponse(
                    crawlExecutionId, 1L, 1L, 1L, CrawlExecutionStatus.SUCCESS,
                    200, "{\"products\": []}", null, 1500L,
                    LocalDateTime.now(), LocalDateTime.now());

            given(crawlExecutionQueryPort.findById(any(CrawlExecutionId.class)))
                    .willReturn(Optional.of(successExecution));
            given(assembler.toDetailResponse(successExecution))
                    .willReturn(expectedResponse);

            // When
            CrawlExecutionDetailResponse result = service.execute(query);

            // Then
            assertThat(result.status()).isEqualTo(CrawlExecutionStatus.SUCCESS);
            assertThat(result.httpStatusCode()).isEqualTo(200);
            assertThat(result.responseBody()).isNotNull();
        }

        @Test
        @DisplayName("[성공] 실패 상태의 실행 조회 시 에러 메시지 포함")
        void shouldReturnFailedExecutionWithError() {
            // Given
            Long crawlExecutionId = 3L;
            GetCrawlExecutionQuery query = new GetCrawlExecutionQuery(crawlExecutionId);
            CrawlExecution failedExecution = CrawlExecutionFixture.aFailedExecution();
            CrawlExecutionDetailResponse expectedResponse = new CrawlExecutionDetailResponse(
                    crawlExecutionId, 1L, 1L, 1L, CrawlExecutionStatus.FAILED,
                    500, null, "Internal Server Error", 500L,
                    LocalDateTime.now(), LocalDateTime.now());

            given(crawlExecutionQueryPort.findById(any(CrawlExecutionId.class)))
                    .willReturn(Optional.of(failedExecution));
            given(assembler.toDetailResponse(failedExecution))
                    .willReturn(expectedResponse);

            // When
            CrawlExecutionDetailResponse result = service.execute(query);

            // Then
            assertThat(result.status()).isEqualTo(CrawlExecutionStatus.FAILED);
            assertThat(result.errorMessage()).isNotNull();
        }
    }
}
