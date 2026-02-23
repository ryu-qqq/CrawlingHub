package com.ryuqq.crawlinghub.adapter.out.http.session;

import com.ryuqq.crawlinghub.adapter.out.http.config.SessionTokenProperties;
import com.ryuqq.crawlinghub.application.useragent.dto.session.SessionToken;
import com.ryuqq.crawlinghub.application.useragent.port.out.command.SessionTokenPort;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * WebClient 기반 세션 토큰 발급 Adapter
 *
 * <p>외부 사이트에 접속하여 Cookie 기반 세션 토큰을 발급받습니다.
 *
 * <p><strong>동작 흐름</strong>:
 *
 * <ol>
 *   <li>타겟 URL에 User-Agent 헤더 포함 GET 요청
 *   <li>Response Set-Cookie 헤더에서 세션 토큰 추출
 *   <li>Cookie expires/max-age에서 만료 시간 파싱
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class WebClientSessionTokenAdapter implements SessionTokenPort {

    private static final Logger log = LoggerFactory.getLogger(WebClientSessionTokenAdapter.class);

    private static final DateTimeFormatter COOKIE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

    private final WebClient webClient;
    private final SessionTokenProperties properties;
    private final Clock clock;

    public WebClientSessionTokenAdapter(
            WebClient webClient, SessionTokenProperties properties, Clock clock) {
        this.webClient = webClient;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public Optional<SessionToken> issueSessionToken(String userAgentValue) {
        try {
            return webClient
                    .get()
                    .uri(properties.getTargetUrl())
                    .header(HttpHeaders.USER_AGENT, userAgentValue)
                    .header(
                            HttpHeaders.ACCEPT,
                            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .exchangeToMono(this::extractSessionToken)
                    .blockOptional()
                    .flatMap(token -> token);

        } catch (Exception e) {
            log.error(
                    "세션 발급 중 오류 발생: {} for UserAgent '{}'",
                    e.getMessage(),
                    truncateUserAgent(userAgentValue));
            return Optional.empty();
        }
    }

    private Mono<Optional<SessionToken>> extractSessionToken(ClientResponse response) {
        if (!response.statusCode().is2xxSuccessful()) {
            log.warn(
                    "세션 발급 실패: HTTP {} for URL '{}'",
                    response.statusCode().value(),
                    properties.getTargetUrl());
            return Mono.just(Optional.empty());
        }

        // 1. ResponseCookie에서 추출 시도
        MultiValueMap<String, ResponseCookie> cookies = response.cookies();
        List<ResponseCookie> sessionCookies = cookies.get(properties.getSessionCookieName());

        // nid, mustit_uid 쿠키 추출
        String nid = extractCookieValue(cookies, "nid");
        String mustitUid = extractCookieValue(cookies, "mustit_uid");

        if (sessionCookies != null && !sessionCookies.isEmpty()) {
            ResponseCookie cookie = sessionCookies.get(0);
            String token = cookie.getValue();
            Instant expiresAt = calculateExpiresAt(cookie);

            log.info(
                    "세션 토큰 발급 성공 (ResponseCookie): token={}, nid={}, mustitUid={}, expiresAt={}",
                    truncateToken(token),
                    nid != null ? "있음" : "없음",
                    mustitUid != null ? "있음" : "없음",
                    expiresAt);
            return Mono.just(Optional.of(new SessionToken(token, nid, mustitUid, expiresAt)));
        }

        // 2. Set-Cookie 헤더에서 직접 추출 시도
        List<String> setCookieHeaders = response.headers().header(HttpHeaders.SET_COOKIE);

        // Set-Cookie 헤더에서 nid, mustit_uid 추출 (ResponseCookie에서 못 찾은 경우)
        if (nid == null) {
            nid = extractCookieFromHeaders(setCookieHeaders, "nid");
        }
        if (mustitUid == null) {
            mustitUid = extractCookieFromHeaders(setCookieHeaders, "mustit_uid");
        }

        for (String setCookie : setCookieHeaders) {
            if (setCookie.contains(properties.getSessionCookieName())) {
                String token = extractTokenValue(setCookie);
                if (token != null) {
                    Instant expiresAt = extractExpiresAt(setCookie);
                    log.info(
                            "세션 토큰 발급 성공 (Set-Cookie): token={}, nid={}, mustitUid={},"
                                    + " expiresAt={}",
                            truncateToken(token),
                            nid != null ? "있음" : "없음",
                            mustitUid != null ? "있음" : "없음",
                            expiresAt);
                    return Mono.just(
                            Optional.of(new SessionToken(token, nid, mustitUid, expiresAt)));
                }
            }
        }

        log.warn("세션 토큰을 찾을 수 없음: cookieName='{}'", properties.getSessionCookieName());
        return Mono.just(Optional.empty());
    }

    private String extractCookieValue(
            MultiValueMap<String, ResponseCookie> cookies, String cookieName) {
        List<ResponseCookie> targetCookies = cookies.get(cookieName);
        if (targetCookies != null && !targetCookies.isEmpty()) {
            return targetCookies.get(0).getValue();
        }
        return null;
    }

    private String extractCookieFromHeaders(List<String> setCookieHeaders, String cookieName) {
        for (String setCookie : setCookieHeaders) {
            if (setCookie.startsWith(cookieName + "=")) {
                String[] parts = setCookie.split(";");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (trimmed.startsWith(cookieName + "=")) {
                        return trimmed.substring((cookieName + "=").length());
                    }
                }
            }
        }
        return null;
    }

    private Instant calculateExpiresAt(ResponseCookie cookie) {
        Duration maxAge = cookie.getMaxAge();
        if (!maxAge.isNegative() && !maxAge.isZero()) {
            return clock.instant().plus(maxAge);
        }
        // 기본값: 설정된 시간
        return clock.instant().plus(Duration.ofHours(properties.getDefaultSessionDurationHours()));
    }

    private String extractTokenValue(String setCookie) {
        String cookieName = properties.getSessionCookieName();
        String[] parts = setCookie.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith(cookieName + "=")) {
                return trimmed.substring((cookieName + "=").length());
            }
        }
        return null;
    }

    private Instant extractExpiresAt(String setCookie) {
        String lowerCase = setCookie.toLowerCase(Locale.ENGLISH);

        // max-age 우선
        int maxAgeIdx = lowerCase.indexOf("max-age=");
        if (maxAgeIdx != -1) {
            try {
                String afterMaxAge = setCookie.substring(maxAgeIdx + 8);
                String maxAgeStr = afterMaxAge.split(";")[0].trim();
                long maxAge = Long.parseLong(maxAgeStr);
                return clock.instant().plusSeconds(maxAge);
            } catch (NumberFormatException e) {
                log.debug("max-age 파싱 실패: {}", e.getMessage());
            }
        }

        // expires 파싱
        int expiresIdx = lowerCase.indexOf("expires=");
        if (expiresIdx != -1) {
            try {
                String afterExpires = setCookie.substring(expiresIdx + 8);
                String expiresStr = afterExpires.split(";")[0].trim();
                ZonedDateTime zdt = ZonedDateTime.parse(expiresStr, COOKIE_DATE_FORMATTER);
                return zdt.toInstant();
            } catch (Exception e) {
                log.debug("expires 파싱 실패: {}", e.getMessage());
            }
        }

        // 기본값
        return clock.instant().plus(Duration.ofHours(properties.getDefaultSessionDurationHours()));
    }

    private String truncateUserAgent(String userAgent) {
        if (userAgent == null) {
            return "null";
        }
        return userAgent.length() > 50 ? userAgent.substring(0, 50) + "..." : userAgent;
    }

    private String truncateToken(String token) {
        if (token == null) {
            return "null";
        }
        return token.length() > 10 ? token.substring(0, 10) + "..." : token;
    }
}
