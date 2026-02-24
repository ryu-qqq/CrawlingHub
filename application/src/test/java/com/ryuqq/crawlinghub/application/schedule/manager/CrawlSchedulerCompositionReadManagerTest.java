package com.ryuqq.crawlinghub.application.schedule.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlSchedulerCompositionQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.exception.CrawlSchedulerNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerCompositionReadManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: CompositionQueryPort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerCompositionReadManager 테스트")
class CrawlSchedulerCompositionReadManagerTest {

    @Mock private CrawlSchedulerCompositionQueryPort compositionQueryPort;

    @InjectMocks private CrawlSchedulerCompositionReadManager manager;

    /** 테스트용 CrawlSchedulerDetailResult 생성 */
    private CrawlSchedulerDetailResult createDetailResult(Long schedulerId) {
        return new CrawlSchedulerDetailResult(
                new CrawlSchedulerDetailResult.SchedulerInfo(
                        schedulerId,
                        "test-scheduler",
                        "cron(0 0 * * ? *)",
                        "ACTIVE",
                        Instant.now(),
                        Instant.now()),
                new CrawlSchedulerDetailResult.SellerSummary(1L, "seller-name", "mustit-seller"),
                new CrawlSchedulerDetailResult.ExecutionInfo(Instant.now(), "SUCCESS"),
                new CrawlSchedulerDetailResult.SchedulerStatistics(100L, 90L, 10L, 0.9),
                List.of());
    }

    @Nested
    @DisplayName("getSchedulerDetail() 테스트")
    class GetSchedulerDetail {

        @Test
        @DisplayName("[성공] 스케줄러 ID로 상세 정보 조회 성공")
        void shouldReturnSchedulerDetailWhenFound() {
            // Given
            Long schedulerId = 1L;
            CrawlSchedulerDetailResult expectedResult = createDetailResult(schedulerId);
            given(compositionQueryPort.findSchedulerDetailById(schedulerId))
                    .willReturn(Optional.of(expectedResult));

            // When
            CrawlSchedulerDetailResult result = manager.getSchedulerDetail(schedulerId);

            // Then
            assertThat(result).isEqualTo(expectedResult);
            then(compositionQueryPort).should().findSchedulerDetailById(schedulerId);
        }

        @Test
        @DisplayName("[실패] 스케줄러 없으면 CrawlSchedulerNotFoundException 발생")
        void shouldThrowExceptionWhenSchedulerNotFound() {
            // Given
            Long schedulerId = 999L;
            given(compositionQueryPort.findSchedulerDetailById(schedulerId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> manager.getSchedulerDetail(schedulerId))
                    .isInstanceOf(CrawlSchedulerNotFoundException.class);
        }
    }
}
