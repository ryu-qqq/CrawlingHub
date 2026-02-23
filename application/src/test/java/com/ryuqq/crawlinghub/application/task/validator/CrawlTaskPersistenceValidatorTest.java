package com.ryuqq.crawlinghub.application.task.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.exception.DuplicateCrawlTaskException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskPersistenceValidator 단위 테스트
 *
 * <p>스케줄러 상태 검증 및 중복 태스크 검증 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskPersistenceValidator 테스트")
class CrawlTaskPersistenceValidatorTest {

    @Mock private CrawlSchedulerReadManager crawlSchedulerReadManager;

    @Mock private CrawlTaskReadManager crawlTaskReadManager;

    @InjectMocks private CrawlTaskPersistenceValidator validator;

    @Nested
    @DisplayName("findAndValidateScheduler() 테스트")
    class FindAndValidateScheduler {

        @Test
        @DisplayName("[성공] ACTIVE 스케줄러 조회 및 검증 성공")
        void shouldReturnActiveScheduler() {
            // Given
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            CrawlScheduler activeScheduler = CrawlSchedulerFixture.anActiveScheduler();
            given(crawlSchedulerReadManager.getById(schedulerId)).willReturn(activeScheduler);

            // When
            CrawlScheduler result = validator.findAndValidateScheduler(schedulerId);

            // Then
            assertThat(result).isEqualTo(activeScheduler);
            then(crawlSchedulerReadManager).should().getById(schedulerId);
        }

        @Test
        @DisplayName("[실패] INACTIVE 스케줄러는 validateActive에서 예외 발생")
        void shouldThrowWhenSchedulerIsInactive() {
            // Given
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            CrawlScheduler inactiveScheduler = CrawlSchedulerFixture.anInactiveScheduler();
            given(crawlSchedulerReadManager.getById(schedulerId)).willReturn(inactiveScheduler);

            // When / Then
            assertThatThrownBy(() -> validator.findAndValidateScheduler(schedulerId))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("validateNoDuplicateTask() 테스트")
    class ValidateNoDuplicateTask {

        @Test
        @DisplayName("[성공] 중복 태스크 없으면 예외 없음")
        void shouldNotThrowWhenNoDuplicateTask() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            given(
                            crawlTaskReadManager
                                    .existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                                            any(), any(), anyString(), anyString(), anyList()))
                    .willReturn(false);

            // When / Then - 예외 발생하지 않아야 함
            validator.validateNoDuplicateTask(task);
            then(crawlTaskReadManager)
                    .should()
                    .existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                            any(), any(), anyString(), anyString(), anyList());
        }

        @Test
        @DisplayName("[실패] 중복 태스크 있으면 DuplicateCrawlTaskException 발생")
        void shouldThrowWhenDuplicateTaskExists() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            given(
                            crawlTaskReadManager
                                    .existsBySchedulerIdAndTaskTypeAndEndpointAndStatusIn(
                                            any(), any(), anyString(), anyString(), anyList()))
                    .willReturn(true);

            // When / Then
            assertThatThrownBy(() -> validator.validateNoDuplicateTask(task))
                    .isInstanceOf(DuplicateCrawlTaskException.class);
        }
    }
}
