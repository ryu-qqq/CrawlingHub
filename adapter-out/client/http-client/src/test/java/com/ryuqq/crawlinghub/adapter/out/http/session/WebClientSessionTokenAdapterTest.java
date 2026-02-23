package com.ryuqq.crawlinghub.adapter.out.http.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ryuqq.crawlinghub.adapter.out.http.config.SessionTokenProperties;
import com.ryuqq.crawlinghub.application.useragent.dto.session.SessionToken;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * WebClientSessionTokenAdapter 단위 테스트
 *
 * <p>WebClient를 모킹하여 세션 토큰 발급 로직을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebClientSessionTokenAdapter 단위 테스트")
class WebClientSessionTokenAdapterTest {

    @Mock private WebClient webClient;

    private SessionTokenProperties properties;
    private Clock clock;
    private WebClientSessionTokenAdapter adapter;

    @BeforeEach
    void setUp() {
        properties = new SessionTokenProperties();
        properties.setTargetUrl("https://m.web.mustit.co.kr/");
        properties.setSessionCookieName("PHPSESSID");
        properties.setDefaultSessionDurationHours(2);

        // 고정 시간 Clock
        Instant fixedNow = Instant.parse("2026-02-23T00:00:00Z");
        clock = Clock.fixed(fixedNow, ZoneId.of("UTC"));

        adapter = new WebClientSessionTokenAdapter(webClient, properties, clock);
    }

    @Nested
    @DisplayName("issueSessionToken - 성공 케이스")
    class IssueSessionTokenSuccessTest {

        @Test
        @DisplayName("ResponseCookie에서 세션 토큰을 발급한다")
        void issueSessionToken_withResponseCookie_returnsSessionToken() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";
            String tokenValue = "test-session-token-123";

            ResponseCookie sessionCookie =
                    ResponseCookie.from("PHPSESSID", tokenValue).maxAge(7200).build();

            MultiValueMap<String, ResponseCookie> cookies = new LinkedMultiValueMap<>();
            cookies.add("PHPSESSID", sessionCookie);

            ClientResponse mockResponse =
                    ClientResponse.create(HttpStatus.OK).cookies(c -> c.addAll(cookies)).build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isPresent();
            SessionToken token = result.get();
            assertThat(token.token()).isEqualTo(tokenValue);
            assertThat(token.expiresAt()).isAfter(clock.instant());
        }

        @Test
        @DisplayName("Set-Cookie 헤더에서 세션 토큰을 발급한다")
        void issueSessionToken_withSetCookieHeader_returnsSessionToken() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";
            String tokenValue = "header-session-token-456";

