package com.ryuqq.crawlinghub.application.crawl.orchestration.dto.command;

/**
 * MiniShopResultCommand Test Fixture
 *
 * @author Cascade
 * @since 2025-10-31
 */
public class MiniShopResultCommandFixture {

    private static final Long DEFAULT_TASK_ID = 1L;
    private static final String DEFAULT_RESPONSE_DATA = "{\"items\":[{\"itemNo\":\"12345\",\"name\":\"테스트상품\"}]}";

    /**
     * 기본 MiniShopResultCommand 생성
     *
     * @return MiniShopResultCommand
     */
    public static MiniShopResultCommand create() {
        return new MiniShopResultCommand(
            DEFAULT_TASK_ID,
            DEFAULT_RESPONSE_DATA
        );
    }

    /**
     * 특정 태스크 ID로 MiniShopResultCommand 생성
     *
     * @param taskId 태스크 ID
     * @return MiniShopResultCommand
     */
    public static MiniShopResultCommand createWithTaskId(Long taskId) {
        return new MiniShopResultCommand(
            taskId,
            DEFAULT_RESPONSE_DATA
        );
    }

    /**
     * 특정 응답 데이터로 MiniShopResultCommand 생성
     *
     * @param responseData 응답 데이터
     * @return MiniShopResultCommand
     */
    public static MiniShopResultCommand createWithResponseData(String responseData) {
        return new MiniShopResultCommand(
            DEFAULT_TASK_ID,
            responseData
        );
    }

    /**
     * 완전한 커스텀 MiniShopResultCommand 생성
     *
     * @param taskId       태스크 ID
     * @param responseData 응답 데이터
     * @return MiniShopResultCommand
     */
    public static MiniShopResultCommand createCustom(
        Long taskId,
        String responseData
    ) {
        return new MiniShopResultCommand(taskId, responseData);
    }
}
