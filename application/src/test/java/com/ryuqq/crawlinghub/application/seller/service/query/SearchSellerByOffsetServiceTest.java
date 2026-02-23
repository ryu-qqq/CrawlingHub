package com.ryuqq.crawlinghub.application.seller.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.common.dto.query.CommonSearchParams;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.query.SellerSearchParams;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerPageResult;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResult;
import com.ryuqq.crawlinghub.application.seller.factory.query.SellerQueryFactory;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.query.SellerQueryCriteria;
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
 * SearchSellerByOffsetService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: ReadManager, QueryFactory, Assembler 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchSellerByOffsetService 테스트")
class SearchSellerByOffsetServiceTest {

    @Mock private SellerReadManager readManager;

    @Mock private SellerQueryFactory queryFactory;

    @Mock private SellerAssembler assembler;

    @InjectMocks private SearchSellerByOffsetService service;

    @Nested
    @DisplayName("execute() 셀러 목록 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 조건에 맞는 셀러 목록 조회 시 SellerPageResult 반환")
        void shouldReturnPageResultWhenSellersExist() {
            // Given
            SellerSearchParams params =
                    SellerSearchParams.of(
                            "mustit",
                            "seller",
                            List.of("ACTIVE"),
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, 0, 10));

            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(null, null, null, null, null, 0, 10);
            List<Seller> sellers = List.of(SellerFixture.anActiveSeller());
            long totalElements = 1L;

            SellerPageResult expectedResult =
                    SellerPageResult.of(
                            List.of(
                                    new SellerResult(
                                            1L,
                                            "mustit-seller",
                                            "seller-name",
                                            "ACTIVE",
                                            sellers.get(0).getCreatedAt(),
                                            sellers.get(0).getUpdatedAt())),
                            PageMeta.of(0, 10, 1L));

            given(queryFactory.createCriteria(params)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(sellers);
            given(readManager.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResult(sellers, 0, 10, totalElements)).willReturn(expectedResult);

            // When
            SellerPageResult result = service.execute(params);

            // Then
            assertThat(result).isEqualTo(expectedResult);
            assertThat(result.results()).hasSize(1);
            then(queryFactory).should().createCriteria(params);
            then(readManager).should().findByCriteria(criteria);
            then(readManager).should().countByCriteria(criteria);
            then(assembler).should().toPageResult(sellers, 0, 10, totalElements);
        }

        @Test
        @DisplayName("[성공] 조건에 맞는 셀러 없을 시 빈 SellerPageResult 반환")
        void shouldReturnEmptyPageResultWhenNoSellersFound() {
            // Given
            SellerSearchParams params =
                    SellerSearchParams.of(
                            "nonexistent",
                            null,
                            null,
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, 0, 10));

            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(null, null, null, null, null, 0, 10);
            List<Seller> emptySellers = Collections.emptyList();
            long totalElements = 0L;

            SellerPageResult expectedResult = SellerPageResult.empty();

            given(queryFactory.createCriteria(params)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(emptySellers);
            given(readManager.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResult(emptySellers, 0, 10, totalElements))
                    .willReturn(expectedResult);

            // When
            SellerPageResult result = service.execute(params);

            // Then
            assertThat(result.results()).isEmpty();
            then(readManager).should().findByCriteria(criteria);
            then(readManager).should().countByCriteria(criteria);
        }

        @Test
        @DisplayName("[성공] 페이징 파라미터가 올바르게 전달됨")
        void shouldPassCorrectPagingParameters() {
            // Given
            int page = 2;
            int size = 20;
            SellerSearchParams params =
                    SellerSearchParams.of(
                            null,
                            null,
                            null,
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, page, size));

            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(null, null, null, null, null, page, size);
            List<Seller> sellers = List.of(SellerFixture.anActiveSeller());
            long totalElements = 50L;

            SellerPageResult expectedResult =
                    SellerPageResult.of(
                            List.of(
                                    new SellerResult(
                                            1L,
                                            "mustit-seller",
                                            "seller-name",
                                            "ACTIVE",
                                            sellers.get(0).getCreatedAt(),
                                            sellers.get(0).getUpdatedAt())),
                            PageMeta.of(page, size, totalElements));

            given(queryFactory.createCriteria(params)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(sellers);
            given(readManager.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResult(sellers, page, size, totalElements))
                    .willReturn(expectedResult);

            // When
            SellerPageResult result = service.execute(params);

            // Then
            assertThat(result.pageMeta().page()).isEqualTo(page);
            assertThat(result.pageMeta().size()).isEqualTo(size);
            then(assembler).should().toPageResult(sellers, page, size, totalElements);
        }
    }
}
