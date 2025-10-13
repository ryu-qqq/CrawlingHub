package com.ryuqq.crawlinghub.application.token.service;

import com.ryuqq.crawlinghub.application.token.port.CircuitBreakerPort;
import com.ryuqq.crawlinghub.application.token.port.UserAgentInfo;
import com.ryuqq.crawlinghub.application.token.port.UserAgentTokenPort;
import com.ryuqq.crawlinghub.domain.token.TokenAcquisitionException;
import com.ryuqq.crawlinghub.domain.token.TokenResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Token Transaction Service
 * 
 * 트랜잭션 관리를 전담하는 서비스
 * UseCase에서 내부 메서드 호출 시 Spring AOP 프록시 한계를 해결하기 위해 분리
 * 
 * 책임:
 * - DB 트랜잭션 경계 관리
 * - 짧은 트랜잭션 유지 (외부 API 호출 제외)
 * - 트랜잭션별 독립 실행
 *
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 * @since 2025-10-14
 */
@Service
public class TokenTransactionService {

    private final UserAgentTokenPort tokenPort;
    private final CircuitBreakerPort circuitBreakerPort;

    public TokenTransactionService(
            UserAgentTokenPort tokenPort,
            CircuitBreakerPort circuitBreakerPort) {
        this.tokenPort = tokenPort;
        this.circuitBreakerPort = circuitBreakerPort;
    }

    /**
     * User-Agent 및 Token 조회 (읽기 전용 트랜잭션)
     * 
     * 트랜잭션 특성:
     * - readOnly = true (최적화)
     * - 빠른 실행 (DB 조회만)
     * 
     * @param userAgentId User-Agent ID
     * @return User-Agent 정보 및 Token
     * @throws TokenAcquisitionException Circuit Breaker Open 또는 User-Agent 없음
     */
    @Transactional(readOnly = true)
    public UserAgentInfo loadUserAgentWithToken(Long userAgentId) {
        // Circuit Breaker 확인
        if (circuitBreakerPort.isOpen(userAgentId)) {
            throw new TokenAcquisitionException(
                    TokenAcquisitionException.Reason.CIRCUIT_BREAKER_OPEN
            );
        }

        // DB에서 User-Agent 및 Token 조회
        UserAgentInfo userAgentInfo = tokenPort.findActiveToken(userAgentId);
        if (userAgentInfo == null) {
            throw new TokenAcquisitionException(
                    TokenAcquisitionException.Reason.INVALID_USER_AGENT
            );
        }

        return userAgentInfo;
    }

    /**
     * 새 토큰 저장 및 성공 통계 업데이트 (쓰기 트랜잭션)
     * 
     * 트랜잭션 특성:
     * - 독립된 트랜잭션
     * - 빠른 실행 (DB 쓰기만)
     * - 외부 API 호출 후 실행됨
     * 
     * @param userAgentId User-Agent ID
     * @param token 새로 발급받은 토큰
     */
    @Transactional
    public void saveNewToken(Long userAgentId, TokenResponse token) {
        tokenPort.saveOrUpdateToken(
                userAgentId,
                token.accessToken(),
                token.tokenType(),
                token.expiresIn()
        );
        tokenPort.recordSuccess(userAgentId);
    }

    /**
     * 토큰 사용 통계 기록 (쓰기 트랜잭션)
     * 
     * 트랜잭션 특성:
     * - 독립된 트랜잭션
     * - 빠른 실행
     * 
     * @param userAgentId User-Agent ID
     */
    @Transactional
    public void recordTokenUsage(Long userAgentId) {
        tokenPort.recordUsage(userAgentId);
    }

    /**
     * 토큰 발급 실패 기록 (쓰기 트랜잭션)
     * 
     * 트랜잭션 특성:
     * - 독립된 트랜잭션
     * - 실패 시에도 통계는 저장됨
     * 
     * @param userAgentId User-Agent ID
     */
    @Transactional
    public void recordTokenFailure(Long userAgentId) {
        tokenPort.recordFailure(userAgentId);
    }
}
