package com.ryuqq.crawlinghub.application.schedule.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.crawlinghub.application.common.dto.command.UpdateContext;
import com.ryuqq.crawlinghub.application.schedule.assembler.CrawlSchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.component.CrawlSchedulerPersistenceValidator;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.facade.CrawlerSchedulerFacade;
import com.ryuqq.crawlinghub.application.schedule.factory.command.CrawlSchedulerCommandFactory;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.exception.CrawlSchedulerNotFoundException;
import com.ryuqq.crawlinghub.domain.schedule.exception.DuplicateSchedulerNameException;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerUpdateData;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UpdateCrawlSchedulerService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Validator, CommandFactory, Facade, ReadManager Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCrawlSchedulerService 테스트")
class UpdateCrawlSchedulerServiceTest {

    @Mock private CrawlSchedulerPersistenceValidator validator;

    @Mock private CrawlSchedulerReadManager readManager;

    @Mock private CrawlSchedulerCommandFactory commandFactory;

    @Mock private CrawlerSchedulerFacade facade;

    @Mock private CrawlSchedulerAssembler assembler;

    @InjectMocks private UpdateCrawlSchedulerService service;

    @Nested
    @DisplayName("update() 스케줄러 수정 테스트")
    class Update {

        @Test
        @DisplayName("[성공] 스케줄러 정보 수정 시 CrawlSchedulerResponse 반환")
        void shouldUpdateSchedulerAndReturnResponse() {
            // Given
            Long schedulerId = 1L;
            UpdateCrawlSchedulerCommand command =
                    new UpdateCrawlSchedulerCommand(
                            schedulerId, "updated-scheduler", "cron(0 12 * * ? *)", true);
            CrawlScheduler existingScheduler = CrawlSchedulerFixture.anActiveScheduler();

            Instant changedAt = Instant.now();
            CrawlSchedulerUpdateData updateData =
                    CrawlSchedulerUpdateData.of(
                            SchedulerName.of("updated-scheduler"),
                            CronExpression.of("cron(0 12 * * ? *)"),
                            SchedulerStatus.ACTIVE);
            UpdateContext<CrawlSchedulerId, CrawlSchedulerUpdateData> context =
                    new UpdateContext<>(CrawlSchedulerId.of(schedulerId), updateData, changedAt);

            CrawlSchedulerResponse expectedResponse =
                    new CrawlSchedulerResponse(
                            schedulerId,
                            1L,
                            "updated-scheduler",
                            "cron(0 12 * * ? *)",
                            SchedulerStatus.ACTIVE,
                            Instant.now(),
                            Instant.now());

            given(commandFactory.createUpdateContext(command)).willReturn(context);
            given(readManager.getById(context.id())).willReturn(existingScheduler);
            given(assembler.toResponse(existingScheduler)).willReturn(expectedResponse);

            // When
            CrawlSchedulerResponse result = service.update(command);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(commandFactory).should().createUpdateContext(command);
            then(readManager).should().getById(context.id());
            then(validator)
                    .should()
                    .validateDuplicateSchedulerNameForUpdate(
                            any(CrawlScheduler.class), anyString());
            then(facade).should().update(existingScheduler);
            then(assembler).should().toResponse(existingScheduler);
        }

