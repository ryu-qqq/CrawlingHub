package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.integration;

import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.adapter.UserAgentCommandAdapter;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.adapter.UserAgentQueryAdapter;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.repository.UserAgentJpaRepository;
import com.ryuqq.crawlinghub.application.useragent.assembler.UserAgentAssembler;
import com.ryuqq.crawlinghub.application.useragent.port.out.LoadUserAgentPort;
import com.ryuqq.crawlinghub.application.useragent.port.out.SaveUserAgentPort;
import com.ryuqq.crawlinghub.domain.token.Token;
import com.ryuqq.crawlinghub.domain.useragent.TokenStatus;
import com.ryuqq.crawlinghub.domain.useragent.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentFixture;
import com.ryuqq.crawlinghub.domain.useragent.UserAgentId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserAgent 토큰 획득 및 로테이션 통합 테스트
 *
 * <p>실제 DB 환경에서 토큰 발급, 로테이션, Rate Limit 복구 등의 전체 플로우를 검증합니다.
 *
 * <p><strong>테스트 시나리오:</strong></p>
 * <ul>
 *   <li>1. 여러 UserAgent 생성 및 토큰 발급</li>
 *   <li>2. 로테이션 메커니즘 검증 (남은 요청 수 기반 선택)</li>
 *   <li>3. 요청 소비 후 상태 변경 검증</li>
 *   <li>4. Rate Limit 발생 및 복구 검증</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserAgentCommandAdapter.class, UserAgentQueryAdapter.class, UserAgentAssembler.class})
@DisplayName("UserAgent 토큰 획득 및 로테이션 통합 테스트")
class UserAgentTokenRotationIntegrationTest {

    @Autowired
    private UserAgentJpaRepository jpaRepository;

    @Autowired
    private SaveUserAgentPort saveUserAgentPort;

    @Autowired
    private LoadUserAgentPort loadUserAgentPort;

    @Autowired
    private UserAgentAssembler assembler;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("토큰 발급 및 저장 플로우")
    class TokenIssuanceFlow {

        @Test
        @DisplayName("UserAgent를 생성하고 토큰을 발급하면 DB에 저장된다")
        void it_saves_user_agent_with_token() {
            // Given
            UserAgent userAgent = UserAgentFixture.create();
            Token token = Token.of("test-token-12345", LocalDateTime.now(), LocalDateTime.now().plusHours(24));

            // When: 토큰 발급 및 저장
            userAgent.issueNewToken(token);
            UserAgent saved = saveUserAgentPort.save(userAgent);

            // Then
            assertThat(saved.getIdValue()).isNotNull();
            assertThat(saved.getCurrentToken()).isEqualTo(token);
            assertThat(saved.getTokenStatus()).isEqualTo(TokenStatus.IDLE);
            assertThat(saved.getRemainingRequests()).isEqualTo(80);

            // And: DB에서 조회 가능
            Optional<UserAgent> found = loadUserAgentPort.findById(UserAgentId.of(saved.getIdValue()))
                .map(assembler::toDomain);
            assertThat(found).isPresent();
            assertThat(found.get().getCurrentToken()).isEqualTo(token);
        }

