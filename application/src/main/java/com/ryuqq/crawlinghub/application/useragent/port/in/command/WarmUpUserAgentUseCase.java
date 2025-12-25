package com.ryuqq.crawlinghub.application.useragent.port.in.command;

/**
 * UserAgent Pool Warm-up UseCase
 *
 * <p>DB에서 AVAILABLE 상태의 UserAgent를 Redis Pool에 로드합니다.
 *
 * <p><strong>Lazy Token Issuance 전략</strong>:
 *
 * <ul>
 *   <li>세션 없이 AVAILABLE 상태로 Pool에 추가
 *   <li>세션은 토큰 소비 시점에 Lazy 발급
 * </ul>
 *
 * <p><strong>사용 시점</strong>:
 *
 * <ul>
 *   <li>서버 시작 시
 *   <li>Pool 초기화 필요 시
 *   <li>관리자 수동 실행
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface WarmUpUserAgentUseCase {

    /**
     * DB에서 AVAILABLE 상태의 UserAgent를 Redis Pool에 로드
     *
     * @return Pool에 추가된 UserAgent 수
     */
    int warmUp();
}
