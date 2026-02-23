package com.ryuqq.crawlinghub.application.execution.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

import com.ryuqq.crawlinghub.application.task.dto.bundle.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.factory.command.CrawlTaskCommandFactory;
import com.ryuqq.crawlinghub.application.task.internal.CrawlTaskCommandFacade;
import com.ryuqq.crawlinghub.application.task.validator.CrawlTaskPersistenceValidator;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.util.List;
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
@DisplayName("FollowUpTaskCreator 단위 테스트")
class FollowUpTaskCreatorTest {

    @Mock private CrawlTaskPersistenceValidator validator;

    @Mock private CrawlTaskCommandFactory commandFactory;

    @Mock private CrawlTaskCommandFacade coordinator;

    @Mock private CrawlTaskBundle mockBundle;

    @InjectMocks private FollowUpTaskCreator creator;

    @Nested
    @DisplayName("execute() 단건 태스크 생성 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] DETAIL 태스크 생성")
        void shouldCreateDetailTask() {
            // Given
            CreateCrawlTaskCommand command =
                    CreateCrawlTaskCommand.forDetail(1L, 1L, "test-seller", 12345L);
            CrawlTask crawlTask =
                    com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture.aWaitingTask();

            given(commandFactory.createBundle(command)).willReturn(mockBundle);
            given(mockBundle.crawlTask()).willReturn(crawlTask);

            // When
            creator.execute(command);

            // Then
            then(commandFactory).should().createBundle(command);
            then(validator).should().validateNoDuplicateTask(crawlTask);
            then(coordinator).should().persist(mockBundle);
        }
    }

    @Nested
    @DisplayName("executeBatch() 일괄 태스크 생성 테스트")
    class ExecuteBatch {

        @Test
        @DisplayName("[성공] 여러 태스크 일괄 생성")
        void shouldCreateMultipleTasks() {
            // Given
            List<CreateCrawlTaskCommand> commands =
                    List.of(
                            CreateCrawlTaskCommand.forDetail(1L, 1L, "test-seller", 100L),
                            CreateCrawlTaskCommand.forDetail(1L, 1L, "test-seller", 101L),
                            CreateCrawlTaskCommand.forOption(1L, 1L, "test-seller", 100L));

            given(commandFactory.createBundle(any(CreateCrawlTaskCommand.class)))
                    .willReturn(mockBundle);
            given(mockBundle.crawlTask())
                    .willReturn(
                            com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture
                                    .aWaitingTask());

            // When
            creator.executeBatch(commands);

            // Then
            then(commandFactory).should(times(3)).createBundle(any(CreateCrawlTaskCommand.class));
            then(coordinator).should(times(3)).persist(mockBundle);
        }

        @Test
        @DisplayName("[성공] 일부 태스크 실패해도 나머지 계속 처리")
        void shouldContinueOnIndividualFailure() {
            // Given
            CreateCrawlTaskCommand command1 =
                    CreateCrawlTaskCommand.forDetail(1L, 1L, "test-seller", 100L);
            CreateCrawlTaskCommand command2 =
                    CreateCrawlTaskCommand.forDetail(1L, 1L, "test-seller", 101L);
            CreateCrawlTaskCommand command3 =
                    CreateCrawlTaskCommand.forDetail(1L, 1L, "test-seller", 102L);
            List<CreateCrawlTaskCommand> commands = List.of(command1, command2, command3);

            CrawlTaskBundle bundle1 = org.mockito.Mockito.mock(CrawlTaskBundle.class);
            CrawlTaskBundle bundle2 = org.mockito.Mockito.mock(CrawlTaskBundle.class);
            CrawlTaskBundle bundle3 = org.mockito.Mockito.mock(CrawlTaskBundle.class);

            given(commandFactory.createBundle(command1)).willReturn(bundle1);
            given(commandFactory.createBundle(command2)).willReturn(bundle2);
            given(commandFactory.createBundle(command3)).willReturn(bundle3);
            given(bundle1.crawlTask())
                    .willReturn(
                            com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture
                                    .aWaitingTask());
            given(bundle2.crawlTask())
                    .willReturn(
                            com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture
                                    .aWaitingTask());
            given(bundle3.crawlTask())
                    .willReturn(
                            com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture
                                    .aWaitingTask());

            doThrow(new RuntimeException("Duplicate task")).when(coordinator).persist(bundle2);

            // When
            creator.executeBatch(commands);

            // Then
            then(coordinator).should().persist(bundle1);
            then(coordinator).should().persist(bundle2);
            then(coordinator).should().persist(bundle3);
        }

        @Test
        @DisplayName("[성공] 빈 목록 처리")
        void shouldHandleEmptyList() {
            // Given
            List<CreateCrawlTaskCommand> commands = List.of();

            // When
            creator.executeBatch(commands);

            // Then
            then(commandFactory).shouldHaveNoInteractions();
            then(coordinator).shouldHaveNoInteractions();
        }
    }
}
