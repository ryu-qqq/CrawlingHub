package com.ryuqq.crawlinghub.application.seller.component;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateMustItSellerIdException;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerNameException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerHasActiveSchedulersException;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerUpdateData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerPersistenceValidator 단위 테스트")
class SellerPersistenceValidatorTest {

    @Mock private SellerReadManager sellerReadManager;

    @Mock private CrawlSchedulerReadManager crawlSchedulerReadManager;

    @InjectMocks private SellerPersistenceValidator validator;

    @Nested
    @DisplayName("validateForRegistration() - 등록 시 검증")
    class ValidateForRegistration {

        @Test
        @DisplayName("[성공] 중복 없으면 예외 없이 통과")
        void shouldPassWhenNoDuplicate() {
            // Given
            Seller seller = SellerFixture.aNewActiveSeller();
            given(sellerReadManager.existsByMustItSellerName(any(MustItSellerName.class)))
                    .willReturn(false);
            given(sellerReadManager.existsBySellerName(any(SellerName.class))).willReturn(false);

            // When & Then
            assertThatCode(() -> validator.validateForRegistration(seller))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("[실패] MustItSellerName 중복 시 DuplicateMustItSellerIdException 발생")
        void shouldThrowWhenMustItSellerNameDuplicated() {
            // Given
            Seller seller = SellerFixture.aNewActiveSeller();
            given(sellerReadManager.existsByMustItSellerName(any(MustItSellerName.class)))
                    .willReturn(true);

            // When & Then
            assertThatThrownBy(() -> validator.validateForRegistration(seller))
                    .isInstanceOf(DuplicateMustItSellerIdException.class);
        }

        @Test
        @DisplayName("[실패] SellerName 중복 시 DuplicateSellerNameException 발생")
        void shouldThrowWhenSellerNameDuplicated() {
            // Given
            Seller seller = SellerFixture.aNewActiveSeller();
            given(sellerReadManager.existsByMustItSellerName(any(MustItSellerName.class)))
                    .willReturn(false);
            given(sellerReadManager.existsBySellerName(any(SellerName.class))).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> validator.validateForRegistration(seller))
                    .isInstanceOf(DuplicateSellerNameException.class);
        }
    }

    @Nested
    @DisplayName("validateForUpdate() - 수정 시 검증 (이름 중복 + 비활성화)")
    class ValidateForUpdate {

        @Test
        @DisplayName("[성공] 이름 변경 없고 상태 유지면 검증 통과")
        void shouldPassWhenNoNameChangeAndSameStatus() {
            // Given
            Seller existingSeller = SellerFixture.anActiveSeller();
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            existingSeller.getMustItSellerName(),
                            existingSeller.getSellerName(),
                            SellerStatus.ACTIVE);

            // When & Then
            assertThatCode(() -> validator.validateForUpdate(existingSeller, updateData))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("[실패] MustItSellerName 변경 시 중복이면 예외 발생")
        void shouldThrowWhenNewMustItSellerNameDuplicated() {
            // Given
            Seller existingSeller = SellerFixture.anActiveSeller();
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            MustItSellerName.of("new-different-name"),
                            existingSeller.getSellerName(),
                            SellerStatus.ACTIVE);

            given(
                            sellerReadManager.existsByMustItSellerNameExcludingId(
                                    any(MustItSellerName.class), any(SellerId.class)))
                    .willReturn(true);

            // When & Then
            assertThatThrownBy(() -> validator.validateForUpdate(existingSeller, updateData))
                    .isInstanceOf(DuplicateMustItSellerIdException.class);
        }

        @Test
        @DisplayName("[실패] SellerName 변경 시 중복이면 예외 발생")
        void shouldThrowWhenNewSellerNameDuplicated() {
            // Given
            Seller existingSeller = SellerFixture.anActiveSeller();
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            existingSeller.getMustItSellerName(),
                            SellerName.of("new-different-name"),
                            SellerStatus.ACTIVE);

            given(
                            sellerReadManager.existsBySellerNameExcludingId(
                                    any(SellerName.class), any(SellerId.class)))
                    .willReturn(true);

            // When & Then
            assertThatThrownBy(() -> validator.validateForUpdate(existingSeller, updateData))
                    .isInstanceOf(DuplicateSellerNameException.class);
        }

        @Test
        @DisplayName("[성공] ACTIVE → INACTIVE 전환 시 활성 스케줄러 없으면 통과")
        void shouldPassDeactivationWhenNoActiveSchedulers() {
            // Given
            Seller existingSeller = SellerFixture.anActiveSeller();
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            existingSeller.getMustItSellerName(),
                            existingSeller.getSellerName(),
                            SellerStatus.INACTIVE);

            given(crawlSchedulerReadManager.countActiveSchedulersBySellerId(any(SellerId.class)))
                    .willReturn(0L);

            // When & Then
            assertThatCode(() -> validator.validateForUpdate(existingSeller, updateData))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("[실패] ACTIVE → INACTIVE 전환 시 활성 스케줄러 존재하면 예외 발생")
        void shouldThrowWhenDeactivatingWithActiveSchedulers() {
            // Given
            Seller existingSeller = SellerFixture.anActiveSeller();
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            existingSeller.getMustItSellerName(),
                            existingSeller.getSellerName(),
                            SellerStatus.INACTIVE);

            given(crawlSchedulerReadManager.countActiveSchedulersBySellerId(any(SellerId.class)))
                    .willReturn(3L);

            // When & Then
            assertThatThrownBy(() -> validator.validateForUpdate(existingSeller, updateData))
                    .isInstanceOf(SellerHasActiveSchedulersException.class);
        }

        @Test
        @DisplayName("[성공] 이미 INACTIVE면 비활성화 검증 스킵")
        void shouldSkipDeactivationWhenAlreadyInactive() {
            // Given
            Seller existingSeller = SellerFixture.anInactiveSeller();
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            existingSeller.getMustItSellerName(),
                            existingSeller.getSellerName(),
                            SellerStatus.INACTIVE);

            // When & Then
            assertThatCode(() -> validator.validateForUpdate(existingSeller, updateData))
                    .doesNotThrowAnyException();
        }
    }
}
