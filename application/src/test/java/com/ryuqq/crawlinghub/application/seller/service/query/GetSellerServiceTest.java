package com.ryuqq.crawlinghub.application.seller.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GetSellerService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetSellerService 테스트")
class GetSellerServiceTest {

    @Mock private SellerQueryPort sellerQueryPort;

    @Mock private SellerAssembler sellerAssembler;

    @InjectMocks private GetSellerService service;

    @Nested
    @DisplayName("execute() 셀러 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 존재하는 셀러 조회 시 SellerResponse 반환")
        void shouldReturnSellerResponseWhenSellerExists() {
            // Given
            Long sellerId = 1L;
            GetSellerQuery query = new GetSellerQuery(sellerId);
            Seller seller = SellerFixture.anActiveSeller();
            SellerResponse expectedResponse =
                    new SellerResponse(
                            sellerId,
                            "mustit-seller",
                            "seller-name",
                            true,
                            LocalDateTime.now(),
                            LocalDateTime.now());

            given(sellerQueryPort.findById(any(SellerId.class))).willReturn(Optional.of(seller));
            given(sellerAssembler.toResponse(seller)).willReturn(expectedResponse);

            // When
            SellerResponse result = service.execute(query);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(sellerQueryPort).should().findById(SellerId.of(sellerId));
            then(sellerAssembler).should().toResponse(seller);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 셀러 조회 시 SellerNotFoundException 발생")
        void shouldThrowExceptionWhenSellerNotFound() {
            // Given
            Long sellerId = 999L;
            GetSellerQuery query = new GetSellerQuery(sellerId);

            given(sellerQueryPort.findById(any(SellerId.class))).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(query))
                    .isInstanceOf(SellerNotFoundException.class);

            then(sellerQueryPort).should().findById(SellerId.of(sellerId));
            then(sellerAssembler).shouldHaveNoInteractions();
        }
    }
}
