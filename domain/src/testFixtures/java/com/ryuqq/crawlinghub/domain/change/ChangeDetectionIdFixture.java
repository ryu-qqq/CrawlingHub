package com.ryuqq.crawlinghub.domain.change;

/**
 * ChangeDetectionId Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class ChangeDetectionIdFixture {

    private static final Long DEFAULT_ID = 1L;

    /**
     * 기본 ChangeDetectionId 생성
     *
     * @return ChangeDetectionId
     */
    public static ChangeDetectionId create() {
        return ChangeDetectionId.of(DEFAULT_ID);
    }

    /**
     * 지정된 ID로 ChangeDetectionId 생성
     *
     * @param id ID 값
     * @return ChangeDetectionId
     */
    public static ChangeDetectionId createWithId(Long id) {
        return ChangeDetectionId.of(id);
    }

    /**
     * null ID로 ChangeDetectionId 생성 (신규 엔티티용)
     *
     * @return null
     */
    public static ChangeDetectionId createNull() {
        return null;
    }
}
