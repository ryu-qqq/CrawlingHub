package com.ryuqq.crawlinghub.domain.product;

/**
 * DataHash Test Fixture
 *
 * @author windsurf
 * @since 1.0.0
 */
public class DataHashFixture {

    private static final String DEFAULT_HASH = "a".repeat(64);
    private static final String ALTERNATIVE_HASH = "b".repeat(64);

    /**
     * 기본 DataHash 생성
     *
     * @return DataHash
     */
    public static DataHash create() {
        return DataHash.of(DEFAULT_HASH);
    }

    /**
     * 지정된 해시값으로 DataHash 생성
     *
     * @param hash 해시값 (64자)
     * @return DataHash
     */
    public static DataHash createWithHash(String hash) {
        return DataHash.of(hash);
    }

    /**
     * 대체 DataHash 생성 (변경 감지 테스트용)
     *
     * @return DataHash
     */
    public static DataHash createAlternative() {
        return DataHash.of(ALTERNATIVE_HASH);
    }

    /**
     * 특정 문자로 채워진 DataHash 생성
     *
     * @param character 채울 문자
     * @return DataHash
     */
    public static DataHash createWithCharacter(char character) {
        return DataHash.of(String.valueOf(character).repeat(64));
    }
}
