package com.ryuqq.crawlinghub.application.seller.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerCompositionQueryPort;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
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
 * SellerCompositionReadManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: SellerCompositionQueryPort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerCompositionReadManager 테스트")
class SellerCompositionReadManagerTest {

    @Mock private SellerCompositionQueryPort compositionQueryPort;

    @InjectMocks private SellerCompositionReadManager manager;

    /** 테스트용 SellerDetailResult 생성 */
    private SellerDetailResult createDetailResult(Long sellerId) {
        return new SellerDetailResult(
                new SellerDetailResult.SellerInfo(
                        sellerId,
                        "mustit-seller",
                        "seller-name",
                        "ACTIVE",
                        100,
                        Instant.now(),
                        Instant.now()),
                List.of(
                        new SellerDetailResult.SchedulerSummary(
                                1L, "scheduler-1", "ACTIVE", "cron(0 0 * * ? *)")),
                List.of(
                        new SellerDetailResult.TaskSummary(
                                1L, "SUCCESS", "MINISHOP", Instant.now(), Instant.now())),
                new SellerDetailResult.SellerStatistics(500L, 480L, 20L, 0.96));
    }

    @Nested
    @DisplayName("getSellerDetail() 테스트")
    class GetSellerDetail {

        @Test
        @DisplayName("[성공] 셀러 ID로 상세 정보 조회 성공")
        void shouldReturnSellerDetailWhenFound() {
            // Given
            Long sellerId = 1L;
            SellerDetailResult expectedResult = createDetailResult(sellerId);
            given(compositionQueryPort.findSellerDetailById(sellerId))
                    .willReturn(Optional.of(expectedResult));

            // When
            SellerDetailResult result = manager.getSellerDetail(sellerId);

            // Then
            assertThat(result).isEqualTo(expectedResult);
            assertThat(result.seller().sellerId()).isEqualTo(sellerId);
            then(compositionQueryPort).should().findSellerDetailById(sellerId);
        }

        @Test
        @DisplayName("[성공] 상세 정보에 스케줄러 목록이 포함됨")
        void shouldReturnDetailWithSchedulerList() {
            // Given
            Long sellerId = 1L;
            SellerDetailResult expectedResult = createDetailResult(sellerId);
            given(compositionQueryPort.findSellerDetailById(sellerId))
                    .willReturn(Optional.of(expectedResult));

            // When
            SellerDetailResult result = manager.getSellerDetail(sellerId);

            // Then
            assertThat(result.schedulers()).hasSize(1);
            assertThat(result.recentTasks()).hasSize(1);
        }

        @Test
        @DisplayName("[실패] 셀러 없으면 SellerNotFoundException 발생")
        void shouldThrowExceptionWhenSellerNotFound() {
            // Given
            Long sellerId = 999L;
            given(compositionQueryPort.findSellerDetailById(sellerId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> manager.getSellerDetail(sellerId))
                    .isInstanceOf(SellerNotFoundException.class);
        }
    }
}
