package com.ryuqq.crawlinghub.application.useragent.dto.command;

import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.util.List;

/**
 * UserAgent 상태 일괄 변경 Command
 *
 * <p>관리자가 여러 UserAgent의 상태를 일괄 변경하기 위한 Command 객체입니다.
 *
 * <p><strong>지원 상태 전환</strong>:
 *
 * <ul>
 *   <li>AVAILABLE ↔ SUSPENDED ↔ BLOCKED (모든 전환 가능)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record UpdateUserAgentStatusCommand(List<Long> userAgentIds, UserAgentStatus status) {

    /**
     * Command 생성 (유효성 검증 포함)
     *
     * @param userAgentIds UserAgent ID 목록
     * @param status 변경할 상태
     * @throws IllegalArgumentException ID 목록이 비어있거나 상태가 null인 경우
     */
    public UpdateUserAgentStatusCommand {
        if (userAgentIds == null || userAgentIds.isEmpty()) {
            throw new IllegalArgumentException("UserAgent ID 목록은 비어있을 수 없습니다");
        }
        if (status == null) {
            throw new IllegalArgumentException("변경할 상태는 null일 수 없습니다");
        }
    }

    /**
     * 변경 대상 UserAgent 수 반환
     *
     * @return UserAgent ID 목록 크기
     */
    public int count() {
        return userAgentIds.size();
    }
}
