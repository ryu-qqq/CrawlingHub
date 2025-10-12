package com.ryuqq.crawlinghub.domain.site;

import com.ryuqq.crawlinghub.domain.common.SiteType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrawlSiteTest {

    @Test
    @DisplayName("사이트 생성 시 필수 값 검증")
    void shouldCreateSiteWithRequiredFields() {
        // given
        String siteName = "Test Site";
        String baseUrl = "https://api.test.com";
        SiteType siteType = SiteType.REST_API;

        // when
        CrawlSite site = CrawlSite.create(siteName, baseUrl, siteType);

        // then
        assertThat(site.getSiteName()).isEqualTo(siteName);
        assertThat(site.getBaseUrl()).isEqualTo(baseUrl);
        assertThat(site.getSiteType()).isEqualTo(siteType);
        assertThat(site.isActive()).isTrue();
        assertThat(site.getSiteId()).isNull();
    }

    @Test
    @DisplayName("사이트 이름이 null이면 예외 발생")
    void shouldThrowExceptionWhenSiteNameIsNull() {
        // given
        String siteName = null;
        String baseUrl = "https://api.test.com";
        SiteType siteType = SiteType.REST_API;

        // when & then
        assertThatThrownBy(() -> CrawlSite.create(siteName, baseUrl, siteType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Site name cannot be null or blank");
    }

    @Test
    @DisplayName("사이트 이름이 빈 문자열이면 예외 발생")
    void shouldThrowExceptionWhenSiteNameIsBlank() {
        // given
        String siteName = "   ";
        String baseUrl = "https://api.test.com";
        SiteType siteType = SiteType.REST_API;

        // when & then
        assertThatThrownBy(() -> CrawlSite.create(siteName, baseUrl, siteType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Site name cannot be null or blank");
    }

    @Test
    @DisplayName("Base URL이 null이면 예외 발생")
    void shouldThrowExceptionWhenBaseUrlIsNull() {
        // given
        String siteName = "Test Site";
        String baseUrl = null;
        SiteType siteType = SiteType.REST_API;

        // when & then
        assertThatThrownBy(() -> CrawlSite.create(siteName, baseUrl, siteType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Base URL cannot be null or blank");
    }

    @Test
    @DisplayName("Base URL이 빈 문자열이면 예외 발생")
    void shouldThrowExceptionWhenBaseUrlIsBlank() {
        // given
        String siteName = "Test Site";
        String baseUrl = "";
        SiteType siteType = SiteType.REST_API;

        // when & then
        assertThatThrownBy(() -> CrawlSite.create(siteName, baseUrl, siteType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Base URL cannot be null or blank");
    }

    @Test
    @DisplayName("Site Type이 null이면 예외 발생")
    void shouldThrowExceptionWhenSiteTypeIsNull() {
        // given
        String siteName = "Test Site";
        String baseUrl = "https://api.test.com";
        SiteType siteType = null;

        // when & then
        assertThatThrownBy(() -> CrawlSite.create(siteName, baseUrl, siteType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Site type cannot be null");
    }

    @Test
    @DisplayName("사이트 비활성화 가능")
    void shouldDeactivateSite() {
        // given
        CrawlSite site = CrawlSite.create("Test", "https://api.test.com", SiteType.REST_API);

        // when
        site.deactivate();

        // then
        assertThat(site.isActive()).isFalse();
    }

    @Test
    @DisplayName("사이트 활성화 가능")
    void shouldActivateSite() {
        // given
        CrawlSite site = CrawlSite.create("Test", "https://api.test.com", SiteType.REST_API);
        site.deactivate();

        // when
        site.activate();

        // then
        assertThat(site.isActive()).isTrue();
    }

    @Test
    @DisplayName("reconstitute로 DB에서 복원 가능")
    void shouldReconstituteFromDatabase() {
        // given
        SiteId siteId = SiteId.of(1L);
        String siteName = "Existing Site";
        String baseUrl = "https://api.existing.com";
        SiteType siteType = SiteType.WEB_SCRAPING;
        boolean isActive = false;

        // when
        CrawlSite site = CrawlSite.reconstitute(siteId, siteName, baseUrl, siteType, isActive);

        // then
        assertThat(site.getSiteId()).isEqualTo(siteId);
        assertThat(site.getSiteName()).isEqualTo(siteName);
        assertThat(site.getBaseUrl()).isEqualTo(baseUrl);
        assertThat(site.getSiteType()).isEqualTo(siteType);
        assertThat(site.isActive()).isFalse();
    }

    @Test
    @DisplayName("getIdValue는 siteId가 null이면 null 반환")
    void shouldReturnNullWhenSiteIdIsNull() {
        // given
        CrawlSite site = CrawlSite.create("Test", "https://api.test.com", SiteType.REST_API);

        // when
        Long idValue = site.getIdValue();

        // then
        assertThat(idValue).isNull();
    }

    @Test
    @DisplayName("getIdValue는 siteId가 있으면 값 반환")
    void shouldReturnValueWhenSiteIdExists() {
        // given
        SiteId siteId = SiteId.of(100L);
        CrawlSite site = CrawlSite.reconstitute(siteId, "Test", "https://api.test.com", SiteType.REST_API, true);

        // when
        Long idValue = site.getIdValue();

        // then
        assertThat(idValue).isEqualTo(100L);
    }

    @Test
    @DisplayName("다양한 SiteType으로 사이트 생성 가능")
    void shouldCreateSitesWithDifferentTypes() {
        // when & then
        CrawlSite restApiSite = CrawlSite.create("REST API Site", "https://api.rest.com", SiteType.REST_API);
        assertThat(restApiSite.getSiteType()).isEqualTo(SiteType.REST_API);

        CrawlSite graphqlSite = CrawlSite.create("GraphQL Site", "https://api.graphql.com", SiteType.GRAPHQL);
        assertThat(graphqlSite.getSiteType()).isEqualTo(SiteType.GRAPHQL);

        CrawlSite scrapingSite = CrawlSite.create("Scraping Site", "https://web.scrape.com", SiteType.WEB_SCRAPING);
        assertThat(scrapingSite.getSiteType()).isEqualTo(SiteType.WEB_SCRAPING);

        CrawlSite rssSite = CrawlSite.create("RSS Site", "https://rss.feed.com", SiteType.RSS_FEED);
        assertThat(rssSite.getSiteType()).isEqualTo(SiteType.RSS_FEED);

        CrawlSite soapSite = CrawlSite.create("SOAP Site", "https://soap.api.com", SiteType.SOAP);
        assertThat(soapSite.getSiteType()).isEqualTo(SiteType.SOAP);

        CrawlSite customSite = CrawlSite.create("Custom Site", "https://custom.com", SiteType.CUSTOM);
        assertThat(customSite.getSiteType()).isEqualTo(SiteType.CUSTOM);
    }

}
