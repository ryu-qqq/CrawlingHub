package com.ryuqq.crawlinghub.application.seller.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult;
import com.ryuqq.crawlinghub.application.seller.manager.SellerCompositionReadManager;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import java.time.Instant;
import java.util.Collections;
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
 * <p>Mockist 스타일 테스트: CompositionReadManager 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetSellerService 테스트")
class GetSellerServiceTest {

    @Mock private SellerCompositionReadManager compositionReadManager;

    @InjectMocks private GetSellerService service;

    @Nested
    @DisplayName("execute() 셀러 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 존재하는 셀러 조회 시 SellerDetailResult 반환")
        void shouldReturnSellerDetailResultWhenSellerExists() {
            // Given
            Long sellerId = 1L;
            Instant now = Instant.now();
            SellerDetailResult expectedResult =
                    new SellerDetailResult(
                            new SellerDetailResult.SellerInfo(
                                    sellerId,
                                    "mustit-seller",
                                    "seller-name",
                                    "ACTIVE",
                                    100,
                                    now,
                                    now),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            new SellerDetailResult.SellerStatistics(0L, 0L, 0L, 0.0));

            given(compositionReadManager.getSellerDetail(sellerId)).willReturn(expectedResult);

            // When
            SellerDetailResult result = service.execute(sellerId);

            // Then
            assertThat(result).isEqualTo(expectedResult);
            then(compositionReadManager).should().getSellerDetail(sellerId);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 셀러 조회 시 SellerNotFoundException 발생")
        void shouldThrowExceptionWhenSellerNotFound() {
            // Given
            Long sellerId = 999L;

            given(compositionReadManager.getSellerDetail(sellerId))
                    .willThrow(new SellerNotFoundException(sellerId));

            // When & Then
            assertThatThrownBy(() -> service.execute(sellerId))
                    .isInstanceOf(SellerNotFoundException.class);

            then(compositionReadManager).should().getSellerDetail(sellerId);
        }
    }
}
