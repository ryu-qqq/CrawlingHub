package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.fixture.UpdateSellerIntervalCommandFixture;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerIntervalCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.out.command.SellerPersistencePort;
import com.ryuqq.crawlinghub.application.seller.port.out.external.EventBridgePort;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.fixture.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.CrawlingInterval;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerSearchCriteria;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * UpdateSellerIntervalService 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ Part 1: Seller 조회 (findById)</li>
 *   <li>⏳ Part 2: changeInterval() 호출 및 저장</li>
 *   <li>⏳ Part 3: EventBridge 업데이트 (트랜잭션 밖)</li>
 *   <li>⏳ Part 4: Seller 없을 경우 예외 처리</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ TestFixture 사용 필수 (UpdateSellerIntervalCommandFixture)</li>
 *   <li>✅ Mock Port 사용 (Outbound Port)</li>
 *   <li>✅ @Transactional 내 외부 API 호출 금지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSellerIntervalService 테스트")
class UpdateSellerIntervalServiceTest {

    @Mock
    private SellerQueryPort sellerQueryPort;

    @Mock
    private SellerPersistencePort sellerPersistencePort;

    @Mock
    private EventBridgePort eventBridgePort;

    @Mock
    private SellerAssembler sellerAssembler;

    private UpdateSellerIntervalService updateSellerIntervalService;

    @BeforeEach
    void setUp() {
        updateSellerIntervalService = new UpdateSellerIntervalService(
            sellerQueryPort,
            sellerPersistencePort,
            eventBridgePort,
            sellerAssembler
        );
    }

    /**
     * Part 1: Seller 조회 성공 케이스
     */
    @Test
    @DisplayName("유효한 Command로 Seller 주기를 업데이트하면 SellerResponse를 반환해야 한다")
    void shouldUpdateIntervalSuccessfully() {
        // Given
        UpdateSellerIntervalCommand command = UpdateSellerIntervalCommandFixture.anUpdateSellerIntervalCommand();

        // 기존 Seller (주기 1일)
        Seller existingSeller = SellerFixture.reconstitute(
            new SellerId(1L),
            "무신사",
            new CrawlingInterval(1),
            SellerStatus.ACTIVE,
            0
        );

        SellerSearchCriteria criteria = SellerSearchCriteria.of(
            command.sellerId(),
            null,
            null,
            null,
            null
        );
        given(sellerQueryPort.findByCriteria(criteria))
            .willReturn(List.of(existingSeller));

        // Seller 저장 성공
        SellerId savedSellerId = new SellerId(1L);
        given(sellerPersistencePort.persist(any(Seller.class)))
            .willReturn(savedSellerId);

        // SellerAssembler Mock 설정
        SellerResponse expectedResponse = new SellerResponse(
            command.sellerId(),
            existingSeller.getName(),
            SellerStatus.ACTIVE,
            command.newIntervalDays(),
            existingSeller.getTotalProductCount(),
            null,
            null
        );
        given(sellerAssembler.toResponse(any(Seller.class)))
            .willReturn(expectedResponse);

        // When
        SellerResponse response = updateSellerIntervalService.execute(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.sellerId()).isEqualTo(command.sellerId());
        assertThat(response.crawlingIntervalDays()).isEqualTo(command.newIntervalDays());
    }

    /**
     * Part 2: Seller 없을 경우 예외 처리
     */
    @Test
    @DisplayName("존재하지 않는 Seller ID로 업데이트 시도하면 IllegalArgumentException을 던져야 한다")
    void shouldThrowExceptionWhenSellerNotFound() {
        // Given
        UpdateSellerIntervalCommand command = UpdateSellerIntervalCommandFixture.anUpdateSellerIntervalCommand();

        // Seller 없음
        SellerSearchCriteria criteria = SellerSearchCriteria.of(
            command.sellerId(),
            null,
            null,
            null,
            null
        );
        given(sellerQueryPort.findByCriteria(criteria))
            .willReturn(List.of());

        // When & Then
        assertThatThrownBy(() -> updateSellerIntervalService.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("존재하지 않는 Seller ID입니다");
    }

    /**
     * Part 3: EventBridge 업데이트 (트랜잭션 밖)
     */
    @Test
    @DisplayName("EventBridge Rule 업데이트는 트랜잭션 커밋 후 실행되어야 한다")
    void shouldCallEventBridgeAfterTransactionCommit() {
        // Given
        UpdateSellerIntervalCommand command = UpdateSellerIntervalCommandFixture.anUpdateSellerIntervalCommand();

        // 기존 Seller
        Seller existingSeller = SellerFixture.reconstitute(
            new SellerId(1L),
            "무신사",
            new CrawlingInterval(1),
            SellerStatus.ACTIVE,
            0
        );

        SellerSearchCriteria criteria = SellerSearchCriteria.of(
            command.sellerId(),
            null,
            null,
            null,
            null
        );
        given(sellerQueryPort.findByCriteria(criteria))
            .willReturn(List.of(existingSeller));

        // Seller 저장 성공
        SellerId savedSellerId = new SellerId(1L);
        given(sellerPersistencePort.persist(any(Seller.class)))
            .willReturn(savedSellerId);

        // SellerAssembler Mock 설정
        SellerResponse expectedResponse = new SellerResponse(
            command.sellerId(),
            existingSeller.getName(),
            SellerStatus.ACTIVE,
            command.newIntervalDays(),
            existingSeller.getTotalProductCount(),
            null,
            null
        );
        given(sellerAssembler.toResponse(any(Seller.class)))
            .willReturn(expectedResponse);

        // When
        updateSellerIntervalService.execute(command);

        // Then
        // EventBridge Rule 업데이트가 호출되어야 함
        verify(eventBridgePort).updateRule(
            eq(command.sellerId()),
            eq(command.newIntervalDays())
        );
    }
}
