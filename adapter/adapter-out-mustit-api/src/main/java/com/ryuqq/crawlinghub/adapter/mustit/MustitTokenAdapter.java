package com.ryuqq.crawlinghub.adapter.mustit;

import com.ryuqq.crawlinghub.application.token.port.MustitTokenPort;
import com.ryuqq.crawlinghub.domain.token.TokenAcquisitionException;
import com.ryuqq.crawlinghub.domain.token.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 머스트잇 API 토큰 관리 어댑터
 * MustitTokenPort 구현체
 *
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 */
@Component
public class MustitTokenAdapter implements MustitTokenPort {

    private static final Logger LOG = LoggerFactory.getLogger(MustitTokenAdapter.class);

    private static final String TOKEN_ENDPOINT = "/v1/auth/token";
    private static final String VALIDATION_ENDPOINT = "/v1/auth/validate";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final WebClient webClient;

    /**
     * MustitTokenAdapter 생성자
     *
     * @param mustitWebClient 머스트잇 API WebClient
     */
    public MustitTokenAdapter(WebClient mustitWebClient) {
        this.webClient = mustitWebClient;
    }

    /**
     * 토큰 발급
     * 최대 3회 재시도 (2초, 4초 지수 백오프)
     */
    @Override
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2.0),
            retryFor = {WebClientResponseException.class}
    )
    public TokenResponse issueToken(String userAgent) {
        LOG.info("Issuing token for User-Agent: {}", userAgent);

        try {
            TokenApiResponse response = webClient.post()
                    .uri(TOKEN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("User-Agent", userAgent)
                    .bodyValue(IssueTokenRequest.of(userAgent))
                    .retrieve()
                    .onStatus(
                            HttpStatus.TOO_MANY_REQUESTS::equals,
                            clientResponse -> Mono.error(
                                    new TokenAcquisitionException(
                                            TokenAcquisitionException.Reason.RATE_LIMIT_EXCEEDED
                                    )
                            )
                    )
                    .onStatus(
                            HttpStatus.UNAUTHORIZED::equals,
                            clientResponse -> Mono.error(
                                    new TokenAcquisitionException(
                                            TokenAcquisitionException.Reason.INVALID_USER_AGENT
                                    )
                            )
                    )
                    .bodyToMono(TokenApiResponse.class)
                    .timeout(TIMEOUT)
                    .block();

            if (response == null) {
                throw new TokenAcquisitionException(
                        TokenAcquisitionException.Reason.TOKEN_EXPIRED
                );
            }

            LOG.info("Token issued successfully for User-Agent: {}", userAgent);
            return response.toDomain();

        } catch (TokenAcquisitionException e) {
            // onStatus에서 발생한 도메인 예외는 그대로 전파
            throw e;
        } catch (WebClientResponseException e) {
            LOG.error("Failed to issue token for User-Agent: {}, Status: {}, Body: {}",
                    userAgent, e.getStatusCode(), e.getResponseBodyAsString());
            throw mapException(e);
        } catch (Exception e) {
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                LOG.error("Timeout while issuing token for User-Agent: {}", userAgent, e);
                throw new TokenAcquisitionException(
                        TokenAcquisitionException.Reason.NETWORK_ERROR,
                        e
                );
            }
            LOG.error("Unexpected error while issuing token for User-Agent: {}", userAgent, e);
            throw new TokenAcquisitionException(
                    TokenAcquisitionException.Reason.NETWORK_ERROR,
                    e
            );
        }
    }

    /**
     * 토큰 갱신
     * 최대 3회 재시도
     */
    @Override
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2.0),
            retryFor = {WebClientResponseException.class}
    )
    public TokenResponse refreshToken(String refreshToken) {
        LOG.info("Refreshing token");

        try {
            TokenApiResponse response = webClient.post()
                    .uri(TOKEN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(RefreshTokenRequest.of(refreshToken))
                    .retrieve()
                    .onStatus(
                            HttpStatus.UNAUTHORIZED::equals,
                            clientResponse -> Mono.error(
                                    new TokenAcquisitionException(
                                            TokenAcquisitionException.Reason.TOKEN_EXPIRED
                                    )
                            )
                    )
                    .bodyToMono(TokenApiResponse.class)
                    .timeout(TIMEOUT)
                    .block();

            if (response == null) {
                throw new TokenAcquisitionException(
                        TokenAcquisitionException.Reason.TOKEN_EXPIRED
                );
            }

            LOG.info("Token refreshed successfully");
            return response.toDomain();

        } catch (TokenAcquisitionException e) {
            // onStatus에서 발생한 도메인 예외는 그대로 전파
            throw e;
        } catch (WebClientResponseException e) {
            LOG.error("Failed to refresh token, Status: {}, Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw mapException(e);
        } catch (Exception e) {
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                LOG.error("Timeout while refreshing token", e);
                throw new TokenAcquisitionException(
                        TokenAcquisitionException.Reason.NETWORK_ERROR,
                        e
                );
            }
            LOG.error("Unexpected error while refreshing token", e);
            throw new TokenAcquisitionException(
                    TokenAcquisitionException.Reason.NETWORK_ERROR,
                    e
            );
        }
    }

    /**
     * 토큰 유효성 검증
     */
    @Override
    public boolean validateToken(String accessToken) {
        LOG.debug("Validating token");

        Boolean isValid = webClient.post()
                .uri(VALIDATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Boolean.class)
                .timeout(TIMEOUT)
                .onErrorReturn(false)
                .block();

        boolean valid = isValid != null && isValid;
        LOG.debug("Token validation result: {}", valid);
        return valid;
    }

    /**
     * WebClient 예외를 도메인 예외로 매핑
     */
    private TokenAcquisitionException mapException(WebClientResponseException e) {
        HttpStatus status = (HttpStatus) e.getStatusCode();

        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            return new TokenAcquisitionException(
                    TokenAcquisitionException.Reason.RATE_LIMIT_EXCEEDED,
                    e
            );
        } else if (status == HttpStatus.UNAUTHORIZED) {
            return new TokenAcquisitionException(
                    TokenAcquisitionException.Reason.INVALID_USER_AGENT,
                    e
            );
        } else if (status.is5xxServerError()) {
            return new TokenAcquisitionException(
                    TokenAcquisitionException.Reason.API_ERROR,
                    e
            );
        } else {
            return new TokenAcquisitionException(
                    TokenAcquisitionException.Reason.TOKEN_EXPIRED,
                    e
            );
        }
    }
}
