package com.ryuqq.crawlinghub.application.seller.factory.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.common.dto.query.CommonSearchParams;
import com.ryuqq.crawlinghub.application.seller.dto.query.SellerSearchParams;
import com.ryuqq.crawlinghub.domain.seller.query.SellerQueryCriteria;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("application")
@Tag("factory")
@DisplayName("SellerQueryFactory 단위 테스트")
class SellerQueryFactoryTest {

    private SellerQueryFactory factory;

    @BeforeEach
    void setUp() {
        factory = new SellerQueryFactory();
    }

    @Nested
    @DisplayName("createCriteria() 메서드는")
    class CreateCriteriaMethod {

        @Test
        @DisplayName("모든 필드가 있는 SearchParams를 Criteria로 변환한다")
        void shouldConvertParamsWithAllFields() {
            // Given
            Instant createdFrom = Instant.parse("2024-01-01T00:00:00Z");
            Instant createdTo = Instant.parse("2024-12-31T23:59:59Z");
            SellerSearchParams params =
                    SellerSearchParams.of(
                            "mustit-seller",
                            "seller-name",
                            List.of("ACTIVE"),
                            createdFrom,
                            createdTo,
                            CommonSearchParams.of(null, null, null, null, null, 1, 20));

            // When
            SellerQueryCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.mustItSellerName().value()).isEqualTo("mustit-seller");
            assertThat(criteria.sellerName().value()).isEqualTo("seller-name");
            assertThat(criteria.status()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(criteria.createdFrom()).isEqualTo(createdFrom);
            assertThat(criteria.createdTo()).isEqualTo(createdTo);
            assertThat(criteria.page()).isEqualTo(1);
            assertThat(criteria.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("mustItSellerName이 null이면 Criteria에도 null로 설정한다")
        void shouldSetNullMustItSellerNameWhenParamsHasNull() {
            // Given
            SellerSearchParams params =
                    SellerSearchParams.of(
                            null,
                            "seller-name",
                            null,
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, 1, 10));

            // When
            SellerQueryCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.mustItSellerName()).isNull();
            assertThat(criteria.sellerName().value()).isEqualTo("seller-name");
        }

        @Test
        @DisplayName("sellerName이 null이면 Criteria에도 null로 설정한다")
        void shouldSetNullSellerNameWhenParamsHasNull() {
            // Given
            SellerSearchParams params =
                    SellerSearchParams.of(
                            "mustit-seller",
                            null,
                            null,
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, 1, 10));

            // When
            SellerQueryCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.mustItSellerName().value()).isEqualTo("mustit-seller");
            assertThat(criteria.sellerName()).isNull();
        }

        @Test
        @DisplayName("모든 검색 조건이 null이어도 페이지네이션은 유지한다")
        void shouldKeepPaginationWhenAllSearchConditionsAreNull() {
            // Given
            SellerSearchParams params =
                    SellerSearchParams.of(
                            null,
                            null,
                            null,
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, 5, 50));

            // When
            SellerQueryCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.mustItSellerName()).isNull();
            assertThat(criteria.sellerName()).isNull();
            assertThat(criteria.status()).isNull();
            assertThat(criteria.createdFrom()).isNull();
            assertThat(criteria.createdTo()).isNull();
            assertThat(criteria.page()).isEqualTo(5);
            assertThat(criteria.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("createdFrom만 있는 경우 Criteria에 반영한다")
        void shouldConvertParamsWithOnlyCreatedFrom() {
            // Given
            Instant createdFrom = Instant.parse("2024-06-01T00:00:00Z");
            SellerSearchParams params =
                    SellerSearchParams.of(
                            null,
                            null,
                            null,
                            createdFrom,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, 0, 10));

            // When
            SellerQueryCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.createdFrom()).isEqualTo(createdFrom);
            assertThat(criteria.createdTo()).isNull();
        }

        @Test
        @DisplayName("createdTo만 있는 경우 Criteria에 반영한다")
        void shouldConvertParamsWithOnlyCreatedTo() {
            // Given
            Instant createdTo = Instant.parse("2024-12-31T23:59:59Z");
            SellerSearchParams params =
                    SellerSearchParams.of(
                            null,
                            null,
                            null,
                            null,
                            createdTo,
                            CommonSearchParams.of(null, null, null, null, null, 0, 10));

            // When
            SellerQueryCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.createdFrom()).isNull();
            assertThat(criteria.createdTo()).isEqualTo(createdTo);
        }

        @Test
        @DisplayName("statuses 문자열 목록을 SellerStatus Enum으로 변환한다")
        void shouldParseStatusStringsToEnums() {
            // Given
            SellerSearchParams params =
                    SellerSearchParams.of(
                            null,
                            null,
                            List.of("ACTIVE", "INACTIVE"),
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, 0, 10));

            // When
            SellerQueryCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.statuses())
                    .containsExactly(SellerStatus.ACTIVE, SellerStatus.INACTIVE);
        }

        @Test
        @DisplayName("빈 statuses 목록은 null로 변환한다")
        void shouldReturnNullForEmptyStatuses() {
            // Given
            SellerSearchParams params =
                    SellerSearchParams.of(
                            null,
                            null,
                            List.of(),
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, 0, 10));

            // When
            SellerQueryCriteria criteria = factory.createCriteria(params);

            // Then
            assertThat(criteria.statuses()).isNull();
        }
    }
}
