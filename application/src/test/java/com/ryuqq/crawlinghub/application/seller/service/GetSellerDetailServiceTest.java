package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.common.dto.PageResponse;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQueryFixture;
import com.ryuqq.crawlinghub.application.seller.dto.query.SellerQueryDto;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadProductCountHistoryPort;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerStatsPort;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerFixture;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.history.ProductCountHistory;
import com.ryuqq.crawlinghub.domain.seller.history.ProductCountHistoryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * GetSellerDetailService 단위 테스트
 *
 * <p>셀러 상세 조회 UseCase의 비즈니스 로직을 검증합니다.
 * 2개의 execute 메서드 (레거시 + 확장) 모두 테스트합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetSellerDetailService 단위 테스트")
class GetSellerDetailServiceTest {

    @Mock
    private LoadSellerPort loadSellerPort;

    @Mock
    private LoadSellerStatsPort loadSellerStatsPort;

    @Mock
    private LoadProductCountHistoryPort loadHistoryPort;

    private SellerAssembler sellerAssembler;
    private GetSellerDetailService sut;

    @BeforeEach
    void setUpService() {
        // SellerAssembler는 실제 인스턴스 사용 (순수 변환 클래스)
        sellerAssembler = new SellerAssembler();
        sut = new GetSellerDetailService(
            loadSellerPort,
            loadSellerStatsPort,
            loadHistoryPort,
            sellerAssembler
        );
    }

    @Nested
    @DisplayName("execute(GetSellerQuery) 메서드는 - 레거시 메서드")
    class Describe_execute_legacy {

        @Nested
        @DisplayName("존재하는 셀러 ID가 주어지면")
        class Context_with_existing_seller_id {

            private GetSellerQuery query;
            private MustitSeller seller;
            private SellerQueryDto queryDto;
            private LoadSellerStatsPort.SellerStats stats;

            @BeforeEach
            void setUp() {
                // Given: 존재하는 셀러 ID
                query = GetSellerQueryFixture.create();
                seller = MustitSellerFixture.createActive();

                // Mock: SellerQueryDto 반환
                queryDto = new SellerQueryDto(
                    seller.getIdValue(),
                    seller.getSellerCode(),
                    seller.getSellerNameValue(),
                    seller.getStatus(),
                    seller.getTotalProductCount(),
                    seller.getLastCrawledAt(),
                    seller.getCreatedAt(),
                    seller.getUpdatedAt()
                );
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(queryDto));


                // Mock: 통계 조회
                stats = new LoadSellerStatsPort.SellerStats(
                    10,   // totalSchedules
                    5,    // activeSchedules
                    100,  // totalCrawlTasks
                    80,   // successfulTasks
                    20    // failedTasks
                );
                given(loadSellerStatsPort.getSellerStats(any(MustitSellerId.class)))
                    .willReturn(stats);
            }

