package com.ryuqq.crawlinghub.domain.seller.exception;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ryuqq.crawlinghub.domain.fixture.seller.SellerExceptionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Seller Domain Exception 테스트")
class DuplicateMustItSellerIdExceptionTest {

    @Nested
    @DisplayName("DuplicateMustItSellerIdException")
    class DuplicateMustItSellerIdExceptionSpec {

        @Test
        @DisplayName("shouldCreateExceptionWithMessage")
        void shouldCreateExceptionWithMessage() {
            DuplicateMustItSellerIdException exception = SellerExceptionFixture.duplicateMustItSellerIdException(1001L);

            assertAll(
                () -> assertEquals("SELLER-001", exception.code()),
                () -> assertEquals("이미 등록된 머스트잇 셀러 ID입니다.", exception.message()),
                () -> assertEquals(1001L, exception.args().get("mustItSellerId"))
            );
        }
    }

    @Nested
    @DisplayName("DuplicateSellerNameException")
    class DuplicateSellerNameExceptionSpec {

        @Test
        @DisplayName("shouldCreateExceptionWithMessage")
        void shouldCreateExceptionWithMessage() {
            DuplicateSellerNameException exception = SellerExceptionFixture.duplicateSellerNameException("머스트잇");

            assertAll(
                () -> assertEquals("SELLER-002", exception.code()),
                () -> assertEquals("이미 등록된 셀러 이름입니다.", exception.message()),
                () -> assertEquals("머스트잇", exception.args().get("sellerName"))
            );
        }
    }

    @Nested
    @DisplayName("SellerHasActiveSchedulersException")
    class SellerHasActiveSchedulersExceptionSpec {

        @Test
        @DisplayName("shouldCreateExceptionWithMessage")
        void shouldCreateExceptionWithMessage() {
            SellerHasActiveSchedulersException exception = SellerExceptionFixture.sellerHasActiveSchedulersException(55L, 2);

            assertAll(
                () -> assertEquals("SELLER-003", exception.code()),
                () -> assertEquals("활성 상태의 스케줄러가 존재하여 셀러를 비활성화할 수 없습니다.", exception.message()),
                () -> assertEquals(55L, exception.args().get("sellerId")),
                () -> assertEquals(2, exception.args().get("activeSchedulerCount"))
            );
        }
    }

    @Nested
    @DisplayName("SellerNotFoundException")
    class SellerNotFoundExceptionSpec {

        @Test
        @DisplayName("shouldCreateExceptionWithMessage")
        void shouldCreateExceptionWithMessage() {
            SellerNotFoundException exception = SellerExceptionFixture.sellerNotFoundException(77L);

            assertAll(
                () -> assertEquals("SELLER-004", exception.code()),
                () -> assertEquals("존재하지 않는 셀러입니다.", exception.message()),
                () -> assertEquals(77L, exception.args().get("sellerId"))
            );
        }
    }
}

