package com.ryuqq.crawlinghub.adapter.in.rest.useragent.dto.command;

import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * UserAgent 상태 일괄 변경 API Request
 *
 * <p>관리자가 여러 UserAgent의 상태를 일괄 변경할 때 사용합니다.
 *
 * <p><strong>사용 시나리오</strong>:
 *
 * <ul>
 *   <li>관리자가 체크박스로 여러 UserAgent를 선택하여 상태 변경
 *   <li>문제가 있는 UserAgent들을 일괄 정지(SUSPENDED) 처리
 *   <li>점검 완료 후 일괄 활성화(AVAILABLE) 처리
 *   <li>보안 문제로 일괄 차단(BLOCKED) 처리
 * </ul>
 *
 * @param userAgentIds 변경할 UserAgent ID 목록
 * @param status 변경할 상태
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "UserAgent 상태 일괄 변경 요청")
public record UpdateUserAgentStatusApiRequest(
        @Schema(
                        description = "변경할 UserAgent ID 목록",
                        example = "[1, 2, 3]",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotEmpty(message = "UserAgent ID 목록은 비어있을 수 없습니다")
                List<Long> userAgentIds,
        @Schema(
                        description = "변경할 상태 (AVAILABLE, SUSPENDED, BLOCKED)",
                        example = "SUSPENDED",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "변경할 상태는 필수입니다")
                UserAgentStatus status) {}
