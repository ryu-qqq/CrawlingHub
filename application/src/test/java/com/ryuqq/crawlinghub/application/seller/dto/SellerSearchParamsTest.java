package com.ryuqq.crawlinghub.application.seller.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.common.dto.query.CommonSearchParams;
import com.ryuqq.crawlinghub.application.seller.dto.query.SellerSearchParams;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * SellerSearchParams 단위 테스트
 *
 * <p>Compact Constructor 기본값 적용 및 위임 메서드 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SellerSearchParams 테스트")
class SellerSearchParamsTest {

    @Nested
    @DisplayName("생성자 테스트")
    class Constructor {

        @Test
        @DisplayName("[성공] 유효한 값으로 생성")
        void shouldCreateWithValidValues() {
            CommonSearchParams searchParams =
                    new CommonSearchParams(false, null, null, "createdAt", "DESC", 0, 10);
            SellerSearchParams params =
                    new SellerSearchParams(
                            "mustIt-seller",
                            "seller-name",
                            List.of("ACTIVE"),
                            Instant.parse("2025-01-01T00:00:00Z"),
                            Instant.parse("2025-12-31T23:59:59Z"),
                            searchParams);

            assertThat(params.mustItSellerName()).isEqualTo("mustIt-seller");
            assertThat(params.sellerName()).isEqualTo("seller-name");
            assertThat(params.statuses()).containsExactly("ACTIVE");
            assertThat(params.searchParams()).isEqualTo(searchParams);
        }

        @Test
        @DisplayName("[성공] searchParams가 null이면 기본 값 적용")
        void shouldApplyDefaultSearchParamsWhenNull() {
            SellerSearchParams params = new SellerSearchParams(null, null, null, null, null, null);

            assertThat(params.searchParams()).isNotNull();
            assertThat(params.page()).isZero();
            assertThat(params.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("[성공] statuses가 null이면 null 유지")
        void shouldPreserveNullStatuses() {
            SellerSearchParams params = new SellerSearchParams(null, null, null, null, null, null);

            assertThat(params.statuses()).isNull();
        }

        @Test
        @DisplayName("[성공] statuses가 있으면 불변 목록으로 변환")
        void shouldConvertStatusesToUnmodifiableList() {
            List<String> statuses = List.of("ACTIVE", "INACTIVE");
            SellerSearchParams params =
                    new SellerSearchParams(null, null, statuses, null, null, null);

            assertThat(params.statuses()).containsExactly("ACTIVE", "INACTIVE");
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class Of {

        @Test
        @DisplayName("[성공] 팩토리 메서드로 생성")
        void shouldCreateViaFactory() {
            CommonSearchParams searchParams =
                    new CommonSearchParams(false, null, null, "createdAt", "ASC", 1, 20);
            SellerSearchParams params =
                    SellerSearchParams.of(
                            "mustIt", "seller", List.of("ACTIVE"), null, null, searchParams);

            assertThat(params.mustItSellerName()).isEqualTo("mustIt");
        }
    }

    @Nested
    @DisplayName("위임 메서드 테스트")
    class DelegateMethods {

        @Test
        @DisplayName("[성공] page()는 searchParams.page() 반환")
        void shouldDelegatePage() {
            CommonSearchParams searchParams =
                    new CommonSearchParams(false, null, null, null, null, 2, 10);
            SellerSearchParams params =
                    new SellerSearchParams(null, null, null, null, null, searchParams);

            assertThat(params.page()).isEqualTo(2);
        }

        @Test
        @DisplayName("[성공] size()는 searchParams.size() 반환")
        void shouldDelegateSize() {
            CommonSearchParams searchParams =
                    new CommonSearchParams(false, null, null, null, null, 0, 50);
            SellerSearchParams params =
                    new SellerSearchParams(null, null, null, null, null, searchParams);

            assertThat(params.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("[성공] sortKey()는 searchParams.sortKey() 반환")
        void shouldDelegateSortKey() {
            CommonSearchParams searchParams =
                    new CommonSearchParams(false, null, null, "sellerName", "ASC", 0, 10);
            SellerSearchParams params =
                    new SellerSearchParams(null, null, null, null, null, searchParams);

            assertThat(params.sortKey()).isEqualTo("sellerName");
        }

        @Test
        @DisplayName("[성공] sortDirection()은 searchParams.sortDirection() 반환")
        void shouldDelegateSortDirection() {
            CommonSearchParams searchParams =
                    new CommonSearchParams(false, null, null, "createdAt", "ASC", 0, 10);
            SellerSearchParams params =
                    new SellerSearchParams(null, null, null, null, null, searchParams);

            assertThat(params.sortDirection()).isEqualTo("ASC");
        }
    }
}
