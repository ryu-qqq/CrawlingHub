package com.ryuqq.crawlinghub.application.task.dto.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CreateCrawlTaskCommand 단위 테스트
 *
 * <p>CrawlTask 동적 생성 Command 유효성 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CreateCrawlTaskCommand 테스트")
class CreateCrawlTaskCommandTest {

    @Nested
    @DisplayName("compact 생성자 검증 테스트")
    class CompactConstructorValidation {

        @Test
        @DisplayName("[성공] 유효한 값으로 생성")
        void shouldCreateWithValidValues() {
            // When
            CreateCrawlTaskCommand command =
                    new CreateCrawlTaskCommand(
                            1L, 2L, "test-seller", CrawlTaskType.MINI_SHOP, 100L, null);

            // Then
            assertThat(command.crawlSchedulerId()).isEqualTo(1L);
            assertThat(command.sellerId()).isEqualTo(2L);
            assertThat(command.mustItSellerName()).isEqualTo("test-seller");
            assertThat(command.taskType()).isEqualTo(CrawlTaskType.MINI_SHOP);
            assertThat(command.targetId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("[실패] crawlSchedulerId가 null이면 예외")
        void shouldThrowWhenCrawlSchedulerIdIsNull() {
            // When / Then
            assertThatThrownBy(
                            () ->
                                    new CreateCrawlTaskCommand(
                                            null,
                                            2L,
                                            "seller",
                                            CrawlTaskType.MINI_SHOP,
                                            null,
                                            null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("crawlSchedulerId");
        }

        @Test
        @DisplayName("[실패] sellerId가 null이면 예외")
        void shouldThrowWhenSellerIdIsNull() {
            // When / Then
            assertThatThrownBy(
                            () ->
                                    new CreateCrawlTaskCommand(
                                            1L,
                                            null,
                                            "seller",
                                            CrawlTaskType.MINI_SHOP,
                                            null,
                                            null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sellerId");
        }

        @Test
        @DisplayName("[실패] mustItSellerName이 null이면 예외")
        void shouldThrowWhenMustItSellerNameIsNull() {
            // When / Then
            assertThatThrownBy(
                            () ->
                                    new CreateCrawlTaskCommand(
                                            1L, 2L, null, CrawlTaskType.MINI_SHOP, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("mustItSellerName");
        }

        @Test
        @DisplayName("[실패] mustItSellerName이 빈 문자열이면 예외")
        void shouldThrowWhenMustItSellerNameIsBlank() {
            // When / Then
            assertThatThrownBy(
                            () ->
                                    new CreateCrawlTaskCommand(
                                            1L, 2L, "  ", CrawlTaskType.MINI_SHOP, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("[실패] taskType이 null이면 예외")
        void shouldThrowWhenTaskTypeIsNull() {
            // When / Then
            assertThatThrownBy(() -> new CreateCrawlTaskCommand(1L, 2L, "seller", null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("taskType");
        }
    }

    @Nested
    @DisplayName("팩토리 메서드 테스트")
    class FactoryMethods {

        @Test
        @DisplayName("[성공] forMiniShop 팩토리로 MINI_SHOP 타입 생성")
        void shouldCreateMiniShopCommand() {
            // When
            CreateCrawlTaskCommand command =
                    CreateCrawlTaskCommand.forMiniShop(1L, 2L, "test-seller", 1L);

            // Then
            assertThat(command.taskType()).isEqualTo(CrawlTaskType.MINI_SHOP);
            assertThat(command.targetId()).isEqualTo(1L);
            assertThat(command.endpoint()).isNull();
        }

        @Test
        @DisplayName("[성공] forDetail 팩토리로 DETAIL 타입 생성")
        void shouldCreateDetailCommand() {
            // When
            CreateCrawlTaskCommand command =
                    CreateCrawlTaskCommand.forDetail(1L, 2L, "test-seller", 12345L);

            // Then
            assertThat(command.taskType()).isEqualTo(CrawlTaskType.DETAIL);
            assertThat(command.targetId()).isEqualTo(12345L);
        }

        @Test
        @DisplayName("[성공] forOption 팩토리로 OPTION 타입 생성")
        void shouldCreateOptionCommand() {
            // When
            CreateCrawlTaskCommand command =
                    CreateCrawlTaskCommand.forOption(1L, 2L, "test-seller", 99L);

            // Then
            assertThat(command.taskType()).isEqualTo(CrawlTaskType.OPTION);
            assertThat(command.targetId()).isEqualTo(99L);
        }

        @Test
        @DisplayName("[성공] forSearchNextPage 팩토리로 SEARCH 타입 생성")
        void shouldCreateSearchNextPageCommand() {
            // When
            CreateCrawlTaskCommand command =
                    CreateCrawlTaskCommand.forSearchNextPage(
                            1L, 2L, "test-seller", "https://api.example.com/next");

            // Then
            assertThat(command.taskType()).isEqualTo(CrawlTaskType.SEARCH);
            assertThat(command.endpoint()).isEqualTo("https://api.example.com/next");
            assertThat(command.targetId()).isNull();
        }

        @Test
        @DisplayName("[실패] of 팩토리는 mustItSellerName이 빈 문자열이므로 예외 발생")
        void shouldThrowWithOfFactoryDueToBlankSellerName() {
            // of() 팩토리는 mustItSellerName으로 빈 문자열("")을 전달하므로
            // compact 생성자의 isBlank() 검증에 걸려 IllegalArgumentException 발생
            assertThatThrownBy(() -> CreateCrawlTaskCommand.of(1L, 2L, CrawlTaskType.DETAIL, 123L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("mustItSellerName");
        }
    }
}
