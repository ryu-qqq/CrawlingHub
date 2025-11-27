package com.ryuqq.crawlinghub.application.useragent.port.in.command;

import com.ryuqq.crawlinghub.application.useragent.dto.command.RecordUserAgentResultCommand;

/**
 * UserAgent 결과 기록 UseCase
 *
 * <p>크롤링 결과를 기록하고 UserAgent 상태를 업데이트합니다.
 *
 * <p><strong>결과 처리 규칙</strong>:
 *
 * <ul>
 *   <li>성공: Health Score +5 (최대 100)
 *   <li>429 응답: 즉시 SUSPENDED (Pool에서 제거)
 *   <li>기타 에러: 단순 로깅 (무시)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RecordUserAgentResultUseCase {

    /**
     * 결과 기록 실행
     *
     * @param command 결과 기록 커맨드 (userAgentId, httpStatusCode, success)
     */
    void execute(RecordUserAgentResultCommand command);
}
