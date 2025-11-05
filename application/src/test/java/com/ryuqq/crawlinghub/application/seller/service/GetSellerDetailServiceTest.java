package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQueryFixture;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.port.SellerStatsFixture;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerStatsPort;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerStatsPort.SellerStats;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerFixture;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.seller.SellerStatus;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * GetSellerDetailService 단위 테스트
 *
 * <p>셀러 상세 조회 UseCase의 비즈니스 로직을 검증합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetSellerDetailService 단위 테스트")
class GetSellerDetailServiceTest {

    @Mock
    private LoadSellerPort loadSellerPort;

    @Mock
    private LoadSellerStatsPort loadSellerStatsPort;

    @InjectMocks
    private GetSellerDetailService sut;

    @Nested
    @DisplayName("execute 메서드는")
    class Describe_execute {

        @Nested
        @DisplayName("유효한 셀러 ID가 주어지면")
        class Context_with_valid_seller_id {

            private GetSellerQuery query;
            private MustitSeller seller;
            private SellerStats stats;

            @BeforeEach
            void setUp() {
                // Given: 유효한 셀러 ID
                query = GetSellerQueryFixture.create();
                seller = MustitSellerFixture.createActive();
                stats = SellerStatsFixture.create();

                // Mock 설정
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(seller));
                given(loadSellerStatsPort.getSellerStats(any(MustitSellerId.class)))
                    .willReturn(stats);
            }

            @Test
            @DisplayName("셀러 상세 정보를 조회하여 반환한다")
            void it_returns_seller_detail() {
                // When: 셀러 상세 조회 실행
                SellerDetailResponse response = sut.execute(query);

                // Then: 셀러 조회가 수행됨
                then(loadSellerPort).should().findById(any(MustitSellerId.class));

                // And: 통계 조회가 수행됨
                then(loadSellerStatsPort).should().getSellerStats(any(MustitSellerId.class));

                // And: 셀러 기본 정보가 포함됨
                assertThat(response).isNotNull();
                assertThat(response.seller()).isNotNull();
                assertThat(response.seller().sellerId()).isEqualTo(seller.getIdValue());
                assertThat(response.seller().sellerCode()).isEqualTo(seller.getSellerCode());
                assertThat(response.seller().sellerName()).isEqualTo(seller.getSellerName());
                assertThat(response.seller().status()).isEqualTo(seller.getStatus());

                // And: 통계 정보가 포함됨
                assertThat(response.totalSchedules()).isEqualTo(stats.totalSchedules());
                assertThat(response.activeSchedules()).isEqualTo(stats.activeSchedules());
                assertThat(response.totalCrawlTasks()).isEqualTo(stats.totalCrawlTasks());
                assertThat(response.successfulTasks()).isEqualTo(stats.successfulTasks());
                assertThat(response.failedTasks()).isEqualTo(stats.failedTasks());
            }
        }

        @Nested
        @DisplayName("존재하지 않는 셀러 ID가 주어지면")
        class Context_with_non_existent_seller_id {

            private GetSellerQuery query;

            @BeforeEach
            void setUp() {
                // Given: 존재하지 않는 셀러 ID
                query = GetSellerQueryFixture.createWithId(999L);

                // And: 셀러를 찾을 수 없음
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("SellerNotFoundException을 발생시킨다")
            void it_throws_seller_not_found_exception() {
                // When & Then: 셀러 상세 조회 시도 시 예외 발생
                assertThatThrownBy(() -> sut.execute(query))
                    .isInstanceOf(SellerNotFoundException.class)
                    .hasMessageContaining(query.sellerId().toString());

                // And: 셀러 조회는 수행됨
                then(loadSellerPort).should().findById(any(MustitSellerId.class));

                // And: 통계 조회는 수행되지 않음
                then(loadSellerStatsPort).should(never()).getSellerStats(any(MustitSellerId.class));
            }
        }

        @Nested
        @DisplayName("다양한 상태의 셀러 조회 시")
        class Context_with_various_seller_statuses {

            @Test
            @DisplayName("ACTIVE 상태 셀러는 정상 조회된다")
            void active_seller_is_retrieved() {
                // Given
                GetSellerQuery query = GetSellerQueryFixture.create();
                MustitSeller activeSeller = MustitSellerFixture.createActive();
                SellerStats stats = SellerStatsFixture.create();

                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(activeSeller));
                given(loadSellerStatsPort.getSellerStats(any(MustitSellerId.class)))
                    .willReturn(stats);

                // When
                SellerDetailResponse response = sut.execute(query);

                // Then
                assertThat(response.seller().status()).isEqualTo(SellerStatus.ACTIVE);
            }

            @Test
            @DisplayName("PAUSED 상태 셀러는 정상 조회된다")
            void paused_seller_is_retrieved() {
                // Given
                GetSellerQuery query = GetSellerQueryFixture.create();
                MustitSeller pausedSeller = MustitSellerFixture.createPaused();
                SellerStats stats = SellerStatsFixture.create();

                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(pausedSeller));
                given(loadSellerStatsPort.getSellerStats(any(MustitSellerId.class)))
                    .willReturn(stats);

                // When
                SellerDetailResponse response = sut.execute(query);

                // Then
                assertThat(response.seller().status()).isEqualTo(SellerStatus.PAUSED);
            }

            @Test
            @DisplayName("DISABLED 상태 셀러는 정상 조회된다")
            void disabled_seller_is_retrieved() {
                // Given
                GetSellerQuery query = GetSellerQueryFixture.create();
                MustitSeller disabledSeller = MustitSellerFixture.createDisabled();
                SellerStats stats = SellerStatsFixture.create();

                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(disabledSeller));
                given(loadSellerStatsPort.getSellerStats(any(MustitSellerId.class)))
                    .willReturn(stats);

