package com.ryuqq.crawlinghub.persistence.site;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.config.QueryDslConfig;
import com.ryuqq.crawlinghub.adapter.persistence.jpa.site.*;
import com.ryuqq.crawlinghub.domain.common.SiteType;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for SitePersistenceAdapter
 * Tests CQRS pattern implementation with Command and Query separation
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QueryDslConfig.class, SitePersistenceAdapter.class, SiteMapper.class, CrawlSiteQueryRepository.class})
class SitePersistenceAdapterTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SitePersistenceAdapter adapter;

    @Autowired
    private CrawlSiteJpaRepository jpaRepository;

    @Test
    @DisplayName("Command: save() - 새로운 사이트를 저장하고 ID가 생성된다")
    void shouldSaveNewSite() {
        // given
        CrawlSite newSite = CrawlSite.create(
                "Test Site",
                "https://api.test.com",
                SiteType.REST_API
        );

        // when
        CrawlSite saved = adapter.save(newSite);

        // then
        assertThat(saved.getSiteId()).isNotNull();
        assertThat(saved.getSiteName()).isEqualTo("Test Site");
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    @DisplayName("Command: delete() - 사이트를 삭제한다")
    void shouldDeleteSite() {
        // given
        CrawlSiteEntity entity = createSiteEntity("Delete Test", true);
        CrawlSiteEntity saved = jpaRepository.save(entity);
        SiteId siteId = new SiteId(saved.getSiteId());

        // when
        adapter.delete(siteId);

        // then
        assertThat(jpaRepository.existsById(saved.getSiteId())).isFalse();
    }

    @Test
    @DisplayName("Query: findById() - ID로 사이트를 조회한다")
    void shouldFindSiteById() {
        // given
        CrawlSiteEntity entity = createSiteEntity("Find By ID Test", true);
        CrawlSiteEntity saved = jpaRepository.save(entity);

        // when
        Optional<CrawlSite> found = adapter.findById(new SiteId(saved.getSiteId()));

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getSiteName()).isEqualTo("Find By ID Test");
    }

    @Test
    @DisplayName("Query: findActiveSites() - 활성화된 사이트 목록을 조회한다")
    void shouldFindActiveSites() {
        // given
        jpaRepository.save(createSiteEntity("Active Site 1", true));
        jpaRepository.save(createSiteEntity("Active Site 2", true));
        jpaRepository.save(createSiteEntity("Inactive Site", false));

        // when
        List<CrawlSite> activeSites = adapter.findActiveSites();

        // then
        assertThat(activeSites).hasSize(2);
        assertThat(activeSites).allMatch(CrawlSite::isActive);
    }

    @Test
    @DisplayName("Query: existsBySiteName() - 사이트명 중복을 확인한다")
    void shouldCheckSiteNameExists() {
        // given
        jpaRepository.save(createSiteEntity("Existing Site", true));

        // when
        boolean exists = adapter.existsBySiteName("Existing Site");
        boolean notExists = adapter.existsBySiteName("Non-Existing Site");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    private CrawlSiteEntity createSiteEntity(String siteName, boolean isActive) {
        return CrawlSiteEntity.builder()
                .siteName(siteName)
                .baseUrl("https://api." + siteName.toLowerCase().replace(" ", "") + ".com")
                .siteType(SiteType.REST_API)
                .isActive(isActive)
                .build();
    }

}
