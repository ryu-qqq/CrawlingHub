package com.ryuqq.crawlinghub.application.schedule.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.crawlinghub.application.schedule.assembler.CrawlSchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.facade.CrawlerSchedulerFacade;
import com.ryuqq.crawlinghub.application.schedule.manager.query.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.exception.CrawlSchedulerNotFoundException;
import com.ryuqq.crawlinghub.domain.schedule.exception.DuplicateSchedulerNameException;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import java.util.Optional;
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
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCrawlSchedulerService 테스트")
class UpdateCrawlSchedulerServiceTest {

    @Mock private CrawlSchedulerReadManager readManager;

    @Mock private CrawlerSchedulerFacade crawlerSchedulerFacade;

    @Mock private CrawlSchedulerAssembler crawlSchedulerAssembler;

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
            CrawlSchedulerResponse expectedResponse =
                    new CrawlSchedulerResponse(
                            schedulerId,
                            1L,
                            "updated-scheduler",
                            "cron(0 12 * * ? *)",
                            SchedulerStatus.ACTIVE,
                            Instant.now(),
                            Instant.now());

            given(readManager.findById(any(CrawlSchedulerId.class)))
                    .willReturn(Optional.of(existingScheduler));
            given(readManager.existsBySellerIdAndSchedulerName(any(SellerId.class), anyString()))
                    .willReturn(false);
            given(crawlSchedulerAssembler.toResponse(existingScheduler))
                    .willReturn(expectedResponse);

            // When
            CrawlSchedulerResponse result = service.update(command);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(readManager).should().findById(CrawlSchedulerId.of(schedulerId));
            then(crawlerSchedulerFacade)
                    .should()
                    .updateScheduler(
                            any(CrawlScheduler.class),
                            any(SchedulerName.class),
                            any(CronExpression.class),
                            any(SchedulerStatus.class));
            then(crawlSchedulerAssembler).should().toResponse(existingScheduler);
        }

        @Test
        @DisplayName("[성공] 이름 변경 없이 상태만 수정 시 중복 검사 생략")
        void shouldSkipDuplicateCheckWhenNameNotChanged() {
            // Given
            Long schedulerId = 1L;
            CrawlScheduler existingScheduler = CrawlSchedulerFixture.anActiveScheduler();
            String sameName = existingScheduler.getSchedulerNameValue();

            UpdateCrawlSchedulerCommand command =
                    new UpdateCrawlSchedulerCommand(
                            schedulerId, sameName, "cron(0 12 * * ? *)", false);
            CrawlSchedulerResponse expectedResponse =
                    new CrawlSchedulerResponse(
                            schedulerId,
                            1L,
                            sameName,
                            "cron(0 12 * * ? *)",
                            SchedulerStatus.INACTIVE,
                            Instant.now(),
                            Instant.now());

            given(readManager.findById(any(CrawlSchedulerId.class)))
                    .willReturn(Optional.of(existingScheduler));
            given(crawlSchedulerAssembler.toResponse(existingScheduler))
                    .willReturn(expectedResponse);

            // When
            service.update(command);

            // Then
            then(readManager)
                    .should(never())
                    .existsBySellerIdAndSchedulerName(any(SellerId.class), anyString());
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 스케줄러 수정 시 CrawlSchedulerNotFoundException 발생")
        void shouldThrowExceptionWhenSchedulerNotFound() {
            // Given
            Long schedulerId = 999L;
            UpdateCrawlSchedulerCommand command =
                    new UpdateCrawlSchedulerCommand(
                            schedulerId, "new-name", "cron(0 0 * * ? *)", true);

            given(readManager.findById(any(CrawlSchedulerId.class))).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.update(command))
                    .isInstanceOf(CrawlSchedulerNotFoundException.class);

            then(crawlerSchedulerFacade)
                    .should(never())
                    .updateScheduler(
                            any(CrawlScheduler.class),
                            any(SchedulerName.class),
                            any(CronExpression.class),
                            any(SchedulerStatus.class));
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

            given(readManager.findById(any(CrawlSchedulerId.class)))
                    .willReturn(Optional.of(existingScheduler));
            given(readManager.existsBySellerIdAndSchedulerName(any(SellerId.class), anyString()))
                    .willReturn(true);

            // When & Then
            assertThatThrownBy(() -> service.update(command))
                    .isInstanceOf(DuplicateSchedulerNameException.class);

            then(crawlerSchedulerFacade)
                    .should(never())
                    .updateScheduler(
                            any(CrawlScheduler.class),
                            any(SchedulerName.class),
                            any(CronExpression.class),
                            any(SchedulerStatus.class));
        }
    }
}
