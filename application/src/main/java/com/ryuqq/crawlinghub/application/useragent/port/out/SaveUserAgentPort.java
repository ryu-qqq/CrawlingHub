package com.ryuqq.crawlinghub.application.useragent.port.out;

import com.ryuqq.crawlinghub.domain.useragent.UserAgent;

/**
 * UserAgent 저장 Port
 *
 * <p>Persistence Adapter에 의해 구현됩니다.
 *
 * <p><strong>CQRS 패턴 적용 - Command 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Write 작업 전용 (save)</li>
 *   <li>✅ Domain Aggregate를 저장</li>
 *   <li>✅ 저장된 Domain Aggregate 반환</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
public interface SaveUserAgentPort {

    /**
     * UserAgent 저장 (신규 생성 또는 수정)
     *
     * @param userAgent 저장할 UserAgent (null 불가)
     * @return 저장된 UserAgent (ID 포함)
     * @throws IllegalArgumentException userAgent가 null인 경우
     */
    UserAgent save(UserAgent userAgent);
}

