package com.ryuqq.crawlinghub.adapter.out.persistence.seller.integration;

import com.ryuqq.crawlinghub.adapter.out.persistence.seller.adapter.MustItSellerCommandAdapter;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.adapter.MustItSellerQueryAdapter;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository.MustItSellerJpaRepository;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.query.SellerQueryDto;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.application.seller.port.out.SaveSellerPort;
import com.ryuqq.crawlinghub.domain.seller.MustItSeller;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerFixture;
import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;
import com.ryuqq.crawlinghub.domain.seller.SellerStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Seller Persistence Layer 통합 테스트
 *
 * <p>실제 DB 환경에서 Seller 엔티티의 CRUD 및 상태 변경 플로우를 검증합니다.
 *
 * <p><strong>테스트 시나리오:</strong></p>
 * <ul>
 *   <li>1. Seller 생성 및 저장</li>
 *   <li>2. Seller 조회 (ID, SellerCode)</li>
 *   <li>3. Seller 상태 변경 (ACTIVE ↔ PAUSED)</li>
 *   <li>4. Seller 삭제 (soft delete)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({MustItSellerCommandAdapter.class, MustItSellerQueryAdapter.class, SellerAssembler.class})
@DisplayName("Seller Persistence Layer 통합 테스트")
class SellerPersistenceIntegrationTest {

    @Autowired
    private MustItSellerJpaRepository jpaRepository;

    @Autowired
    private SaveSellerPort saveSellerPort;

    @Autowired
    private LoadSellerPort loadSellerPort;

    @Autowired
    private SellerAssembler assembler;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("Seller 생성 및 저장 플로우")
    class SellerCreationFlow {

