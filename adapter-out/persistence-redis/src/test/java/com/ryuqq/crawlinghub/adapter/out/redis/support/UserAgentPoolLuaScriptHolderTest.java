package com.ryuqq.crawlinghub.adapter.out.redis.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * UserAgentPoolLuaScriptHolder 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("redis")
@DisplayName("UserAgentPoolLuaScriptHolder 단위 테스트")
class UserAgentPoolLuaScriptHolderTest {

    @Test
    @DisplayName("borrowScript - Lua 스크립트 로드 성공")
    void shouldLoadBorrowScript() {
        UserAgentPoolLuaScriptHolder holder = new UserAgentPoolLuaScriptHolder();
        assertThat(holder.borrowScript()).isNotBlank();
        assertThat(holder.borrowScript()).contains("BORROWED");
    }

    @Test
    @DisplayName("returnScript - Lua 스크립트 로드 성공")
    void shouldLoadReturnScript() {
        UserAgentPoolLuaScriptHolder holder = new UserAgentPoolLuaScriptHolder();
        assertThat(holder.returnScript()).isNotBlank();
        assertThat(holder.returnScript()).contains("COOLDOWN");
    }

    @Test
    @DisplayName("cooldownRecoverScript - Lua 스크립트 로드 성공")
    void shouldLoadCooldownRecoverScript() {
        UserAgentPoolLuaScriptHolder holder = new UserAgentPoolLuaScriptHolder();
        assertThat(holder.cooldownRecoverScript()).isNotBlank();
        assertThat(holder.cooldownRecoverScript()).contains("cooldownUntil");
    }

    @Test
    @DisplayName("레거시 consumeTokenScript - Lua 스크립트 로드 성공")
    @SuppressWarnings("deprecation")
    void shouldLoadConsumeTokenScript() {
        UserAgentPoolLuaScriptHolder holder = new UserAgentPoolLuaScriptHolder();
        assertThat(holder.consumeTokenScript()).isNotBlank();
    }

    @Test
    @DisplayName("레거시 recordSuccessScript - Lua 스크립트 로드 성공")
    @SuppressWarnings("deprecation")
    void shouldLoadRecordSuccessScript() {
        UserAgentPoolLuaScriptHolder holder = new UserAgentPoolLuaScriptHolder();
        assertThat(holder.recordSuccessScript()).isNotBlank();
    }

    @Test
    @DisplayName("레거시 recordFailureScript - Lua 스크립트 로드 성공")
    @SuppressWarnings("deprecation")
    void shouldLoadRecordFailureScript() {
        UserAgentPoolLuaScriptHolder holder = new UserAgentPoolLuaScriptHolder();
        assertThat(holder.recordFailureScript()).isNotBlank();
    }

    @Test
    @DisplayName("레거시 rateLimitSuspendScript - Lua 스크립트 로드 성공")
    @SuppressWarnings("deprecation")
    void shouldLoadRateLimitSuspendScript() {
        UserAgentPoolLuaScriptHolder holder = new UserAgentPoolLuaScriptHolder();
        assertThat(holder.rateLimitSuspendScript()).isNotBlank();
    }

    @Test
    @DisplayName("모든 스크립트가 한 번만 로드되고 매번 동일한 내용 반환")
    void shouldReturnSameScriptContentOnMultipleCalls() {
        // Given
        UserAgentPoolLuaScriptHolder holder = new UserAgentPoolLuaScriptHolder();

        // When
        String firstCall = holder.borrowScript();
        String secondCall = holder.borrowScript();

        // Then
        assertThat(firstCall).isEqualTo(secondCall);
        assertThat(firstCall).isSameAs(secondCall); // 같은 인스턴스 (캐시됨)
    }

    @Test
    @DisplayName("borrowScript가 IDLE과 BORROWED 키워드 포함")
    void borrowScriptShouldContainIdleAndBorrowed() {
        // Given
        UserAgentPoolLuaScriptHolder holder = new UserAgentPoolLuaScriptHolder();

        // When
        String script = holder.borrowScript();

        // Then
        assertThat(script).contains("BORROWED");
    }

    @Test
    @DisplayName("returnScript가 IDLE, COOLDOWN 키워드 포함")
    void returnScriptShouldContainRelevantKeywords() {
        // Given
        UserAgentPoolLuaScriptHolder holder = new UserAgentPoolLuaScriptHolder();

        // When
        String script = holder.returnScript();

        // Then
        assertThat(script).contains("COOLDOWN");
    }

    @Test
    @DisplayName("cooldownRecoverScript가 cooldownUntil 키워드 포함")
    void cooldownRecoverScriptShouldContainCooldownUntil() {
        // Given
        UserAgentPoolLuaScriptHolder holder = new UserAgentPoolLuaScriptHolder();

        // When
        String script = holder.cooldownRecoverScript();

        // Then
        assertThat(script).contains("cooldownUntil");
    }
}
