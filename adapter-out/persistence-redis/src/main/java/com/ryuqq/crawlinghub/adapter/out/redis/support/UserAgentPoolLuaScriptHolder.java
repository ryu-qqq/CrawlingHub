package com.ryuqq.crawlinghub.adapter.out.redis.support;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

/**
 * UserAgent Pool Lua Script Holder
 *
 * <p>Redis Lua 스크립트를 classpath에서 로드하여 보관합니다.
 *
 * <p><strong>Phase 2 변경사항</strong>:
 *
 * <ul>
 *   <li>기존 consumeToken, recordSuccess, recordFailure, rateLimitSuspend 스크립트 유지 (Phase 6에서 제거)
 *   <li>신규 borrow, return, cooldownRecover 스크립트 추가
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentPoolLuaScriptHolder {

    // Phase 2: 신규 Borrow/Return 패턴 스크립트
    private final String borrowScript;
    private final String returnScript;
    private final String cooldownRecoverScript;

    // 레거시 스크립트 (Phase 6에서 제거 예정)
    private final String consumeTokenScript;
    private final String recordSuccessScript;
    private final String recordFailureScript;
    private final String rateLimitSuspendScript;

    @SuppressFBWarnings(
            value = "CT_CONSTRUCTOR_THROW",
            justification = "Spring @Component: IoC 컨테이너가 생성을 관리하므로 Finalizer 공격 위험 없음")
    public UserAgentPoolLuaScriptHolder() {
        // Phase 2: 신규 스크립트 로드
        this.borrowScript = loadLuaScript("lua/useragent_borrow.lua");
        this.returnScript = loadLuaScript("lua/useragent_return.lua");
        this.cooldownRecoverScript = loadLuaScript("lua/useragent_cooldown_recover.lua");

        // 레거시 스크립트 로드 (Phase 6까지 유지)
        this.consumeTokenScript = loadLuaScript("lua/useragent_consume_token.lua");
        this.recordSuccessScript = loadLuaScript("lua/useragent_record_success.lua");
        this.recordFailureScript = loadLuaScript("lua/useragent_record_failure.lua");
        this.rateLimitSuspendScript = loadLuaScript("lua/useragent_rate_limit_suspend.lua");
    }

    /** Phase 2: UserAgent Borrow 스크립트 (IDLE -> BORROWED) */
    public String borrowScript() {
        return borrowScript;
    }

    /** Phase 2: UserAgent Return 스크립트 (BORROWED -> IDLE/COOLDOWN/SUSPENDED) */
    public String returnScript() {
        return returnScript;
    }

    /** Phase 2: Cooldown Recovery 스크립트 (COOLDOWN -> IDLE/SESSION_REQUIRED) */
    public String cooldownRecoverScript() {
        return cooldownRecoverScript;
    }

    /**
     * @deprecated Phase 6에서 제거 예정. {@link #borrowScript()} 사용
     */
    @Deprecated
    public String consumeTokenScript() {
        return consumeTokenScript;
    }

    /**
     * @deprecated Phase 6에서 제거 예정. {@link #returnScript()} 사용
     */
    @Deprecated
    public String recordSuccessScript() {
        return recordSuccessScript;
    }

    /**
     * @deprecated Phase 6에서 제거 예정. {@link #returnScript()} 사용
     */
    @Deprecated
    public String recordFailureScript() {
        return recordFailureScript;
    }

    /**
     * @deprecated Phase 6에서 제거 예정. {@link #returnScript()} 사용
     */
    @Deprecated
    public String rateLimitSuspendScript() {
        return rateLimitSuspendScript;
    }

    private String loadLuaScript(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            try (InputStream inputStream = resource.getInputStream()) {
                return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Lua script 로드 실패: " + path, e);
        }
    }
}
