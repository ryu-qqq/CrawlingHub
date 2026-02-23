package com.ryuqq.crawlinghub.domain.task.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CrawlTaskType Enum 단위 테스트
 *
 * <p>Kent Beck TDD - Red Phase
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlTaskType Enum 테스트")
class CrawlTaskTypeTest {

    @Test
    @DisplayName("MINI_SHOP 유형은 설명을 가짐")
    void shouldHaveDescriptionForMiniShop() {
        // given & when
        String description = CrawlTaskType.MINI_SHOP.getDescription();

        // then
        assertThat(description).isNotBlank();
        assertThat(description).contains("미니샵");
    }

    @Test
    @DisplayName("DETAIL 유형은 설명을 가짐")
    void shouldHaveDescriptionForDetail() {
        // given & when
        String description = CrawlTaskType.DETAIL.getDescription();

        // then
        assertThat(description).isNotBlank();
        assertThat(description).contains("상세");
    }

    @Test
    @DisplayName("OPTION 유형은 설명을 가짐")
    void shouldHaveDescriptionForOption() {
        // given & when
        String description = CrawlTaskType.OPTION.getDescription();

        // then
        assertThat(description).isNotBlank();
        assertThat(description).contains("옵션");
    }

    @Test
    @DisplayName("SEARCH 유형은 설명을 가짐")
    void shouldHaveDescriptionForSearch() {
        // given & when
        String description = CrawlTaskType.SEARCH.getDescription();

        // then
        assertThat(description).isNotBlank();
        assertThat(description).contains("검색");
    }

    @Test
    @DisplayName("모든 유형은 4개")
    void shouldHaveFourTypes() {
        // given & when & then
        assertThat(CrawlTaskType.values()).hasSize(4);
    }
}