        @Test
        @DisplayName("여러 UserAgent에 토큰을 발급하고 모두 저장된다")
        void it_saves_multiple_user_agents_with_tokens() {
            // Given
            UserAgent userAgent1 = UserAgentFixture.createChrome();
            UserAgent userAgent2 = UserAgentFixture.createFirefox();
            LocalDateTime now = LocalDateTime.now();
            Token token1 = Token.of("token-chrome-123", now, now.plusHours(24));
            Token token2 = Token.of("token-firefox-456", now, now.plusHours(24));

            // When
            userAgent1.issueNewToken(token1);
            userAgent2.issueNewToken(token2);
            UserAgent saved1 = saveUserAgentPort.save(userAgent1);
            UserAgent saved2 = saveUserAgentPort.save(userAgent2);

            // Then
            assertThat(saved1.getIdValue()).isNotNull();
            assertThat(saved2.getIdValue()).isNotNull();
            assertThat(saved1.getCurrentToken()).isEqualTo(token1);
            assertThat(saved2.getCurrentToken()).isEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("로테이션 메커니즘")
    class RotationMechanism {

        @Test
        @DisplayName("여러 UserAgent 중 남은 요청 수가 가장 많은 것을 선택한다")
        void it_selects_user_agent_with_most_remaining_requests() {
            // Given: 여러 UserAgent 생성 및 저장
            UserAgent userAgent1 = UserAgentFixture.createCanMakeRequest(30); // 남은 요청 30
            UserAgent userAgent2 = UserAgentFixture.createCanMakeRequest(80); // 남은 요청 80 (가장 많음)
            UserAgent userAgent3 = UserAgentFixture.createCanMakeRequest(50); // 남은 요청 50

            LocalDateTime now = LocalDateTime.now();
            userAgent1.issueNewToken(Token.of("token-1", now, now.plusHours(24)));
            userAgent2.issueNewToken(Token.of("token-2", now, now.plusHours(24)));
            userAgent3.issueNewToken(Token.of("token-3", now, now.plusHours(24)));

            saveUserAgentPort.save(userAgent1);
            UserAgent saved2 = saveUserAgentPort.save(userAgent2);
            saveUserAgentPort.save(userAgent3);

            // When: 로테이션용 UserAgent 조회
            Optional<com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDto> selected =
                loadUserAgentPort.findAvailableForRotation();

            // Then: 남은 요청 수가 가장 많은 것이 선택됨
            assertThat(selected).isPresent();
            assertThat(selected.get().remainingRequests()).isEqualTo(80);
            assertThat(selected.get().id()).isEqualTo(saved2.getIdValue());
        }

        @Test
        @DisplayName("요청 소비 후 다시 조회하면 남은 요청 수가 감소한 것을 선택한다")
        void it_selects_user_agent_after_consuming_requests() {
            // Given: UserAgent 생성 및 저장
            UserAgent userAgent = UserAgentFixture.createCanMakeRequest(80);
            LocalDateTime now = LocalDateTime.now();
            userAgent.issueNewToken(Token.of("token-123", now, now.plusHours(24)));
            UserAgent saved = saveUserAgentPort.save(userAgent);

            // When: 요청 소비
            UserAgent found = loadUserAgentPort.findById(UserAgentId.of(saved.getIdValue()))
                .map(assembler::toDomain)
                .orElseThrow();
            found.consumeRequest();
            found.consumeRequest();
            UserAgent updated = saveUserAgentPort.save(found);

            // Then: 남은 요청 수가 감소함
            assertThat(updated.getRemainingRequests()).isEqualTo(78);

            // And: 다시 조회 시 감소한 값이 조회됨
            Optional<com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDto> reloaded =
                loadUserAgentPort.findById(UserAgentId.of(updated.getIdValue()));
            assertThat(reloaded).isPresent();
            assertThat(reloaded.get().remainingRequests()).isEqualTo(78);
        }
    }

    @Nested
    @DisplayName("Rate Limit 복구 플로우")
    class RateLimitRecoveryFlow {

        @Test
        @DisplayName("Rate Limit 발생 후 복구하면 다시 사용 가능해진다")
        void it_recovers_from_rate_limit_and_becomes_available() {
            // Given: UserAgent 생성 및 Rate Limit 발생
            UserAgent userAgent = UserAgentFixture.createCanMakeRequest(80);
            LocalDateTime now = LocalDateTime.now();
            userAgent.issueNewToken(Token.of("token-123", now, now.plusHours(24)));
            UserAgent saved = saveUserAgentPort.save(userAgent);

            // And: Rate Limit 발생
            UserAgent found = loadUserAgentPort.findById(UserAgentId.of(saved.getIdValue()))
                .map(assembler::toDomain)
                .orElseThrow();
            found.handleRateLimitError();
            saveUserAgentPort.save(found);

            // When: Rate Limit 복구
            UserAgent rateLimited = loadUserAgentPort.findById(UserAgentId.of(saved.getIdValue()))
                .map(assembler::toDomain)
                .orElseThrow();
            rateLimited.recoverFromRateLimit();
            UserAgent recovered = saveUserAgentPort.save(rateLimited);

            // Then: 복구됨
            assertThat(recovered.getTokenStatus()).isEqualTo(TokenStatus.RECOVERED);
            assertThat(recovered.getRemainingRequests()).isEqualTo(80);
            assertThat(recovered.getRateLimitResetAt()).isNull();

            // And: 다시 로테이션 가능
            Optional<com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDto> available =
                loadUserAgentPort.findAvailableForRotation();
            assertThat(available).isPresent();
        }
    }

    @Nested
    @DisplayName("통합 시나리오: 토큰 획득 → 요청 소비 → 로테이션")
    class IntegratedScenario {

        @Test
        @DisplayName("여러 UserAgent를 생성하고 토큰을 발급한 후 로테이션으로 선택하여 요청을 소비한다")
        void it_rotates_user_agents_and_consumes_requests() {
            // Given: 3개의 UserAgent 생성
            UserAgent userAgent1 = UserAgentFixture.createChrome();
            UserAgent userAgent2 = UserAgentFixture.createFirefox();
            UserAgent userAgent3 = UserAgentFixture.create();

            LocalDateTime now = LocalDateTime.now();
            userAgent1.issueNewToken(Token.of("token-chrome", now, now.plusHours(24)));
            userAgent2.issueNewToken(Token.of("token-firefox", now, now.plusHours(24)));
            userAgent3.issueNewToken(Token.of("token-default", now, now.plusHours(24)));

            saveUserAgentPort.save(userAgent1);
            saveUserAgentPort.save(userAgent2);
            saveUserAgentPort.save(userAgent3);

            // When: 첫 번째 로테이션 (남은 요청이 가장 많은 것 선택)
            Optional<com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDto> first =
                loadUserAgentPort.findAvailableForRotation();
            assertThat(first).isPresent();

            // And: 선택된 UserAgent로 요청 소비
            UserAgent firstSelected = loadUserAgentPort.findById(UserAgentId.of(first.get().id()))
                .map(assembler::toDomain)
                .orElseThrow();
            firstSelected.consumeRequest();
            saveUserAgentPort.save(firstSelected);

            // And: 두 번째 로테이션 (다시 남은 요청이 가장 많은 것 선택)
            Optional<com.ryuqq.crawlinghub.application.useragent.dto.query.UserAgentQueryDto> second =
                loadUserAgentPort.findAvailableForRotation();
            assertThat(second).isPresent();

            // Then: 첫 번째 선택과 다른 UserAgent가 선택될 수 있음 (남은 요청 수에 따라)
            // 또는 같은 UserAgent가 선택될 수 있음 (여전히 가장 많을 경우)
            assertThat(second.get().remainingRequests()).isGreaterThan(0);

            // And: 두 번째 선택으로도 요청 소비
            UserAgent secondSelected = loadUserAgentPort.findById(UserAgentId.of(second.get().id()))
                .map(assembler::toDomain)
                .orElseThrow();
            secondSelected.consumeRequest();
            UserAgent updated = saveUserAgentPort.save(secondSelected);

            // Then: 요청이 소비됨
            assertThat(updated.getRemainingRequests()).isLessThan(second.get().remainingRequests());
        }
    }
}

