package com.ryuqq.crawlinghub.application.schedule.component;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.exception.DuplicateSchedulerNameException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerPersistenceValidator 단위 테스트
 *
 * <p>Mockist 스타일 테스트: SellerReadManager, CrawlSchedulerReadManager Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerPersistenceValidator 테스트")
class CrawlSchedulerPersistenceValidatorTest {

    @Mock private SellerReadManager sellerReadManager;

    @Mock private CrawlSchedulerReadManager crawlSchedulerReadManager;

    @InjectMocks private CrawlSchedulerPersistenceValidator validator;

    @Nested
    @DisplayName("validateForRegistration() 등록 검증 테스트")
    class ValidateForRegistration {

        @Test
        @DisplayName("[성공] 셀러 존재, 스케줄러명 미중복 시 예외 없음")
        void shouldPassWhenSellerExistsAndSchedulerNameNotDuplicated() {
            // Given
            Long sellerId = 1L;
            String schedulerName = "new-scheduler";
            SellerId sellerIdVo = SellerId.of(sellerId);

            given(sellerReadManager.existsById(sellerIdVo)).willReturn(true);
            given(
                            crawlSchedulerReadManager.existsBySellerIdAndSchedulerName(
                                    sellerIdVo, schedulerName))
                    .willReturn(false);

            // When & Then
            assertThatCode(() -> validator.validateForRegistration(sellerId, schedulerName))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("[실패] 셀러 없으면 SellerNotFoundException 발생")
        void shouldThrowExceptionWhenSellerNotFound() {
            // Given
            Long sellerId = 999L;
            String schedulerName = "some-scheduler";
            SellerId sellerIdVo = SellerId.of(sellerId);

            given(sellerReadManager.existsById(sellerIdVo)).willReturn(false);

            // When & Then
            assertThatThrownBy(() -> validator.validateForRegistration(sellerId, schedulerName))
                    .isInstanceOf(SellerNotFoundException.class);

            then(crawlSchedulerReadManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("[실패] 스케줄러명 중복 시 DuplicateSchedulerNameException 발생")
        void shouldThrowExceptionWhenSchedulerNameDuplicated() {
            // Given
            Long sellerId = 1L;
            String duplicateName = "duplicate-scheduler";
            SellerId sellerIdVo = SellerId.of(sellerId);

            given(sellerReadManager.existsById(sellerIdVo)).willReturn(true);
            given(
                            crawlSchedulerReadManager.existsBySellerIdAndSchedulerName(
                                    sellerIdVo, duplicateName))
                    .willReturn(true);

            // When & Then
            assertThatThrownBy(() -> validator.validateForRegistration(sellerId, duplicateName))
                    .isInstanceOf(DuplicateSchedulerNameException.class);
        }
    }

    @Nested
    @DisplayName("validateDuplicateSchedulerNameForUpdate() 수정 검증 테스트")
    class ValidateDuplicateSchedulerNameForUpdate {

        @Test
        @DisplayName("[성공] 동일한 스케줄러명으로 수정 시 중복 체크 건너뜀")
        void shouldSkipDuplicateCheckWhenNameNotChanged() {
            // Given
            CrawlScheduler currentScheduler = CrawlSchedulerFixture.anActiveScheduler();
            String sameName = currentScheduler.getSchedulerName().value();

            // When & Then - hasSameSchedulerName이 true이면 즉시 반환
            assertThatCode(
                            () ->
                                    validator.validateDuplicateSchedulerNameForUpdate(
                                            currentScheduler, sameName))
                    .doesNotThrowAnyException();

            then(crawlSchedulerReadManager).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("[성공] 다른 스케줄러명으로 수정 시 미중복이면 예외 없음")
        void shouldPassWhenNewNameNotDuplicated() {
            // Given
            CrawlScheduler currentScheduler = CrawlSchedulerFixture.anActiveScheduler();
            String newName = "completely-different-scheduler-name";
            SellerId sellerId = currentScheduler.getSellerId();

            given(crawlSchedulerReadManager.existsBySellerIdAndSchedulerName(sellerId, newName))
                    .willReturn(false);

            // When & Then
            assertThatCode(
                            () ->
                                    validator.validateDuplicateSchedulerNameForUpdate(
                                            currentScheduler, newName))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("[실패] 다른 스케줄러명이 이미 존재하면 DuplicateSchedulerNameException 발생")
        void shouldThrowExceptionWhenNewNameAlreadyExists() {
            // Given
            CrawlScheduler currentScheduler = CrawlSchedulerFixture.anActiveScheduler();
            String duplicateName = "existing-other-scheduler-name";
            SellerId sellerId = currentScheduler.getSellerId();

            given(
                            crawlSchedulerReadManager.existsBySellerIdAndSchedulerName(
                                    sellerId, duplicateName))
                    .willReturn(true);

            // When & Then
            assertThatThrownBy(
                            () ->
                                    validator.validateDuplicateSchedulerNameForUpdate(
                                            currentScheduler, duplicateName))
                    .isInstanceOf(DuplicateSchedulerNameException.class);
        }
    }
}
