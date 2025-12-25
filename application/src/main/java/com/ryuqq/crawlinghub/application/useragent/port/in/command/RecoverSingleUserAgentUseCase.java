package com.ryuqq.crawlinghub.application.useragent.port.in.command;

/**
 * 개별 UserAgent 복구 UseCase
 *
 * <p>특정 SUSPENDED 상태의 UserAgent를 즉시 복구합니다. 일반 복구와 달리 시간 조건을 무시합니다.
 *
 * <p><strong>복구 동작</strong>:
 *
 * <ul>
 *   <li>Health Score → 70 리셋
 *   <li>tokens → 80 리셋
 *   <li>상태 → AVAILABLE
 *   <li>세션은 Lazy 발급
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RecoverSingleUserAgentUseCase {

    /**
     * 특정 UserAgent 즉시 복구
     *
     * @param userAgentId UserAgent ID
     * @throws com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentNotFoundException
     *     UserAgent가 없는 경우
     * @throws com.ryuqq.crawlinghub.domain.useragent.exception.InvalidUserAgentStateException 복구
     *     불가능한 상태인 경우
     */
    void execute(long userAgentId);
}
