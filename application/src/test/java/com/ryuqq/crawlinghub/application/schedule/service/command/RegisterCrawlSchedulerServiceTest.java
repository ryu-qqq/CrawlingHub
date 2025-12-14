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
import com.ryuqq.crawlinghub.application.schedule.dto.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.facade.CrawlerSchedulerFacade;
import com.ryuqq.crawlinghub.application.schedule.factory.command.CrawlSchedulerCommandFactory;
import com.ryuqq.crawlinghub.application.schedule.manager.query.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.exception.DuplicateSchedulerNameException;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RegisterCrawlSchedulerService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterCrawlSchedulerService 테스트")
class RegisterCrawlSchedulerServiceTest {

    @Mock private CrawlSchedulerCommandFactory commandFactory;

    @Mock private CrawlSchedulerAssembler assembler;

    @Mock private CrawlerSchedulerFacade crawlerSchedulerFacade;

    @Mock private CrawlSchedulerReadManager readManager;

    @Mock private CrawlSchedulerBundle mockBundle;

    @InjectMocks private RegisterCrawlSchedulerService service;

    @Nested
    @DisplayName("register() 스케줄러 등록 테스트")
    class Register {

        @Test
        @DisplayName("[성공] 중복 없는 신규 스케줄러 등록")
        void shouldRegisterNewSchedulerWhenNoDuplicate() {
            // Given
            RegisterCrawlSchedulerCommand command =
                    new RegisterCrawlSchedulerCommand(1L, "daily-crawl", "cron(0 0 * * ? *)");
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerResponse expectedResponse =
                    new CrawlSchedulerResponse(
                            1L,
                            1L,
                            "daily-crawl",
                            "cron(0 0 * * ? *)",
                            SchedulerStatus.ACTIVE,
                            Instant.now(),
                            Instant.now());

            given(readManager.existsBySellerIdAndSchedulerName(any(SellerId.class), anyString()))
                    .willReturn(false);
            given(commandFactory.createBundle(command)).willReturn(mockBundle);
            given(crawlerSchedulerFacade.persist(mockBundle)).willReturn(scheduler);
            given(assembler.toResponse(scheduler)).willReturn(expectedResponse);

            // When
            CrawlSchedulerResponse result = service.register(command);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(readManager)
                    .should()
                    .existsBySellerIdAndSchedulerName(SellerId.of(1L), "daily-crawl");
            then(commandFactory).should().createBundle(command);
            then(crawlerSchedulerFacade).should().persist(mockBundle);
            then(assembler).should().toResponse(scheduler);
        }

        @Test
        @DisplayName("[실패] 동일 셀러에 중복 스케줄러명 시 DuplicateSchedulerNameException 발생")
        void shouldThrowExceptionWhenSchedulerNameDuplicated() {
            // Given
            RegisterCrawlSchedulerCommand command =
                    new RegisterCrawlSchedulerCommand(
                            1L, "duplicate-scheduler", "cron(0 0 * * ? *)");

            given(readManager.existsBySellerIdAndSchedulerName(any(SellerId.class), anyString()))
                    .willReturn(true);

            // When & Then
            assertThatThrownBy(() -> service.register(command))
                    .isInstanceOf(DuplicateSchedulerNameException.class);

            then(commandFactory).should(never()).createBundle(any());
            then(crawlerSchedulerFacade).should(never()).persist(any());
        }
    }
}
