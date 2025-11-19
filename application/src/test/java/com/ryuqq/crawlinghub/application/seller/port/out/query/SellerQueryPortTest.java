package com.ryuqq.crawlinghub.application.seller.port.out.query;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SellerQueryPort 명세")
class SellerQueryPortTest {

    @Test
    @DisplayName("SellerQueryPort는 인터페이스여야 한다")
    void shouldBeInterface() {
        assertThat(SellerQueryPort.class.isInterface())
            .as("SellerQueryPort must be declared as interface")
            .isTrue();
    }

    @Nested
    @DisplayName("필수 조회 메서드 시그니처")
    class MethodSignatureTest {

        @Test
        @DisplayName("sellerId를 통한 단건 조회를 지원해야 한다")
        void shouldFindBySellerId() throws NoSuchMethodException {
            Method method = SellerQueryPort.class.getMethod("findById", SellerId.class);

            assertThat(method.getReturnType())
                .as("findById must return Optional<Seller>")
                .isEqualTo(Optional.class);
        }

        @Test
        @DisplayName("sellerId 존재 여부 확인을 지원해야 한다")
        void shouldCheckExistsBySellerId() throws NoSuchMethodException {
            Method method = SellerQueryPort.class.getMethod("existsById", SellerId.class);

            assertThat(method.getReturnType())
                .as("existsById must return primitive boolean")
                .isEqualTo(Boolean.TYPE);
        }

        @Test
        @DisplayName("검색 조건 기준 리스트 조회를 지원해야 한다")
        void shouldFindByCriteria() throws NoSuchMethodException {
            Method method = SellerQueryPort.class.getMethod("findByCriteria", SellerQueryCriteria.class);

            assertThat(method.getReturnType())
                .as("findByCriteria must return a list of sellers")
                .isEqualTo(List.class);
        }

        @Test
        @DisplayName("검색 조건 기준 카운트 조회를 지원해야 한다")
        void shouldCountByCriteria() throws NoSuchMethodException {
            Method method = SellerQueryPort.class.getMethod("countByCriteria", SellerQueryCriteria.class);

            assertThat(method.getReturnType())
                .as("countByCriteria must return long primitive")
                .isEqualTo(Long.TYPE);
        }
    }
}

