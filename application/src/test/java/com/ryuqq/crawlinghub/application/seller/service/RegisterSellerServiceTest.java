package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.fixture.RegisterSellerCommandFixture;
import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;
import com.ryuqq.crawlinghub.application.seller.port.out.external.EventBridgePort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.fixture.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * RegisterSellerService 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ Part 1: 중복 Seller ID 검증 (findByCriteria 사용)</li>
 *   <li>⏳ Part 2: Seller 등록 성공 케이스</li>
 *   <li>⏳ Part 3: Transaction 경계 검증</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ TestFixture 사용 필수 (RegisterSellerCommandFixture)</li>
 *   <li>✅ Mock Port 사용 (Outbound Port)</li>
 *   <li>✅ findByCriteria로 중복 체크 (VO 기반)</li>
 *   <li>✅ @Transactional 내 외부 API 호출 금지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterSellerService 테스트")
class RegisterSellerServiceTest {

    @Mock
    private SellerQueryPort sellerQueryPort;

    @Mock
    private SellerPersistencePort sellerPersistencePort;

    @Mock
    private EventBridgePort eventBridgePort;

    private RegisterSellerService registerSellerService;

    @BeforeEach
    void setUp() {
        registerSellerService = new RegisterSellerService(
            sellerQueryPort,
            sellerPersistencePort,
            eventBridgePort
        );
    }

    /**
     * Part 1: 중복 Seller ID 검증 (findByCriteria 사용)
     */
    @Test
    @DisplayName("중복된 Seller ID가 존재하면 IllegalArgumentException을 던져야 한다")
    void shouldThrowExceptionWhenDuplicateSellerId() {
        // Given
        RegisterSellerCommand command = RegisterSellerCommandFixture.aRegisterSellerCommand();

        // 이미 존재하는 Seller (findByCriteria로 조회)
        SellerSearchCriteria criteria = SellerSearchCriteria.of(
            command.sellerId(),
            null,
            null,
            null,
            null
        );
        Seller existingSeller = SellerFixture.forNew();
        given(sellerQueryPort.findByCriteria(criteria))
            .willReturn(List.of(existingSeller));

        // When & Then
        assertThatThrownBy(() -> registerSellerService.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이미 존재하는 Seller ID입니다");
    }
}