            // ResponseCookie가 없고 Set-Cookie 헤더로만 토큰을 제공
            ClientResponse mockResponse =
                    ClientResponse.create(HttpStatus.OK)
                            .header(
                                    HttpHeaders.SET_COOKIE,
                                    "PHPSESSID=" + tokenValue + "; Max-Age=7200; Path=/; HttpOnly")
                            .build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().token()).isEqualTo(tokenValue);
        }

        @Test
        @DisplayName("Set-Cookie에 expires 속성이 있으면 해당 시간을 만료시간으로 설정한다")
        void issueSessionToken_withExpiresAttribute_setsCorrectExpiresAt() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";
            String tokenValue = "expires-session-token";

            ClientResponse mockResponse =
                    ClientResponse.create(HttpStatus.OK)
                            .header(
                                    HttpHeaders.SET_COOKIE,
                                    "PHPSESSID="
                                            + tokenValue
                                            + "; expires=Mon, 24 Feb 2026 00:00:00 GMT; Path=/;"
                                            + " HttpOnly")
                            .build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isPresent();
            // expires가 미래이므로 expiresAt이 현재보다 이후여야 함
            assertThat(result.get().expiresAt()).isAfter(clock.instant());
        }
    }

    @Nested
    @DisplayName("issueSessionToken - 실패 케이스")
    class IssueSessionTokenFailureTest {

        @Test
        @DisplayName("HTTP 응답이 4xx일 때 빈 Optional을 반환한다")
        void issueSessionToken_whenHttpError_returnsEmpty() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";

            ClientResponse mockResponse = ClientResponse.create(HttpStatus.FORBIDDEN).build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("세션 쿠키가 없을 때 빈 Optional을 반환한다")
        void issueSessionToken_whenNoCookie_returnsEmpty() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";

            // 세션 쿠키 없는 성공 응답
            ClientResponse mockResponse =
                    ClientResponse.create(HttpStatus.OK)
                            .header("Content-Type", "text/html")
                            .build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("WebClient 예외 발생 시 빈 Optional을 반환한다")
        void issueSessionToken_whenWebClientThrows_returnsEmpty() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";

            WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);

            when(webClient.get()).thenReturn(uriSpec);
            when(uriSpec.uri(anyString())).thenReturn(headersSpec);
            when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
            when(headersSpec.exchangeToMono(any())).thenThrow(new RuntimeException("네트워크 오류"));

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("issueSessionToken - max-age 파싱 테스트")
    class MaxAgeParsingTest {

        @Test
        @DisplayName("Set-Cookie max-age를 파싱하여 만료시간을 설정한다")
        void issueSessionToken_withMaxAge_setsCorrectExpiresAt() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";
            String tokenValue = "maxage-session-token";
            long maxAgeSeconds = 3600L;

            ClientResponse mockResponse =
                    ClientResponse.create(HttpStatus.OK)
                            .header(
                                    HttpHeaders.SET_COOKIE,
                                    "PHPSESSID="
                                            + tokenValue
                                            + "; max-age="
                                            + maxAgeSeconds
                                            + "; Path=/")
                            .build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isPresent();
            // max-age가 3600초이므로 현재 + 3600초 = 만료시간
            Instant expectedExpiry = clock.instant().plusSeconds(maxAgeSeconds);
            assertThat(result.get().expiresAt()).isEqualTo(expectedExpiry);
        }
    }

    @Nested
    @DisplayName("issueSessionToken - nid/mustit_uid 쿠키 추출 테스트")
    class AdditionalCookieExtractionTest {

        @Test
        @DisplayName("ResponseCookie에서 nid와 mustit_uid 쿠키가 함께 있을 때 세션 토큰을 발급한다")
        void issueSessionToken_withNidAndMustitUidResponseCookies_returnsSessionToken() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";
            String tokenValue = "session-with-extra-cookies";

            ResponseCookie sessionCookie =
                    ResponseCookie.from("PHPSESSID", tokenValue).maxAge(7200).build();
            ResponseCookie nidCookie =
                    ResponseCookie.from("nid", "nid-value-123").maxAge(7200).build();
            ResponseCookie mustitUidCookie =
                    ResponseCookie.from("mustit_uid", "uid-value-456").maxAge(7200).build();

            MultiValueMap<String, ResponseCookie> cookies = new LinkedMultiValueMap<>();
            cookies.add("PHPSESSID", sessionCookie);
            cookies.add("nid", nidCookie);
            cookies.add("mustit_uid", mustitUidCookie);

            ClientResponse mockResponse =
                    ClientResponse.create(HttpStatus.OK).cookies(c -> c.addAll(cookies)).build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().token()).isEqualTo(tokenValue);
        }

        @Test
        @DisplayName("ResponseCookie maxAge가 0이하이면 기본 만료시간을 사용한다")
        void issueSessionToken_withZeroMaxAge_usesDefaultExpiresAt() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";
            String tokenValue = "zero-maxage-token";

            // maxAge -1은 ResponseCookie의 "infinite" 표시 (Duration.MINUS_ONE)
            ResponseCookie sessionCookie = ResponseCookie.from("PHPSESSID", tokenValue).build();

            MultiValueMap<String, ResponseCookie> cookies = new LinkedMultiValueMap<>();
            cookies.add("PHPSESSID", sessionCookie);

            ClientResponse mockResponse =
                    ClientResponse.create(HttpStatus.OK).cookies(c -> c.addAll(cookies)).build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isPresent();
            // 기본값: 현재 시간 + defaultSessionDurationHours(2시간)
            Instant expectedExpiry = clock.instant().plusSeconds(2 * 3600L);
            assertThat(result.get().expiresAt()).isEqualTo(expectedExpiry);
        }

        @Test
        @DisplayName("Set-Cookie 헤더에서 nid 쿠키도 함께 추출한다")
        void issueSessionToken_withSetCookieIncludingNid_extractsNidAlso() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";
            String tokenValue = "session-with-nid-header";

            // ResponseCookie 없이 Set-Cookie 헤더로만 제공
            ClientResponse mockResponse =
                    ClientResponse.create(HttpStatus.OK)
                            .header(
                                    HttpHeaders.SET_COOKIE,
                                    "PHPSESSID=" + tokenValue + "; Max-Age=7200; Path=/; HttpOnly")
                            .header(HttpHeaders.SET_COOKIE, "nid=nid-header-val; Path=/")
                            .header(HttpHeaders.SET_COOKIE, "mustit_uid=uid-header-val; Path=/")
                            .build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().token()).isEqualTo(tokenValue);
        }

        @Test
        @DisplayName("Set-Cookie 헤더에 max-age 파싱 실패 시 기본 만료시간을 사용한다")
        void issueSessionToken_withInvalidMaxAge_usesDefaultExpiresAt() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";
            String tokenValue = "invalid-maxage-token";

            // max-age에 숫자가 아닌 값 설정
            ClientResponse mockResponse =
                    ClientResponse.create(HttpStatus.OK)
                            .header(
                                    HttpHeaders.SET_COOKIE,
                                    "PHPSESSID="
                                            + tokenValue
                                            + "; max-age=INVALID; Path=/; HttpOnly")
                            .build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isPresent();
            // 파싱 실패 시 expires 없으면 기본값
            assertThat(result.get().expiresAt()).isAfter(clock.instant());
        }

        @Test
        @DisplayName("긴 UserAgent 문자열로 예외 발생 시 Empty를 반환하며 로그에 truncate된다")
        void issueSessionToken_withLongUserAgentOnException_returnsEmpty() {
            // given
            // 50자 초과 User-Agent
            String longUserAgent =
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/537.36"
                            + " (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";

            WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);

            when(webClient.get()).thenReturn(uriSpec);
            when(uriSpec.uri(anyString())).thenReturn(headersSpec);
            when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
            when(headersSpec.exchangeToMono(any())).thenThrow(new RuntimeException("연결 오류"));

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(longUserAgent);

            // then - 예외 처리 후 Empty 반환 (truncateUserAgent 호출 경로)
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("issueSessionToken - expires 파싱 실패 시 기본값 테스트")
    class ExpiresParsingFallbackTest {

        @Test
        @DisplayName("expires 형식이 잘못된 경우 기본 만료시간을 사용한다")
        void issueSessionToken_withInvalidExpiresFormat_usesDefaultExpiresAt() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";
            String tokenValue = "invalid-expires-token";

            ClientResponse mockResponse =
                    ClientResponse.create(HttpStatus.OK)
                            .header(
                                    HttpHeaders.SET_COOKIE,
                                    "PHPSESSID="
                                            + tokenValue
                                            + "; expires=INVALID-DATE-FORMAT; Path=/; HttpOnly")
                            .build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isPresent();
            // 파싱 실패 시 기본값 사용 (현재 + 2시간)
            Instant expectedExpiry = clock.instant().plusSeconds(2 * 3600L);
            assertThat(result.get().expiresAt()).isEqualTo(expectedExpiry);
        }

        @Test
        @DisplayName("Set-Cookie 헤더에 세션 쿠키 이름이 포함되지만 값이 없으면 빈 Optional을 반환한다")
        void issueSessionToken_withSetCookieButNoTokenValue_returnsEmpty() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";

            // PHPSESSID가 포함되지만 '='가 없는 헤더 형태로 extractTokenValue가 null을 반환하도록
            // 세션 쿠키 이름이 포함되지만 다른 쿠키 이름에 포함된 경우
            ClientResponse mockResponse =
                    ClientResponse.create(HttpStatus.OK)
                            .header(
                                    HttpHeaders.SET_COOKIE,
                                    "OTHER_PHPSESSID_KEY=value; Path=/; HttpOnly")
                            .build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then - PHPSESSID 자체가 아닌 OTHER_PHPSESSID_KEY이므로 매칭 안 됨
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("issueSessionToken - null UserAgent 처리 테스트")
    class NullUserAgentTest {

        @Test
        @DisplayName("null UserAgent로 예외 발생 시 Empty를 반환한다")
        void issueSessionToken_withNullUserAgent_returnsEmpty() {
            // given
            String nullUserAgent = null;

            WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);

            when(webClient.get()).thenReturn(uriSpec);
            when(uriSpec.uri(anyString())).thenReturn(headersSpec);
            when(headersSpec.header(anyString(), any())).thenReturn(headersSpec);
            when(headersSpec.exchangeToMono(any())).thenThrow(new RuntimeException("null UA 오류"));

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(nullUserAgent);

            // then - truncateUserAgent(null) 경로 커버
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("짧은 UserAgent(50자 이하)로 예외 발생 시 Empty를 반환한다")
        void issueSessionToken_withShortUserAgent_returnsEmpty() {
            // given
            // 50자 이하 User-Agent (truncateUserAgent에서 그대로 반환 경로)
            String shortUserAgent = "ShortUA/1.0";

            WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);

            when(webClient.get()).thenReturn(uriSpec);
            when(uriSpec.uri(anyString())).thenReturn(headersSpec);
            when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
            when(headersSpec.exchangeToMono(any())).thenThrow(new RuntimeException("짧은 UA 오류"));

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(shortUserAgent);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("issueSessionToken - expires 파싱 성공 테스트")
    class ExpiresParsingSuccessTest {

        @Test
        @DisplayName("max-age 없이 expires만 있는 경우 expires를 파싱하여 만료시간을 설정한다")
        void issueSessionToken_withOnlyExpiresNoMaxAge_parsesExpiresCorrectly() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";
            String tokenValue = "expires-only-token";

            // max-age 없이 expires만 있는 Set-Cookie (extractExpiresAt의 expires 파싱 성공 경로)
            ClientResponse mockResponse =
                    ClientResponse.create(HttpStatus.OK)
                            .header(
                                    HttpHeaders.SET_COOKIE,
                                    "PHPSESSID="
                                            + tokenValue
                                            + "; expires=Tue, 24 Feb 2026 00:00:00 GMT; Path=/")
                            .build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isPresent();
            // expires가 미래이므로 expiresAt이 현재보다 이후여야 함
            assertThat(result.get().expiresAt()).isAfter(clock.instant());
        }
    }

    @Nested
    @DisplayName("issueSessionToken - 짧은 토큰 처리 테스트")
    class ShortTokenTest {

        @Test
        @DisplayName("짧은 토큰(10자 이하)이 있는 Set-Cookie에서 세션 토큰을 발급한다")
        void issueSessionToken_withShortToken_returnsSessionToken() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";
            // 10자 이하 토큰 (truncateToken에서 그대로 반환 경로)
            String shortToken = "tok12345";

            ClientResponse mockResponse =
                    ClientResponse.create(HttpStatus.OK)
                            .header(
                                    HttpHeaders.SET_COOKIE,
                                    "PHPSESSID=" + shortToken + "; Max-Age=7200; Path=/; HttpOnly")
                            .build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().token()).isEqualTo(shortToken);
        }
    }

    @Nested
    @DisplayName("issueSessionToken - extractCookieFromHeaders 내부 루프 테스트")
    class ExtractCookieFromHeadersTest {

        @Test
        @DisplayName("nid 헤더가 nid=value; Path=/ 형식일 때 내부 파트를 순회하여 값을 추출한다")
        void issueSessionToken_withNidHeaderInMultiParts_extractsNidFromParts() {
            // given
            String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";
            String tokenValue = "session-nid-multipart";

            // nid 쿠키가 복수 파트를 가진 형태로 Set-Cookie 헤더에 포함
            // extractCookieFromHeaders의 내부 for 루프(line 160) 커버
            ClientResponse mockResponse =
                    ClientResponse.create(HttpStatus.OK)
                            .header(
                                    HttpHeaders.SET_COOKIE,
                                    "PHPSESSID=" + tokenValue + "; Max-Age=7200; Path=/; HttpOnly")
                            .header(
                                    HttpHeaders.SET_COOKIE,
                                    "nid=nid-test-value; Max-Age=86400; Path=/;"
                                            + " Domain=.mustit.co.kr")
                            .build();

            setupWebClientMock(mockResponse);

            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().token()).isEqualTo(tokenValue);
        }
    }

    // ===== 헬퍼 메서드 =====

    private void setupWebClientMock(ClientResponse mockResponse) {
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);

        when(webClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), anyString())).thenReturn(headersSpec);
        when(headersSpec.exchangeToMono(any()))
                .thenAnswer(
                        invocation -> {
                            java.util.function.Function<ClientResponse, Mono<?>> fn =
                                    invocation.getArgument(0);
                            return fn.apply(mockResponse);
                        });
    }
}
