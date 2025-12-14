package com.ryuqq.crawlinghub.application.task.service.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

import com.ryuqq.crawlinghub.application.task.dto.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.facade.CrawlTaskFacade;
import com.ryuqq.crawlinghub.application.task.factory.command.CrawlTaskCommandFactory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CreateCrawlTaskService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCrawlTaskService 테스트")
class CreateCrawlTaskServiceTest {

    @Mock private CrawlTaskCommandFactory commandFactory;

    @Mock private CrawlTaskFacade facade;

    @Mock private CrawlTaskBundle mockBundle;

    @InjectMocks private CreateCrawlTaskService service;

    @Nested
    @DisplayName("execute() 단건 태스크 생성 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] META 태스크 생성")
        void shouldCreateMetaTask() {
            // Given
            CreateCrawlTaskCommand command = CreateCrawlTaskCommand.forMeta(1L, 1L);

            given(commandFactory.createBundle(command)).willReturn(mockBundle);

            // When
            service.execute(command);

            // Then
            then(commandFactory).should().createBundle(command);
            then(facade).should().persist(mockBundle);
        }

        @Test
        @DisplayName("[성공] MINI_SHOP 태스크 생성")
        void shouldCreateMiniShopTask() {
            // Given
            CreateCrawlTaskCommand command = CreateCrawlTaskCommand.forMiniShop(1L, 1L, 1L);

            given(commandFactory.createBundle(command)).willReturn(mockBundle);

            // When
            service.execute(command);

            // Then
            then(commandFactory).should().createBundle(command);
            then(facade).should().persist(mockBundle);
        }

        @Test
        @DisplayName("[성공] DETAIL 태스크 생성")
        void shouldCreateDetailTask() {
            // Given
            CreateCrawlTaskCommand command = CreateCrawlTaskCommand.forDetail(1L, 1L, 12345L);

            given(commandFactory.createBundle(command)).willReturn(mockBundle);

            // When
            service.execute(command);

            // Then
            then(commandFactory).should().createBundle(command);
            then(facade).should().persist(mockBundle);
        }

        @Test
        @DisplayName("[성공] OPTION 태스크 생성")
        void shouldCreateOptionTask() {
            // Given
            CreateCrawlTaskCommand command = CreateCrawlTaskCommand.forOption(1L, 1L, 12345L);

            given(commandFactory.createBundle(command)).willReturn(mockBundle);

            // When
            service.execute(command);

            // Then
            then(commandFactory).should().createBundle(command);
            then(facade).should().persist(mockBundle);
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
                            CreateCrawlTaskCommand.forDetail(1L, 1L, 100L),
                            CreateCrawlTaskCommand.forDetail(1L, 1L, 101L),
                            CreateCrawlTaskCommand.forOption(1L, 1L, 100L));

            given(commandFactory.createBundle(any(CreateCrawlTaskCommand.class)))
                    .willReturn(mockBundle);

            // When
            service.executeBatch(commands);

            // Then
            then(commandFactory).should(times(3)).createBundle(any(CreateCrawlTaskCommand.class));
            then(facade).should(times(3)).persist(mockBundle);
        }

        @Test
        @DisplayName("[성공] 일부 태스크 실패해도 나머지 계속 처리")
        void shouldContinueOnIndividualFailure() {
            // Given
            CreateCrawlTaskCommand command1 = CreateCrawlTaskCommand.forDetail(1L, 1L, 100L);
            CreateCrawlTaskCommand command2 = CreateCrawlTaskCommand.forDetail(1L, 1L, 101L);
            CreateCrawlTaskCommand command3 = CreateCrawlTaskCommand.forDetail(1L, 1L, 102L);
            List<CreateCrawlTaskCommand> commands = List.of(command1, command2, command3);

            CrawlTaskBundle bundle1 = org.mockito.Mockito.mock(CrawlTaskBundle.class);
            CrawlTaskBundle bundle2 = org.mockito.Mockito.mock(CrawlTaskBundle.class);
            CrawlTaskBundle bundle3 = org.mockito.Mockito.mock(CrawlTaskBundle.class);

            given(commandFactory.createBundle(command1)).willReturn(bundle1);
            given(commandFactory.createBundle(command2)).willReturn(bundle2);
            given(commandFactory.createBundle(command3)).willReturn(bundle3);

            // 두 번째 태스크 실패
            doThrow(new RuntimeException("Duplicate task")).when(facade).persist(bundle2);

            // When
            service.executeBatch(commands);

            // Then
            then(facade).should().persist(bundle1);
            then(facade).should().persist(bundle2);
            then(facade).should().persist(bundle3);
        }

        @Test
        @DisplayName("[성공] 빈 목록 처리")
        void shouldHandleEmptyList() {
            // Given
            List<CreateCrawlTaskCommand> commands = List.of();

            // When
            service.executeBatch(commands);

            // Then
            then(commandFactory).shouldHaveNoInteractions();
            then(facade).shouldHaveNoInteractions();
        }
    }
}