        @Test
        @DisplayName("Seller를 생성하고 저장하면 DB에 저장된다")
        void it_saves_seller_successfully() {
            // Given
            MustItSeller seller = MustitSellerFixture.createActive();

            // When
            MustItSeller saved = saveSellerPort.save(seller);

            // Then
            assertThat(saved.getIdValue()).isNotNull();
            assertThat(saved.getSellerCode()).isNotNull();
            assertThat(saved.getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(saved.getTotalProductCount()).isZero();

            // And: DB에서 조회 가능
            Optional<SellerQueryDto> found = loadSellerPort.findById(MustItSellerId.of(saved.getIdValue()));
            assertThat(found).isPresent();
            assertThat(found.get().id()).isEqualTo(saved.getIdValue());
            assertThat(found.get().sellerCode()).isEqualTo(saved.getSellerCode());
        }

        @Test
        @DisplayName("여러 Seller를 생성하고 모두 저장된다")
        void it_saves_multiple_sellers_successfully() {
            // Given
            MustItSeller seller1 = MustitSellerFixture.createActive();
            MustItSeller seller2 = MustitSellerFixture.createPaused();

            // When
            MustItSeller saved1 = saveSellerPort.save(seller1);
            MustItSeller saved2 = saveSellerPort.save(seller2);

            // Then
            assertThat(saved1.getIdValue()).isNotNull();
            assertThat(saved2.getIdValue()).isNotNull();
            assertThat(saved1.getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(saved2.getStatus()).isEqualTo(SellerStatus.PAUSED);

            // And: 모두 DB에서 조회 가능
            long count = jpaRepository.count();
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Seller 조회 플로우")
    class SellerQueryFlow {

        @Test
        @DisplayName("ID로 Seller를 조회할 수 있다")
        void it_finds_seller_by_id() {
            // Given: Seller 저장
            MustItSeller seller = MustitSellerFixture.createActive();
            MustItSeller saved = saveSellerPort.save(seller);

            // When: ID로 조회
            Optional<SellerQueryDto> found = loadSellerPort.findById(MustItSellerId.of(saved.getIdValue()));

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().id()).isEqualTo(saved.getIdValue());
            assertThat(found.get().sellerName()).isEqualTo(saved.getSellerNameValue());
            assertThat(found.get().status()).isEqualTo(SellerStatus.ACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
        void it_returns_empty_for_non_existent_id() {
            // When: 존재하지 않는 ID로 조회
            Optional<SellerQueryDto> found = loadSellerPort.findById(MustItSellerId.of(999L));

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Seller 상태 변경 플로우")
    class SellerStatusChangeFlow {

        @Test
        @DisplayName("ACTIVE 상태의 Seller를 PAUSED로 변경할 수 있다")
        void it_pauses_active_seller() {
            // Given: ACTIVE 상태의 Seller
            MustItSeller seller = MustitSellerFixture.createActive();
            MustItSeller saved = saveSellerPort.save(seller);

            // When: PAUSED로 변경
            MustItSeller found = loadSellerPort.findById(MustItSellerId.of(saved.getIdValue()))
                .map(assembler::toDomain)
                .orElseThrow();
            found.pause();
            MustItSeller paused = saveSellerPort.save(found);

            // Then
            assertThat(paused.getStatus()).isEqualTo(SellerStatus.PAUSED);

            // And: DB에서 조회 시 PAUSED 상태
            Optional<SellerQueryDto> reloaded = loadSellerPort.findById(MustItSellerId.of(paused.getIdValue()));
            assertThat(reloaded).isPresent();
            assertThat(reloaded.get().status()).isEqualTo(SellerStatus.PAUSED);
        }

        @Test
        @DisplayName("PAUSED 상태의 Seller를 ACTIVE로 변경할 수 있다")
        void it_activates_paused_seller() {
            // Given: PAUSED 상태의 Seller
            MustItSeller seller = MustitSellerFixture.createPaused();
            MustItSeller saved = saveSellerPort.save(seller);

            // When: ACTIVE로 변경
            MustItSeller found = loadSellerPort.findById(MustItSellerId.of(saved.getIdValue()))
                .map(assembler::toDomain)
                .orElseThrow();
            found.activate();
            MustItSeller activated = saveSellerPort.save(found);

            // Then
            assertThat(activated.getStatus()).isEqualTo(SellerStatus.ACTIVE);

            // And: DB에서 조회 시 ACTIVE 상태
            Optional<SellerQueryDto> reloaded = loadSellerPort.findById(MustItSellerId.of(activated.getIdValue()));
            assertThat(reloaded).isPresent();
            assertThat(reloaded.get().status()).isEqualTo(SellerStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("통합 시나리오: Seller 생성 → 상태 변경 → 조회")
    class IntegratedScenario {

        @Test
        @DisplayName("Seller를 생성하고 상태를 변경한 후 다시 조회한다")
        void it_creates_changes_status_and_queries_seller() {
            // Given: Seller 생성 및 저장
            MustItSeller seller = MustitSellerFixture.createActive();
            MustItSeller saved = saveSellerPort.save(seller);
            assertThat(saved.getStatus()).isEqualTo(SellerStatus.ACTIVE);

            // When: PAUSED로 변경
            MustItSeller found = loadSellerPort.findById(MustItSellerId.of(saved.getIdValue()))
                .map(assembler::toDomain)
                .orElseThrow();
            found.pause();
            MustItSeller paused = saveSellerPort.save(found);

            // Then: PAUSED 상태 확인
            assertThat(paused.getStatus()).isEqualTo(SellerStatus.PAUSED);

            // And: ACTIVE로 다시 변경
            MustItSeller foundAgain = loadSellerPort.findById(MustItSellerId.of(paused.getIdValue()))
                .map(assembler::toDomain)
                .orElseThrow();
            foundAgain.activate();
            MustItSeller activated = saveSellerPort.save(foundAgain);

            // Then: ACTIVE 상태 확인
            assertThat(activated.getStatus()).isEqualTo(SellerStatus.ACTIVE);

            // And: 최종 조회 시 ACTIVE 상태
            Optional<SellerQueryDto> final_check = loadSellerPort.findById(MustItSellerId.of(activated.getIdValue()));
            assertThat(final_check).isPresent();
            assertThat(final_check.get().status()).isEqualTo(SellerStatus.ACTIVE);
        }
    }
}
