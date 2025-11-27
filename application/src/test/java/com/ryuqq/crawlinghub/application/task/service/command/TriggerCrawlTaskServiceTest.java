package com.ryuqq.crawlinghub.application.task.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.component.CrawlTaskPersistenceValidator;
import com.ryuqq.crawlinghub.application.task.dto.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.task.facade.CrawlTaskFacade;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.exception.CrawlSchedulerNotFoundException;
import com.ryuqq.crawlinghub.domain.schedule.exception.InvalidSchedulerStateException;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import java.time.LocalDateTime;
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

    @Mock
    private CrawlTaskPersistenceValidator validator;

    @Mock
    private CrawlTaskAssembler assembler;

    @Mock
    private CrawlTaskFacade facade;

    @Mock
    private CrawlTaskBundle mockBundle;

    @InjectMocks
    private TriggerCrawlTaskService service;

    @Nested
    @DisplayName("execute() 크롤 태스크 트리거 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 활성 스케줄러로 태스크 트리거 시 CrawlTaskResponse 반환")
        void shouldTriggerTaskAndReturnResponse() {
            // Given
            Long crawlSchedulerId = 1L;
            TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(crawlSchedulerId);
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlTask savedTask = CrawlTaskFixture.aWaitingTask();
            CrawlTaskResponse expectedResponse = new CrawlTaskResponse(
                    1L, crawlSchedulerId, 1L, "https://example.com/api",
                    CrawlTaskStatus.WAITING, CrawlTaskType.META, 0, LocalDateTime.now());

            given(validator.findAndValidateScheduler(any(CrawlSchedulerId.class)))
                    .willReturn(scheduler);
            given(assembler.toBundle(command, scheduler)).willReturn(mockBundle);
            given(facade.persist(mockBundle)).willReturn(mockBundle);
            given(mockBundle.getSavedCrawlTask()).willReturn(savedTask);
            given(assembler.toResponse(savedTask)).willReturn(expectedResponse);

            // When
            CrawlTaskResponse result = service.execute(command);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(validator).should().findAndValidateScheduler(CrawlSchedulerId.of(crawlSchedulerId));
            then(assembler).should().toBundle(command, scheduler);
            then(facade).should().persist(mockBundle);
            then(assembler).should().toResponse(savedTask);
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

            then(assembler).should(never()).toBundle(any(), any());
            then(facade).should(never()).persist(any());
        }

        @Test
        @DisplayName("[실패] 비활성 스케줄러로 트리거 시 InvalidSchedulerStateException 발생")
        void shouldThrowExceptionWhenSchedulerInactive() {
            // Given
            Long crawlSchedulerId = 1L;
            TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(crawlSchedulerId);

            given(validator.findAndValidateScheduler(any(CrawlSchedulerId.class)))
                    .willThrow(new InvalidSchedulerStateException(
                            com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus.INACTIVE,
                            com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus.ACTIVE));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(InvalidSchedulerStateException.class);

            then(assembler).should(never()).toBundle(any(), any());
            then(facade).should(never()).persist(any());
        }
    }
}
