package com.ryuqq.crawlinghub.domain.task;

/**
 * CrawlTaskId Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class CrawlTaskIdFixture {

    private static final Long DEFAULT_ID = 1L;

    /**
     * 기본 CrawlTaskId 생성
     *
     * @return CrawlTaskId
     */
    public static CrawlTaskId create() {
        return CrawlTaskId.of(DEFAULT_ID);
    }

    /**
     * 지정된 ID로 CrawlTaskId 생성
     *
     * @param id ID 값
     * @return CrawlTaskId
     */
    public static CrawlTaskId createWithId(Long id) {
        return CrawlTaskId.of(id);
    }

    /**
     * null ID로 CrawlTaskId 생성 (신규 엔티티용)
     *
     * @return null
     */
    public static CrawlTaskId createNull() {
        return null;
    }
}
