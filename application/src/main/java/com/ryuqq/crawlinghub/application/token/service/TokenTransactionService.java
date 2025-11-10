package com.ryuqq.crawlinghub.application.token.service;

import com.ryuqq.crawlinghub.application.token.port.UserAgentTokenPort;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 토큰 관련 트랜잭션 처리 서비스
 * <p>
 * ⭐ Domain 객체(UserAgent)를 사용
 * - TokenAcquisitionManager에서 트랜잭션이 필요한 부분만 분리
 * - 각 메서드는 독립적인 짧은 트랜잭션으로 실행
 * - Spring AOP Proxy 제약사항 회피 (내부 호출 시 @Transactional 무시 방지)
 * </p>
 * <p>
 * ⚠️ Zero-Tolerance 규칙:
 * - 외부 API 호출은 트랜잭션 밖에서 (Manager에서 처리)
 * - 각 트랜잭션은 짧고 독립적으로 유지
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Service
public class TokenTransactionService {

    private final UserAgentTokenPort tokenPort;

    public TokenTransactionService(UserAgentTokenPort tokenPort) {
        this.tokenPort = tokenPort;
    }

    /**
     * [TX1] User-Agent 조회 (readOnly)
     * <p>
     * DB에서 최신 User-Agent 정보 로드
     * </p>
     *
     * @param userAgentId User-Agent ID
     * @return UserAgent Domain 객체
     */
    @Transactional(readOnly = true)
    public UserAgent loadUserAgent(Long userAgentId) {
        return tokenPort.findById(userAgentId);
    }

    /**
     * [TX2] User-Agent 저장 (토큰 포함)
     * <p>
     * 신규 토큰 발급 후 저장
     * </p>
     *
     * @param userAgent UserAgent Domain 객체
     */
    @Transactional
    public void saveUserAgent(UserAgent userAgent) {
        tokenPort.save(userAgent);
    }

    /**
     * [TX3] 사용 기록
     * <p>
     * 토큰 사용 통계 기록
     * </p>
     *
     * @param userAgent UserAgent Domain 객체
     */
    @Transactional
    public void recordUsage(UserAgent userAgent) {
        tokenPort.recordUsage(userAgent);
    }
}
