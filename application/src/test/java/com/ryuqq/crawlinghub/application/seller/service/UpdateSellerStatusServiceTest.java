package com.ryuqq.crawlinghub.application.seller.service;

import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerStatusCommand;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerStatusCommandFixture;
import com.ryuqq.crawlinghub.application.seller.dto.query.SellerQueryDto;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.application.seller.port.out.SaveSellerPort;
import com.ryuqq.crawlinghub.domain.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerFixture;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.seller.SellerStatus;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * UpdateSellerStatusService 단위 테스트
 *
 * <p>셀러 상태 변경 UseCase의 비즈니스 로직을 검증합니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSellerStatusService 단위 테스트")
class UpdateSellerStatusServiceTest {

    @Mock
    private LoadSellerPort loadSellerPort;

    @Mock
    private SaveSellerPort saveSellerPort;

    @Mock
    private SellerAssembler sellerAssembler;

    @InjectMocks
    private UpdateSellerStatusService sut;

    @Nested
    @DisplayName("execute 메서드는")
    class Describe_execute {

        @Nested
        @DisplayName("셀러를 ACTIVE 상태로 변경할 때")
        class Context_activate_seller {

            private UpdateSellerStatusCommand command;
            private MustitSeller seller;
            private MustitSeller activatedSeller;

            @BeforeEach
            void setUp() {
                // Given: PAUSED 상태의 셀러
                command = UpdateSellerStatusCommandFixture.createActive();
                seller = MustitSellerFixture.createPaused();

                // And: 활성화된 셀러
                activatedSeller = MustitSellerFixture.createActive();

                // Mock 설정: LoadSellerPort는 SellerQueryDto 반환
                SellerQueryDto queryDto = new SellerQueryDto(
                    seller.getIdValue(),
                    seller.getSellerCode(),
                    seller.getSellerName(),
                    seller.getStatus(),
                    seller.getTotalProductCount(),
                    seller.getLastCrawledAt(),
                    seller.getCreatedAt(),
                    seller.getUpdatedAt()
                );

                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(queryDto));
                given(sellerAssembler.toDomain(any(SellerQueryDto.class)))
                    .willReturn(seller);
                given(saveSellerPort.save(any(MustitSeller.class)))
                    .willReturn(activatedSeller);
            }

            @Test
            @DisplayName("셀러를 활성화하고 응답을 반환한다")
            void it_activates_seller_and_returns_response() {
                // When: 셀러 활성화 실행
                SellerResponse response = sut.execute(command);

                // Then: 셀러 조회가 수행됨
                then(loadSellerPort).should().findById(any(MustitSellerId.class));

                // And: 셀러가 저장됨
                then(saveSellerPort).should().save(any(MustitSeller.class));

                // And: 활성화된 셀러 정보가 응답으로 반환됨
                assertThat(response).isNotNull();
                assertThat(response.status()).isEqualTo(SellerStatus.ACTIVE);
            }
        }

        @Nested
        @DisplayName("셀러를 PAUSED 상태로 변경할 때")
        class Context_pause_seller {

            private UpdateSellerStatusCommand command;
            private MustitSeller seller;
            private MustitSeller pausedSeller;

            @BeforeEach
            void setUp() {
                // Given: ACTIVE 상태의 셀러
                command = UpdateSellerStatusCommandFixture.createPaused();
                seller = MustitSellerFixture.createActive();

                // And: 일시중지된 셀러
                pausedSeller = MustitSellerFixture.createPaused();

                // Mock 설정: LoadSellerPort는 SellerQueryDto 반환
                SellerQueryDto queryDto = new SellerQueryDto(
                    seller.getIdValue(),
                    seller.getSellerCode(),
                    seller.getSellerName(),
                    seller.getStatus(),
                    seller.getTotalProductCount(),
                    seller.getLastCrawledAt(),
                    seller.getCreatedAt(),
                    seller.getUpdatedAt()
                );
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(queryDto));
                given(sellerAssembler.toDomain(any(SellerQueryDto.class)))
                    .willReturn(seller);
                given(saveSellerPort.save(any(MustitSeller.class)))
                    .willReturn(pausedSeller);
            }

