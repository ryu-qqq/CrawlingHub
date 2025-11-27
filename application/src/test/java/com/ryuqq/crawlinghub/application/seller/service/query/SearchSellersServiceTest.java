package com.ryuqq.crawlinghub.application.seller.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerQueryCriteria;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SearchSellersService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchSellersService 테스트")
class SearchSellersServiceTest {

    @Mock
    private SellerQueryPort sellerQueryPort;

    @Mock
    private SellerAssembler assembler;

    @InjectMocks
    private SearchSellersService service;

    @Nested
    @DisplayName("execute() 셀러 목록 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 조건에 맞는 셀러 목록 조회 시 PageResponse 반환")
        void shouldReturnPageResponseWhenSellersExist() {
            // Given
            SearchSellersQuery query = new SearchSellersQuery(
                    "mustit", "seller", SellerStatus.ACTIVE, 0, 10);
            SellerQueryCriteria criteria = new SellerQueryCriteria(null, null, null, 0, 10);
            List<Seller> sellers = List.of(SellerFixture.anActiveSeller());
            long totalElements = 1L;

            SellerSummaryResponse summaryResponse = new SellerSummaryResponse(
                    1L, "mustit-seller", "seller-name", true, LocalDateTime.now());
            PageResponse<SellerSummaryResponse> expectedResponse = PageResponse.of(
                    List.of(summaryResponse), 0, 10, 1L, 1, true, true);

            given(assembler.toCriteria(query)).willReturn(criteria);
            given(sellerQueryPort.findByCriteria(criteria)).willReturn(sellers);
            given(sellerQueryPort.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<SellerSummaryResponse> result = service.execute(query);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.content()).hasSize(1);
            then(assembler).should().toCriteria(query);
            then(sellerQueryPort).should().findByCriteria(criteria);
            then(sellerQueryPort).should().countByCriteria(criteria);
            then(assembler).should().toPageResponse(sellers, 0, 10, totalElements);
        }

        @Test
        @DisplayName("[성공] 조건에 맞는 셀러 없을 시 빈 PageResponse 반환")
        void shouldReturnEmptyPageResponseWhenNoSellersFound() {
            // Given
            SearchSellersQuery query = new SearchSellersQuery(
                    "nonexistent", null, null, 0, 10);
            SellerQueryCriteria criteria = new SellerQueryCriteria(null, null, null, 0, 10);
            List<Seller> emptySellers = Collections.emptyList();
            long totalElements = 0L;

            PageResponse<SellerSummaryResponse> expectedResponse = PageResponse.of(
                    Collections.emptyList(), 0, 10, 0L, 0, true, true);

            given(assembler.toCriteria(query)).willReturn(criteria);
            given(sellerQueryPort.findByCriteria(criteria)).willReturn(emptySellers);
            given(sellerQueryPort.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<SellerSummaryResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            then(sellerQueryPort).should().findByCriteria(criteria);
            then(sellerQueryPort).should().countByCriteria(criteria);
        }

        @Test
        @DisplayName("[성공] 페이징 파라미터가 올바르게 전달됨")
        void shouldPassCorrectPagingParameters() {
            // Given
            int page = 2;
            int size = 20;
            SearchSellersQuery query = new SearchSellersQuery(null, null, null, page, size);
            SellerQueryCriteria criteria = new SellerQueryCriteria(null, null, null, page, size);
            List<Seller> sellers = List.of(SellerFixture.anActiveSeller());
            long totalElements = 50L;

            PageResponse<SellerSummaryResponse> expectedResponse = PageResponse.of(
                    Collections.emptyList(), page, size, totalElements, 3, false, false);

            given(assembler.toCriteria(query)).willReturn(criteria);
            given(sellerQueryPort.findByCriteria(criteria)).willReturn(sellers);
            given(sellerQueryPort.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<SellerSummaryResponse> result = service.execute(query);

            // Then
            assertThat(result.page()).isEqualTo(page);
            assertThat(result.size()).isEqualTo(size);
            then(assembler).should().toPageResponse(sellers, page, size, totalElements);
        }
    }
}