        @Test
        @DisplayName("[성공] 이름 변경 없이 상태만 수정 시 중복 검사는 Validator에서 스킵")
        void shouldSkipDuplicateCheckWhenNameNotChanged() {
            // Given
            Long schedulerId = 1L;
            CrawlScheduler existingScheduler = CrawlSchedulerFixture.anActiveScheduler();
            String sameName = existingScheduler.getSchedulerNameValue();

            UpdateCrawlSchedulerCommand command =
                    new UpdateCrawlSchedulerCommand(
                            schedulerId, sameName, "cron(0 12 * * ? *)", false);

            CrawlSchedulerUpdateData updateData =
                    CrawlSchedulerUpdateData.of(
                            SchedulerName.of(sameName),
                            CronExpression.of("cron(0 12 * * ? *)"),
                            SchedulerStatus.INACTIVE);
            UpdateContext<CrawlSchedulerId, CrawlSchedulerUpdateData> context =
                    new UpdateContext<>(
                            CrawlSchedulerId.of(schedulerId), updateData, Instant.now());

            CrawlSchedulerResponse expectedResponse =
                    new CrawlSchedulerResponse(
                            schedulerId,
                            1L,
                            sameName,
                            "cron(0 12 * * ? *)",
                            SchedulerStatus.INACTIVE,
                            Instant.now(),
                            Instant.now());

            given(commandFactory.createUpdateContext(command)).willReturn(context);
            given(readManager.getById(context.id())).willReturn(existingScheduler);
            given(assembler.toResponse(existingScheduler)).willReturn(expectedResponse);

            // When
            service.update(command);

            // Then
            then(validator)
                    .should()
                    .validateDuplicateSchedulerNameForUpdate(existingScheduler, sameName);
            then(facade).should().update(existingScheduler);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 스케줄러 수정 시 CrawlSchedulerNotFoundException 발생")
        void shouldThrowExceptionWhenSchedulerNotFound() {
            // Given
            Long schedulerId = 999L;
            UpdateCrawlSchedulerCommand command =
                    new UpdateCrawlSchedulerCommand(
                            schedulerId, "new-name", "cron(0 0 * * ? *)", true);

            CrawlSchedulerUpdateData updateData =
                    CrawlSchedulerUpdateData.of(
                            SchedulerName.of("new-name"),
                            CronExpression.of("cron(0 0 * * ? *)"),
                            SchedulerStatus.ACTIVE);
            UpdateContext<CrawlSchedulerId, CrawlSchedulerUpdateData> context =
                    new UpdateContext<>(
                            CrawlSchedulerId.of(schedulerId), updateData, Instant.now());

            given(commandFactory.createUpdateContext(command)).willReturn(context);
            given(readManager.getById(context.id()))
                    .willThrow(new CrawlSchedulerNotFoundException(schedulerId));

            // When & Then
            assertThatThrownBy(() -> service.update(command))
                    .isInstanceOf(CrawlSchedulerNotFoundException.class);

            then(facade).should(never()).update(any());
        }

        @Test
        @DisplayName("[실패] 스케줄러명 중복 시 DuplicateSchedulerNameException 발생")
        void shouldThrowExceptionWhenSchedulerNameDuplicated() {
            // Given
            Long schedulerId = 1L;
            UpdateCrawlSchedulerCommand command =
                    new UpdateCrawlSchedulerCommand(
                            schedulerId, "duplicate-name", "cron(0 0 * * ? *)", true);
            CrawlScheduler existingScheduler = CrawlSchedulerFixture.anActiveScheduler();

            CrawlSchedulerUpdateData updateData =
                    CrawlSchedulerUpdateData.of(
                            SchedulerName.of("duplicate-name"),
                            CronExpression.of("cron(0 0 * * ? *)"),
                            SchedulerStatus.ACTIVE);
            UpdateContext<CrawlSchedulerId, CrawlSchedulerUpdateData> context =
                    new UpdateContext<>(
                            CrawlSchedulerId.of(schedulerId), updateData, Instant.now());

            given(commandFactory.createUpdateContext(command)).willReturn(context);
            given(readManager.getById(context.id())).willReturn(existingScheduler);
            doThrow(new DuplicateSchedulerNameException(1L, "duplicate-name"))
                    .when(validator)
                    .validateDuplicateSchedulerNameForUpdate(
                            any(CrawlScheduler.class), anyString());

            // When & Then
            assertThatThrownBy(() -> service.update(command))
                    .isInstanceOf(DuplicateSchedulerNameException.class);

            then(facade).should(never()).update(any());
        }
    }
}
