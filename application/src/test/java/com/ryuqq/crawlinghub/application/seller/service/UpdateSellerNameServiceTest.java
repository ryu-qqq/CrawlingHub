package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.fixture.UpdateSellerNameCommandFixture;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerNameCommand;
import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.fixture.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * UpdateSellerNameService 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ Seller 이름 변경 성공 케이스</li>
 *   <li>✅ Seller를 찾을 수 없는 경우 예외 처리</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ TestFixture 사용 필수 (UpdateSellerNameCommandFixture)</li>
 *   <li>✅ Mock Port 사용 (Outbound Port)</li>
 *   <li>✅ @Transactional 내 외부 API 호출 금지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSellerNameService 테스트")
class UpdateSellerNameServiceTest {

    @Mock
    private SellerQueryPort sellerQueryPort;

    @Mock
    private SellerPersistencePort sellerPersistencePort;

    private UpdateSellerNameService updateSellerNameService;

    @BeforeEach
    void setUp() {
        updateSellerNameService = new UpdateSellerNameService(
            sellerQueryPort,
            sellerPersistencePort
        );
    }

    @Test
    @DisplayName("유효한 Command로 Seller 이름을 변경하면 저장되어야 한다")
    void shouldUpdateSellerNameSuccessfully() {
        // Given
        UpdateSellerNameCommand command = UpdateSellerNameCommandFixture.anUpdateSellerNameCommand();
        SellerId sellerId = new SellerId(command.sellerId());
        Seller seller = SellerFixture.forNew();

        given(sellerQueryPort.findById(sellerId))
            .willReturn(Optional.of(seller));

        given(sellerPersistencePort.persist(any(Seller.class)))
            .willReturn(sellerId);

        // When
        updateSellerNameService.execute(command);

        // Then
        verify(sellerQueryPort).findById(sellerId);
        verify(sellerPersistencePort).persist(any(Seller.class));
    }

    @Test
    @DisplayName("Seller를 찾을 수 없으면 예외를 던져야 한다")
    void shouldThrowExceptionWhenSellerNotFound() {
        // Given
        UpdateSellerNameCommand command = UpdateSellerNameCommandFixture.anUpdateSellerNameCommand();
        SellerId sellerId = new SellerId(command.sellerId());

        given(sellerQueryPort.findById(sellerId))
            .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> updateSellerNameService.execute(command))
            .isInstanceOf(com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException.class);
    }
}