                // When
                SellerDetailResponse response = sut.execute(query);

                // Then
                assertThat(response.seller().status()).isEqualTo(SellerStatus.DISABLED);
            }
        }

        @Nested
        @DisplayName("다양한 통계를 가진 셀러 조회 시")
        class Context_with_various_statistics {

            @Test
            @DisplayName("높은 성공률의 셀러는 정상 조회된다")
            void high_success_rate_seller_is_retrieved() {
                // Given
                GetSellerQuery query = GetSellerQueryFixture.create();
                MustitSeller seller = MustitSellerFixture.createActive();
                SellerStats stats = SellerStatsFixture.createHighSuccessRate();

                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(seller));
                given(loadSellerStatsPort.getSellerStats(any(MustitSellerId.class)))
                    .willReturn(stats);

                // When
                SellerDetailResponse response = sut.execute(query);

                // Then
                assertThat(response.totalCrawlTasks()).isEqualTo(100);
                assertThat(response.successfulTasks()).isEqualTo(98);
                assertThat(response.failedTasks()).isEqualTo(2);
            }

            @Test
            @DisplayName("낮은 성공률의 셀러는 정상 조회된다")
            void low_success_rate_seller_is_retrieved() {
                // Given
                GetSellerQuery query = GetSellerQueryFixture.create();
                MustitSeller seller = MustitSellerFixture.createActive();
                SellerStats stats = SellerStatsFixture.createLowSuccessRate();

                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(seller));
                given(loadSellerStatsPort.getSellerStats(any(MustitSellerId.class)))
                    .willReturn(stats);

                // When
                SellerDetailResponse response = sut.execute(query);

                // Then
                assertThat(response.totalCrawlTasks()).isEqualTo(100);
                assertThat(response.successfulTasks()).isEqualTo(50);
                assertThat(response.failedTasks()).isEqualTo(50);
            }

            @Test
            @DisplayName("태스크가 없는 셀러는 정상 조회된다")
            void seller_with_no_tasks_is_retrieved() {
                // Given
                GetSellerQuery query = GetSellerQueryFixture.create();
                MustitSeller seller = MustitSellerFixture.createActive();
                SellerStats stats = SellerStatsFixture.createNoTasks();

                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(seller));
                given(loadSellerStatsPort.getSellerStats(any(MustitSellerId.class)))
                    .willReturn(stats);

                // When
                SellerDetailResponse response = sut.execute(query);

                // Then
                assertThat(response.totalSchedules()).isZero();
                assertThat(response.activeSchedules()).isZero();
                assertThat(response.totalCrawlTasks()).isZero();
                assertThat(response.successfulTasks()).isZero();
                assertThat(response.failedTasks()).isZero();
            }

            @Test
            @DisplayName("활성 스케줄이 없는 셀러는 정상 조회된다")
            void seller_with_no_active_schedules_is_retrieved() {
                // Given
                GetSellerQuery query = GetSellerQueryFixture.create();
                MustitSeller seller = MustitSellerFixture.createActive();
                SellerStats stats = SellerStatsFixture.createNoActiveSchedules();

                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(seller));
                given(loadSellerStatsPort.getSellerStats(any(MustitSellerId.class)))
                    .willReturn(stats);

                // When
                SellerDetailResponse response = sut.execute(query);

                // Then
                assertThat(response.totalSchedules()).isEqualTo(5);
                assertThat(response.activeSchedules()).isZero();
            }
        }

        @Nested
        @DisplayName("다양한 상품 수를 가진 셀러 조회 시")
        class Context_with_various_product_counts {

            @Test
            @DisplayName("상품이 많은 셀러는 정상 조회된다")
            void seller_with_many_products_is_retrieved() {
                // Given
                GetSellerQuery query = GetSellerQueryFixture.create();
                MustitSeller seller = MustitSellerFixture.createWithProductCount(10000);
                SellerStats stats = SellerStatsFixture.create();

                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(seller));
                given(loadSellerStatsPort.getSellerStats(any(MustitSellerId.class)))
                    .willReturn(stats);

                // When
                SellerDetailResponse response = sut.execute(query);

                // Then
                assertThat(response.seller().totalProductCount()).isEqualTo(10000);
            }

            @Test
            @DisplayName("상품이 없는 셀러는 정상 조회된다")
            void seller_with_no_products_is_retrieved() {
                // Given
                GetSellerQuery query = GetSellerQueryFixture.create();
                MustitSeller seller = MustitSellerFixture.createWithProductCount(0);
                SellerStats stats = SellerStatsFixture.create();

                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(seller));
                given(loadSellerStatsPort.getSellerStats(any(MustitSellerId.class)))
                    .willReturn(stats);

                // When
                SellerDetailResponse response = sut.execute(query);

                // Then
                assertThat(response.seller().totalProductCount()).isZero();
            }
        }

        @Nested
        @DisplayName("readOnly 트랜잭션 검증")
        class Context_readonly_transaction {

            @Test
            @DisplayName("조회 작업만 수행하고 저장은 하지 않는다")
            void it_only_reads_and_does_not_save() {
                // Given
                GetSellerQuery query = GetSellerQueryFixture.create();
                MustitSeller seller = MustitSellerFixture.createActive();
                SellerStats stats = SellerStatsFixture.create();

                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(seller));
                given(loadSellerStatsPort.getSellerStats(any(MustitSellerId.class)))
                    .willReturn(stats);

                // When
                SellerDetailResponse response = sut.execute(query);

                // Then
                assertThat(response).isNotNull();

                // And: 조회 작업만 수행됨
                then(loadSellerPort).should().findById(any(MustitSellerId.class));
                then(loadSellerStatsPort).should().getSellerStats(any(MustitSellerId.class));
            }
        }
    }
}