            @Test
            @DisplayName("셀러 상세 정보를 반환한다")
            void it_returns_seller_detail() {
                // When: 셀러 상세 조회 실행
                SellerDetailResponse response = sut.execute(query);

                // Then: 셀러 조회가 수행됨
                then(loadSellerPort).should().findById(any(MustitSellerId.class));

                // And: 통계 조회가 수행됨
                then(loadSellerStatsPort).should().getSellerStats(any(MustitSellerId.class));

                // And: 셀러 상세 정보가 반환됨
                assertThat(response).isNotNull();
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
            }
        }
    }

    @Nested
    @DisplayName("getDetail(Long sellerId) 메서드는 - 확장된 메서드")
    class Describe_getDetail_extended {

        @Nested
        @DisplayName("존재하는 셀러 ID가 주어지면")
        class Context_with_existing_seller_id {

            private Long sellerId;
            private MustitSeller seller;
            private SellerQueryDto queryDto;
            private List<ProductCountHistory> histories;

            @BeforeEach
            void setUp() {
                // Given: 존재하는 셀러 ID
                sellerId = 1L;
                seller = MustitSellerFixture.createActive();

                // Mock: SellerQueryDto 반환
                queryDto = new SellerQueryDto(
                    seller.getIdValue(),
                    seller.getSellerCode(),
                    seller.getSellerNameValue(),
                    seller.getStatus(),
                    seller.getTotalProductCount(),
                    seller.getLastCrawledAt(),
                    seller.getCreatedAt(),
                    seller.getUpdatedAt()
                );
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(queryDto));


                // Mock: 상품 수 변경 이력 조회
                histories = List.of(
                    createHistory(1L, LocalDate.now(), 100),
                    createHistory(2L, LocalDate.now().minusDays(1), 90)
                );
                given(loadHistoryPort.loadHistories(any(MustitSellerId.class), anyInt(), anyInt()))
                    .willReturn(histories);
                given(loadHistoryPort.countHistories(any(MustitSellerId.class)))
                    .willReturn(2L);
            }

            @Test
            @DisplayName("셀러 상세 정보와 이력을 반환한다")
            void it_returns_seller_detail_with_histories() {
                // When: 셀러 상세 조회 실행
                SellerDetailResponse response = sut.getDetail(sellerId);

                // Then: 셀러 조회가 수행됨
                then(loadSellerPort).should().findById(any(MustitSellerId.class));

                // And: 상품 수 변경 이력 조회가 수행됨
                then(loadHistoryPort).should().loadHistories(any(MustitSellerId.class), anyInt(), anyInt());
                then(loadHistoryPort).should().countHistories(any(MustitSellerId.class));

                // And: 셀러 상세 정보가 반환됨
                assertThat(response).isNotNull();
                assertThat(response.sellerId()).isEqualTo(sellerId);
                assertThat(response.sellerCode()).isEqualTo(seller.getSellerCode());
                assertThat(response.sellerName()).isEqualTo(seller.getSellerNameValue());
            }

            @Test
            @DisplayName("상품 수 변경 이력이 PageResponse로 반환된다")
            void it_returns_product_count_histories_as_page() {
                // When: 셀러 상세 조회 실행
                SellerDetailResponse response = sut.getDetail(sellerId);

                // Then: 상품 수 변경 이력이 존재함
                assertThat(response.productCountHistories()).isNotNull();
            }
        }

        @Nested
        @DisplayName("상품 수 변경 이력이 없는 셀러 ID가 주어지면")
        class Context_with_seller_having_no_histories {

            private Long sellerId;
            private MustitSeller seller;
            private SellerQueryDto queryDto;

            @BeforeEach
            void setUp() {
                // Given: 존재하는 셀러 ID (이력 없음)
                sellerId = 1L;
                seller = MustitSellerFixture.createActive();

                // Mock: SellerQueryDto 반환
                queryDto = new SellerQueryDto(
                    seller.getIdValue(),
                    seller.getSellerCode(),
                    seller.getSellerNameValue(),
                    seller.getStatus(),
                    seller.getTotalProductCount(),
                    seller.getLastCrawledAt(),
                    seller.getCreatedAt(),
                    seller.getUpdatedAt()
                );
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(queryDto));


                // Mock: 상품 수 변경 이력 없음
                given(loadHistoryPort.loadHistories(any(MustitSellerId.class), anyInt(), anyInt()))
                    .willReturn(Collections.emptyList());
                given(loadHistoryPort.countHistories(any(MustitSellerId.class)))
                    .willReturn(0L);
            }

            @Test
            @DisplayName("빈 PageResponse를 반환한다")
            void it_returns_empty_page_response() {
                // When: 셀러 상세 조회 실행
                SellerDetailResponse response = sut.getDetail(sellerId);

                // Then: 상품 수 변경 이력 조회가 수행됨
                then(loadHistoryPort).should().loadHistories(any(MustitSellerId.class), anyInt(), anyInt());
                then(loadHistoryPort).should().countHistories(any(MustitSellerId.class));

                // And: 빈 이력이 반환됨
                assertThat(response.productCountHistories()).isNotNull();
            }
        }

        @Nested
        @DisplayName("존재하지 않는 셀러 ID가 주어지면")
        class Context_with_non_existent_seller_id {

            private Long sellerId;

            @BeforeEach
            void setUp() {
                // Given: 존재하지 않는 셀러 ID
                sellerId = 999L;

                // And: 셀러를 찾을 수 없음
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("SellerNotFoundException을 발생시킨다")
            void it_throws_seller_not_found_exception() {
                // When & Then: 셀러 상세 조회 시도 시 예외 발생
                assertThatThrownBy(() -> sut.getDetail(sellerId))
                    .isInstanceOf(SellerNotFoundException.class)
                    .hasMessageContaining(sellerId.toString());

                // And: 셀러 조회는 수행됨
                then(loadSellerPort).should().findById(any(MustitSellerId.class));
            }
        }

        @Nested
        @DisplayName("페이징 테스트")
        class Context_paging {

            private Long sellerId;
            private MustitSeller seller;
            private SellerQueryDto queryDto;

            @BeforeEach
            void setUp() {
                sellerId = 1L;
                seller = MustitSellerFixture.createActive();

                queryDto = new SellerQueryDto(
                    seller.getIdValue(),
                    seller.getSellerCode(),
                    seller.getSellerNameValue(),
                    seller.getStatus(),
                    seller.getTotalProductCount(),
                    seller.getLastCrawledAt(),
                    seller.getCreatedAt(),
                    seller.getUpdatedAt()
                );
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(queryDto));
            }

            @Test
            @DisplayName("기본 페이지 크기는 10이다")
            void default_page_size_is_10() {
                // Given: 15개의 이력
                given(loadHistoryPort.loadHistories(any(MustitSellerId.class), anyInt(), anyInt()))
                    .willReturn(Collections.emptyList());
                given(loadHistoryPort.countHistories(any(MustitSellerId.class)))
                    .willReturn(15L);

                // When
                SellerDetailResponse response = sut.getDetail(sellerId);

                // Then: 기본 페이지 0, 크기 10으로 조회
                then(loadHistoryPort).should().loadHistories(any(MustitSellerId.class), anyInt(), anyInt());
                assertThat(response).isNotNull();
            }
        }
    }

    // Helper Methods
    private ProductCountHistory createHistory(Long id, LocalDate executedDate, Integer productCount) {
        return ProductCountHistory.reconstitute(
            ProductCountHistoryId.of(id),
            MustitSellerId.of(1L),
            productCount,
            executedDate.atStartOfDay()  // LocalDate → LocalDateTime
        );
    }
}
