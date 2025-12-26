package com.ryuqq.crawlinghub.application.crawl.processor;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ProcessingResult 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ProcessingResult 테스트")
class ProcessingResultTest {

    @Nested
    @DisplayName("팩토리 메서드 테스트")
    class FactoryMethods {

        @Test
        @DisplayName("[성공] withFollowUp - 후속 Task 포함")
        void shouldCreateWithFollowUp() {
            // Given
            List<CreateCrawlTaskCommand> commands =
                    List.of(CreateCrawlTaskCommand.forMiniShop(1L, 100L, "testSeller", 0L));

            // When
            ProcessingResult result = ProcessingResult.withFollowUp(commands, 10, 5);

            // Then
            assertThat(result.hasFollowUpTasks()).isTrue();
            assertThat(result.getFollowUpCommands()).hasSize(1);
            assertThat(result.getParsedItemCount()).isEqualTo(10);
            assertThat(result.getSavedItemCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("[성공] completed - 후속 Task 없음")
        void shouldCreateCompleted() {
            // When
            ProcessingResult result = ProcessingResult.completed(20, 15);

            // Then
            assertThat(result.hasFollowUpTasks()).isFalse();
            assertThat(result.getFollowUpCommands()).isEmpty();
            assertThat(result.getParsedItemCount()).isEqualTo(20);
            assertThat(result.getSavedItemCount()).isEqualTo(15);
        }

        @Test
        @DisplayName("[성공] empty - 빈 결과")
        void shouldCreateEmpty() {
            // When
            ProcessingResult result = ProcessingResult.empty();

            // Then
            assertThat(result.hasFollowUpTasks()).isFalse();
            assertThat(result.getFollowUpCommands()).isEmpty();
            assertThat(result.getParsedItemCount()).isEqualTo(0);
            assertThat(result.getSavedItemCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("[성공] null commands 처리")
        void shouldHandleNullCommands() {
            // When
            ProcessingResult result = ProcessingResult.withFollowUp(null, 5, 3);

            // Then
            assertThat(result.hasFollowUpTasks()).isFalse();
            assertThat(result.getFollowUpCommands()).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasFollowUpTasks() 테스트")
    class HasFollowUpTasks {

        @Test
        @DisplayName("[성공] 후속 Task 있음")
        void shouldReturnTrueWhenHasFollowUp() {
            // Given
            List<CreateCrawlTaskCommand> commands =
                    List.of(
                            CreateCrawlTaskCommand.forMiniShop(1L, 100L, "testSeller", 0L),
                            CreateCrawlTaskCommand.forMiniShop(1L, 100L, "testSeller", 1L));

            ProcessingResult result = ProcessingResult.withFollowUp(commands, 10, 10);

            // When & Then
            assertThat(result.hasFollowUpTasks()).isTrue();
            assertThat(result.getFollowUpCommands()).hasSize(2);
        }

        @Test
        @DisplayName("[성공] 후속 Task 없음")
        void shouldReturnFalseWhenNoFollowUp() {
            // Given
            ProcessingResult result = ProcessingResult.completed(5, 5);

            // When & Then
            assertThat(result.hasFollowUpTasks()).isFalse();
        }
    }
}
