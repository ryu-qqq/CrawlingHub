package com.ryuqq.crawlinghub.domain.crawl.task.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("META 유형은 설명을 가짐")
    void shouldHaveDescriptionForMeta() {
        // given & when
        String description = CrawlTaskType.META.getDescription();

        // then
        assertThat(description).isNotBlank();
        assertThat(description).contains("메타");
    }

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
    @DisplayName("모든 유형은 4개")
    void shouldHaveFourTypes() {
        // given & when & then
        assertThat(CrawlTaskType.values()).hasSize(4);
    }
}