            @Test
            @DisplayName("셀러를 일시중지하고 응답을 반환한다")
            void it_pauses_seller_and_returns_response() {
                // When: 셀러 일시중지 실행
                SellerResponse response = sut.execute(command);

                // Then: 셀러 조회가 수행됨
                then(loadSellerPort).should().findById(any(MustitSellerId.class));

                // And: 셀러가 저장됨
                then(saveSellerPort).should().save(any(MustitSeller.class));

                // And: 일시중지된 셀러 정보가 응답으로 반환됨
                assertThat(response).isNotNull();
                assertThat(response.status()).isEqualTo(SellerStatus.PAUSED);
            }
        }

        @Nested
        @DisplayName("셀러를 DISABLED 상태로 변경할 때")
        class Context_disable_seller {

            private UpdateSellerStatusCommand command;
            private MustitSeller seller;
            private MustitSeller disabledSeller;

            @BeforeEach
            void setUp() {
                // Given: ACTIVE 상태의 셀러
                command = UpdateSellerStatusCommandFixture.createDisabled();
                seller = MustitSellerFixture.createActive();

                // And: 비활성화된 셀러
                disabledSeller = MustitSellerFixture.createDisabled();

                // Mock 설정: LoadSellerPort는 SellerQueryDto 반환
                SellerQueryDto queryDto = new SellerQueryDto(
                    seller.getIdValue(),
                    seller.getSellerCode(),
                    seller.getSellerName(),
                    seller.getStatus(),
                    seller.getTotalProductCount(),
                    seller.getLastCrawledAt(),
                    seller.getCreatedAt(),
                    seller.getUpdatedAt()
                );
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(queryDto));
                given(sellerAssembler.toDomain(any(SellerQueryDto.class)))
                    .willReturn(seller);
                given(saveSellerPort.save(any(MustitSeller.class)))
                    .willReturn(disabledSeller);
            }

            @Test
            @DisplayName("셀러를 비활성화하고 응답을 반환한다")
            void it_disables_seller_and_returns_response() {
                // When: 셀러 비활성화 실행
                SellerResponse response = sut.execute(command);

                // Then: 셀러 조회가 수행됨
                then(loadSellerPort).should().findById(any(MustitSellerId.class));

                // And: 셀러가 저장됨
                then(saveSellerPort).should().save(any(MustitSeller.class));

                // And: 비활성화된 셀러 정보가 응답으로 반환됨
                assertThat(response).isNotNull();
                assertThat(response.status()).isEqualTo(SellerStatus.DISABLED);
            }
        }

        @Nested
        @DisplayName("존재하지 않는 셀러 ID가 주어지면")
        class Context_with_non_existent_seller_id {

            private UpdateSellerStatusCommand command;

            @BeforeEach
            void setUp() {
                // Given: 존재하지 않는 셀러 ID
                command = UpdateSellerStatusCommandFixture.createWithId(999L);

                // And: 셀러를 찾을 수 없음
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("SellerNotFoundException을 발생시킨다")
            void it_throws_seller_not_found_exception() {
                // When & Then: 셀러 상태 변경 시도 시 예외 발생
                assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(SellerNotFoundException.class)
                    .hasMessageContaining(command.sellerId().toString());

                // And: 셀러 조회는 수행됨
                then(loadSellerPort).should().findById(any(MustitSellerId.class));

                // And: 셀러는 저장되지 않음
                then(saveSellerPort).should(never()).save(any(MustitSeller.class));
            }
        }

        @Nested
        @DisplayName("상태 전환 시나리오")
        class Context_status_transition_scenarios {

            @Test
            @DisplayName("PAUSED → ACTIVE 전환은 성공한다")
            void paused_to_active_succeeds() {
                // Given
                UpdateSellerStatusCommand command = UpdateSellerStatusCommandFixture.createActive();
                MustitSeller pausedSeller = MustitSellerFixture.createPaused();
                MustitSeller activeSeller = MustitSellerFixture.createActive();

                SellerQueryDto queryDto = new SellerQueryDto(
                    pausedSeller.getIdValue(),
                    pausedSeller.getSellerCode(),
                    pausedSeller.getSellerName(),
                    pausedSeller.getStatus(),
                    pausedSeller.getTotalProductCount(),
                    pausedSeller.getLastCrawledAt(),
                    pausedSeller.getCreatedAt(),
                    pausedSeller.getUpdatedAt()
                );
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(queryDto));
                given(sellerAssembler.toDomain(any(SellerQueryDto.class)))
                    .willReturn(pausedSeller);
                given(saveSellerPort.save(any(MustitSeller.class)))
                    .willReturn(activeSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response.status()).isEqualTo(SellerStatus.ACTIVE);
            }

            @Test
            @DisplayName("DISABLED → ACTIVE 전환은 성공한다")
            void disabled_to_active_succeeds() {
                // Given
                UpdateSellerStatusCommand command = UpdateSellerStatusCommandFixture.createActive();
                MustitSeller disabledSeller = MustitSellerFixture.createDisabled();
                MustitSeller activeSeller = MustitSellerFixture.createActive();

                SellerQueryDto queryDto = new SellerQueryDto(
                    disabledSeller.getIdValue(),
                    disabledSeller.getSellerCode(),
                    disabledSeller.getSellerName(),
                    disabledSeller.getStatus(),
                    disabledSeller.getTotalProductCount(),
                    disabledSeller.getLastCrawledAt(),
                    disabledSeller.getCreatedAt(),
                    disabledSeller.getUpdatedAt()
                );
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(queryDto));
                given(sellerAssembler.toDomain(any(SellerQueryDto.class)))
                    .willReturn(disabledSeller);
                given(saveSellerPort.save(any(MustitSeller.class)))
                    .willReturn(activeSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response.status()).isEqualTo(SellerStatus.ACTIVE);
            }

            @Test
            @DisplayName("ACTIVE → DISABLED 전환은 성공한다")
            void active_to_disabled_succeeds() {
                // Given
                UpdateSellerStatusCommand command = UpdateSellerStatusCommandFixture.createDisabled();
                MustitSeller activeSeller = MustitSellerFixture.createActive();
                MustitSeller disabledSeller = MustitSellerFixture.createDisabled();

                SellerQueryDto queryDto = new SellerQueryDto(
                    activeSeller.getIdValue(),
                    activeSeller.getSellerCode(),
                    activeSeller.getSellerName(),
                    activeSeller.getStatus(),
                    activeSeller.getTotalProductCount(),
                    activeSeller.getLastCrawledAt(),
                    activeSeller.getCreatedAt(),
                    activeSeller.getUpdatedAt()
                );
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(queryDto));
                given(sellerAssembler.toDomain(any(SellerQueryDto.class)))
                    .willReturn(activeSeller);
                given(saveSellerPort.save(any(MustitSeller.class)))
                    .willReturn(disabledSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response.status()).isEqualTo(SellerStatus.DISABLED);
            }

            @Test
            @DisplayName("PAUSED → DISABLED 전환은 성공한다")
            void paused_to_disabled_succeeds() {
                // Given
                UpdateSellerStatusCommand command = UpdateSellerStatusCommandFixture.createDisabled();
                MustitSeller pausedSeller = MustitSellerFixture.createPaused();
                MustitSeller disabledSeller = MustitSellerFixture.createDisabled();

                SellerQueryDto queryDto = new SellerQueryDto(
                    pausedSeller.getIdValue(),
                    pausedSeller.getSellerCode(),
                    pausedSeller.getSellerName(),
                    pausedSeller.getStatus(),
                    pausedSeller.getTotalProductCount(),
                    pausedSeller.getLastCrawledAt(),
                    pausedSeller.getCreatedAt(),
                    pausedSeller.getUpdatedAt()
                );
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(queryDto));
                given(sellerAssembler.toDomain(any(SellerQueryDto.class)))
                    .willReturn(pausedSeller);
                given(saveSellerPort.save(any(MustitSeller.class)))
                    .willReturn(disabledSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response.status()).isEqualTo(SellerStatus.DISABLED);
            }
        }

        @Nested
        @DisplayName("동일 상태로 변경 시도 시")
        class Context_same_status_update {

            @Test
            @DisplayName("ACTIVE → ACTIVE 전환은 성공한다")
            void active_to_active_succeeds() {
                // Given
                UpdateSellerStatusCommand command = UpdateSellerStatusCommandFixture.createActive();
                MustitSeller activeSeller = MustitSellerFixture.createActive();

                SellerQueryDto queryDto = new SellerQueryDto(
                    activeSeller.getIdValue(),
                    activeSeller.getSellerCode(),
                    activeSeller.getSellerName(),
                    activeSeller.getStatus(),
                    activeSeller.getTotalProductCount(),
                    activeSeller.getLastCrawledAt(),
                    activeSeller.getCreatedAt(),
                    activeSeller.getUpdatedAt()
                );
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(queryDto));
                given(sellerAssembler.toDomain(any(SellerQueryDto.class)))
                    .willReturn(activeSeller);
                given(saveSellerPort.save(any(MustitSeller.class)))
                    .willReturn(activeSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response.status()).isEqualTo(SellerStatus.ACTIVE);
                then(saveSellerPort).should().save(any(MustitSeller.class));
            }

            @Test
            @DisplayName("PAUSED → PAUSED 전환은 성공한다")
            void paused_to_paused_succeeds() {
                // Given
                UpdateSellerStatusCommand command = UpdateSellerStatusCommandFixture.createPaused();
                MustitSeller pausedSeller = MustitSellerFixture.createPaused();

                SellerQueryDto queryDto = new SellerQueryDto(
                    pausedSeller.getIdValue(),
                    pausedSeller.getSellerCode(),
                    pausedSeller.getSellerName(),
                    pausedSeller.getStatus(),
                    pausedSeller.getTotalProductCount(),
                    pausedSeller.getLastCrawledAt(),
                    pausedSeller.getCreatedAt(),
                    pausedSeller.getUpdatedAt()
                );
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(queryDto));
                given(sellerAssembler.toDomain(any(SellerQueryDto.class)))
                    .willReturn(pausedSeller);
                given(saveSellerPort.save(any(MustitSeller.class)))
                    .willReturn(pausedSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response.status()).isEqualTo(SellerStatus.PAUSED);
                then(saveSellerPort).should().save(any(MustitSeller.class));
            }

            @Test
            @DisplayName("DISABLED → DISABLED 전환은 성공한다")
            void disabled_to_disabled_succeeds() {
                // Given
                UpdateSellerStatusCommand command = UpdateSellerStatusCommandFixture.createDisabled();
                MustitSeller disabledSeller = MustitSellerFixture.createDisabled();

                SellerQueryDto queryDto = new SellerQueryDto(
                    disabledSeller.getIdValue(),
                    disabledSeller.getSellerCode(),
                    disabledSeller.getSellerName(),
                    disabledSeller.getStatus(),
                    disabledSeller.getTotalProductCount(),
                    disabledSeller.getLastCrawledAt(),
                    disabledSeller.getCreatedAt(),
                    disabledSeller.getUpdatedAt()
                );
                given(loadSellerPort.findById(any(MustitSellerId.class)))
                    .willReturn(Optional.of(queryDto));
                given(sellerAssembler.toDomain(any(SellerQueryDto.class)))
                    .willReturn(disabledSeller);
                given(saveSellerPort.save(any(MustitSeller.class)))
                    .willReturn(disabledSeller);

                // When
                SellerResponse response = sut.execute(command);

                // Then
                assertThat(response.status()).isEqualTo(SellerStatus.DISABLED);
                then(saveSellerPort).should().save(any(MustitSeller.class));
            }
        }
    }
}
