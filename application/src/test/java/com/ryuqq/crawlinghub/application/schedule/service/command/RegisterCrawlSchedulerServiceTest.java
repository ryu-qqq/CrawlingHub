package com.ryuqq.crawlinghub.application.schedule.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.crawlinghub.application.schedule.component.CrawlSchedulerPersistenceValidator;
import com.ryuqq.crawlinghub.application.schedule.dto.bundle.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterCrawlSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.facade.CrawlerSchedulerFacade;
import com.ryuqq.crawlinghub.application.schedule.factory.command.CrawlSchedulerCommandFactory;
import com.ryuqq.crawlinghub.domain.schedule.exception.DuplicateSchedulerNameException;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
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
 * <p>Mockist 스타일 테스트: Validator, CommandFactory, Facade Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterCrawlSchedulerService 테스트")
class RegisterCrawlSchedulerServiceTest {

    @Mock private CrawlSchedulerPersistenceValidator validator;

    @Mock private CrawlSchedulerCommandFactory commandFactory;

    @Mock private CrawlerSchedulerFacade facade;

    @InjectMocks private RegisterCrawlSchedulerService service;

    @Nested
    @DisplayName("register() 스케줄러 등록 테스트")
    class Register {

        @Test
        @DisplayName("[성공] 중복 없는 신규 스케줄러 등록 → 스케줄러 ID 반환")
        void shouldRegisterNewSchedulerWhenNoDuplicate() {
            // Given
            RegisterCrawlSchedulerCommand command =
                    new RegisterCrawlSchedulerCommand(1L, "daily-crawl", "cron(0 0 * * ? *)");
            CrawlSchedulerId savedId = CrawlSchedulerIdFixture.anAssignedId();
            CrawlSchedulerBundle bundle = mock(CrawlSchedulerBundle.class);

            given(commandFactory.createBundle(command)).willReturn(bundle);
            given(facade.persist(bundle)).willReturn(savedId);

            // When
            long result = service.register(command);

            // Then
            assertThat(result).isEqualTo(savedId.value());
            then(validator).should().validateForRegistration(1L, "daily-crawl");
            then(commandFactory).should().createBundle(command);
            then(facade).should().persist(bundle);
        }

        @Test
        @DisplayName("[실패] 동일 셀러에 중복 스케줄러명 시 DuplicateSchedulerNameException 발생")
        void shouldThrowExceptionWhenSchedulerNameDuplicated() {
            // Given
            RegisterCrawlSchedulerCommand command =
                    new RegisterCrawlSchedulerCommand(
                            1L, "duplicate-scheduler", "cron(0 0 * * ? *)");

            doThrow(new DuplicateSchedulerNameException(1L, "duplicate-scheduler"))
                    .when(validator)
                    .validateForRegistration(1L, "duplicate-scheduler");

            // When & Then
            assertThatThrownBy(() -> service.register(command))
                    .isInstanceOf(DuplicateSchedulerNameException.class);

            then(commandFactory).should(never()).createBundle(any());
            then(facade).should(never()).persist(any());
        }
    }
}
