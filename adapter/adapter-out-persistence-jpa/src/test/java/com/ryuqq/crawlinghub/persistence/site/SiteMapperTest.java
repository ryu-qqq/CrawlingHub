package com.ryuqq.crawlinghub.persistence.site;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.site.CrawlSiteEntity;
import com.ryuqq.crawlinghub.adapter.persistence.jpa.site.SiteMapper;
import com.ryuqq.crawlinghub.domain.common.SiteType;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for SiteMapper
 * Tests Domain <-> Entity conversion
 */
class SiteMapperTest {

    private final SiteMapper mapper = new SiteMapper();

    @Test
    @DisplayName("Domain을 Entity로 변환한다")
    void shouldConvertDomainToEntity() {
        // given
        CrawlSite domain = CrawlSite.reconstitute(
                new SiteId(1L),
                "Test Site",
                "https://api.test.com",
                SiteType.REST_API,
                true
        );

        // when
        CrawlSiteEntity entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getSiteId()).isEqualTo(1L);
        assertThat(entity.getSiteName()).isEqualTo("Test Site");
        assertThat(entity.getBaseUrl()).isEqualTo("https://api.test.com");
        assertThat(entity.getSiteType()).isEqualTo(SiteType.REST_API);
        assertThat(entity.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Entity를 Domain으로 변환한다")
    void shouldConvertEntityToDomain() {
        // given
        CrawlSiteEntity entity = CrawlSiteEntity.builder()
                .siteId(1L)
                .siteName("Test Site")
                .baseUrl("https://api.test.com")
                .siteType(SiteType.REST_API)
                .isActive(true)
                .build();

        // when
        CrawlSite domain = mapper.toDomain(entity);

        // then
        assertThat(domain.getSiteId().value()).isEqualTo(1L);
        assertThat(domain.getSiteName()).isEqualTo("Test Site");
        assertThat(domain.getBaseUrl()).isEqualTo("https://api.test.com");
        assertThat(domain.getSiteType()).isEqualTo(SiteType.REST_API);
        assertThat(domain.isActive()).isTrue();
    }

    @Test
    @DisplayName("새로운 Domain(ID null)을 Entity로 변환한다")
    void shouldConvertNewDomainToEntity() {
        // given
        CrawlSite domain = CrawlSite.create(
                "New Site",
                "https://api.new.com",
                SiteType.REST_API
        );

        // when
        CrawlSiteEntity entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getSiteId()).isNull();
        assertThat(entity.getSiteName()).isEqualTo("New Site");
        assertThat(entity.getIsActive()).isTrue();
    }

}
