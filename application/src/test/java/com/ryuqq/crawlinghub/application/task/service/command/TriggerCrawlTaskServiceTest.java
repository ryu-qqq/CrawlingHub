package com.ryuqq.crawlinghub.application.task.service.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.application.task.dto.bundle.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.factory.command.CrawlTaskCommandFactory;
import com.ryuqq.crawlinghub.application.task.internal.CrawlTaskCommandFacade;
import com.ryuqq.crawlinghub.application.task.validator.CrawlTaskPersistenceValidator;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.exception.CrawlSchedulerNotFoundException;
import com.ryuqq.crawlinghub.domain.schedule.exception.InvalidSchedulerStateException;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * TriggerCrawlTaskService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TriggerCrawlTaskService 테스트")
class TriggerCrawlTaskServiceTest {

    @Mock private CrawlTaskPersistenceValidator validator;

    @Mock private CrawlTaskCommandFactory commandFactory;

    @Mock private CrawlTaskCommandFacade coordinator;

    @Mock private SellerReadManager sellerReadManager;

    @Mock private CrawlTaskBundle mockBundle;

    @InjectMocks private TriggerCrawlTaskService service;

    @Nested
    @DisplayName("execute() 크롤 태스크 트리거 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 활성 스케줄러로 태스크 트리거 시 정상 실행")
        void shouldTriggerTaskSuccessfully() {
            // Given
            Long crawlSchedulerId = 1L;
            TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(crawlSchedulerId);
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            Seller seller = SellerFixture.anActiveSeller();
            CrawlTask crawlTask =
                    com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture.aWaitingTask();

            given(validator.findAndValidateScheduler(any(CrawlSchedulerId.class)))
                    .willReturn(scheduler);
            given(sellerReadManager.findById(any(SellerId.class))).willReturn(Optional.of(seller));
            given(commandFactory.createBundle(scheduler, seller)).willReturn(mockBundle);
            given(mockBundle.crawlTask()).willReturn(crawlTask);

            // When
            service.execute(command);

            // Then
            then(validator)
                    .should()
                    .findAndValidateScheduler(CrawlSchedulerId.of(crawlSchedulerId));
            then(sellerReadManager).should().findById(any(SellerId.class));
            then(commandFactory).should().createBundle(scheduler, seller);
            then(validator).should().validateNoDuplicateTask(crawlTask);
            then(coordinator).should().persist(mockBundle);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 스케줄러 ID로 트리거 시 CrawlSchedulerNotFoundException 발생")
        void shouldThrowExceptionWhenSchedulerNotFound() {
            // Given
            Long crawlSchedulerId = 999L;
            TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(crawlSchedulerId);

            given(validator.findAndValidateScheduler(any(CrawlSchedulerId.class)))
                    .willThrow(new CrawlSchedulerNotFoundException(crawlSchedulerId));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(CrawlSchedulerNotFoundException.class);

            then(commandFactory).should(never()).createBundle(any(CrawlScheduler.class), any());
            then(coordinator).should(never()).persist(any());
        }

        @Test
        @DisplayName("[실패] 비활성 스케줄러로 트리거 시 InvalidSchedulerStateException 발생")
        void shouldThrowExceptionWhenSchedulerInactive() {
            // Given
            Long crawlSchedulerId = 1L;
            TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(crawlSchedulerId);

            given(validator.findAndValidateScheduler(any(CrawlSchedulerId.class)))
                    .willThrow(
                            new InvalidSchedulerStateException(
                                    SchedulerStatus.INACTIVE, SchedulerStatus.ACTIVE));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(InvalidSchedulerStateException.class);

            then(commandFactory).should(never()).createBundle(any(CrawlScheduler.class), any());
            then(coordinator).should(never()).persist(any());
        }
    }
}
