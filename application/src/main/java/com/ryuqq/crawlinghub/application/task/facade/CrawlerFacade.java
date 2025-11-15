package com.ryuqq.crawlinghub.application.task.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlResult;
import com.ryuqq.crawlinghub.application.task.port.out.HttpCrawlerPort;
import com.ryuqq.crawlinghub.application.token.component.TokenAcquisitionManager;
import com.ryuqq.crawlinghub.domain.task.Task;
import com.ryuqq.crawlinghub.domain.token.exception.TokenAcquisitionException;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;
import org.springframework.stereotype.Component;

/**
 * 크롤링 인프라 Facade
 * <p>
 * ⭐ TokenAcquisitionManager 통합:
 * - 레거시 코드 제거 (UserAgentPort, TokenManagerPort 불필요)
 * - TokenAcquisitionManager로 통합 관리
 * - UserAgent Domain 객체 활용
 * </p>
 * <p>
 * ⚠️ Transaction 경계:
 * - Facade는 @Transactional 없음
 * - TokenAcquisitionManager가 내부적으로 짧은 트랜잭션 관리
 * - 외부 API 호출은 트랜잭션 밖에서
 * </p>
 * <p>
 * 실행 흐름:
 * 1. TokenAcquisitionManager.acquireToken() → UserAgent 획득
 * 2. UserAgent에서 token, userAgentString 추출
 * 3. HTTP API 호출
 * 4. 응답을 지정된 타입으로 변환
 * </p>
 * <p>
 * 사용 예시:
 * <pre>{@code
 * CrawlResult<MiniShopOutput> result = crawlerFacade.execute(task, MiniShopOutput.class);
 * if (result.isSuccess()) {
 *     MiniShopOutput output = result.getData();
 *     // 처리 로직
 * }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class CrawlerFacade {

    private final TokenAcquisitionManager tokenAcquisitionManager;
    private final HttpCrawlerPort httpCrawlerPort;
    private final ObjectMapper objectMapper;

    /**
     * Facade 생성자
     *
     * @param tokenAcquisitionManager 토큰 획득 Manager
     * @param httpCrawlerPort         HTTP 크롤러 Port
     * @param objectMapper            JSON 파싱용 ObjectMapper
     */
    public CrawlerFacade(
        TokenAcquisitionManager tokenAcquisitionManager,
        HttpCrawlerPort httpCrawlerPort,
        ObjectMapper objectMapper
    ) {
        this.tokenAcquisitionManager = tokenAcquisitionManager;
        this.httpCrawlerPort = httpCrawlerPort;
        this.objectMapper = objectMapper;
    }

    /**
     * 크롤링 실행 (제네릭 타입 지원)
     * <p>
     * ⭐ TokenAcquisitionManager 사용:
     * - acquireToken() → UserAgent Domain 객체 반환
     * - UserAgent에서 token, userAgentString 추출
     * </p>
     * <p>
     * ⚠️ 예외 처리:
     * - TokenAcquisitionException: Pool 없음, Lock 실패, Rate Limit 등
     * - HttpException: API 호출 실패
     * - JsonProcessingException: JSON 파싱 실패
     * </p>
     *
     * @param task       실행할 Task
     * @param outputType 반환받을 Output 타입 (MiniShopOutput.class 등)
     * @param <T>        Output 타입
     * @return CrawlResult (성공/실패 + 데이터)
     */
    public <T> CrawlResult<T> execute(Task task, Class<T> outputType) {
        try {
            // 1. TokenAcquisitionManager로 UserAgent 획득
            //    - Redis Pool에서 LRU 선택
            //    - 분산 락 획득
            //    - DB 로드
            //    - 토큰 만료 확인 및 갱신
            //    - Rate Limiter 확인
            //    - 사용 기록
            UserAgent userAgent = tokenAcquisitionManager.acquireToken();

            // 2. UserAgent에서 정보 추출
            String userAgentString = userAgent.getUserAgentString();
            String token = extractTokenValue(userAgent);

            // 3. HTTP API 호출 (외부 API 호출은 트랜잭션 밖에서)
            HttpCrawlerPort.CrawlResponse response = httpCrawlerPort.execute(
                task.getRequestUrlValue(),
                userAgentString,
                token
            );

            // 4. 응답 처리
            if (response.isSuccess()) {
                T data = parseResponse(response.body(), outputType);
                return CrawlResult.success(data);
            } else {
                return CrawlResult.failure(
                    response.error(),
                    response.statusCode()
                );
            }

        } catch (TokenAcquisitionException e) {
            // TokenAcquisitionManager에서 발생한 예외 (Pool 없음, Lock 실패 등)
            return CrawlResult.failure(
                "토큰 획득 실패: " + e.getMessage(),
                null
            );

        } catch (Exception e) {
            // 기타 예외 (HTTP 호출 실패, JSON 파싱 실패 등)
            return CrawlResult.failure(e.getMessage(), null);
        }
    }

    /**
     * UserAgent Domain 객체에서 Token 값 추출
     * <p>
     * ⭐ Domain 메서드 활용:
     * - UserAgent.getCurrentToken() → Token VO 반환
     * - Token.getValue() → String 반환
     * </p>
     *
     * @param userAgent UserAgent Domain 객체
     * @return Token 값 (String)
     */
    private String extractTokenValue(UserAgent userAgent) {
        if (userAgent.getCurrentToken() == null) {
            throw new IllegalStateException(
                "UserAgent에 토큰이 없습니다: " + userAgent.getIdValue()
            );
        }

        return userAgent.getCurrentToken().getValue();
    }

    /**
     * JSON 응답을 지정된 타입으로 파싱
     *
     * @param json       JSON 문자열
     * @param outputType 반환할 타입
     * @param <T>        Output 타입
     * @return 파싱된 객체
     */
    private <T> T parseResponse(String json, Class<T> outputType) {
        try {
            return objectMapper.readValue(json, outputType);
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패: " + e.getMessage(), e);
        }
    }

}
