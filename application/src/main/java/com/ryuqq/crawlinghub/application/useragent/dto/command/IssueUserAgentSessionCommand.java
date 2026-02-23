package com.ryuqq.crawlinghub.application.useragent.dto.command;

/**
 * 세션 발급 커맨드
 *
 * @param issueType 발급 타입 (RENEW: 선제적 갱신, NEW: 신규 발급)
 * @param renewalBufferMinutes RENEW 시 만료 N분 전 기준
 * @param maxBatchSize 한 번에 처리할 최대 건수
 * @param sessionDelayMillis 세션 발급 간 대기 시간 (ms)
 * @author development-team
 * @since 1.0.0
 */
public record IssueUserAgentSessionCommand(
        SessionIssueType issueType,
        int renewalBufferMinutes,
        int maxBatchSize,
        long sessionDelayMillis) {

    public enum SessionIssueType {
        RENEW,
        NEW
    }

    public static IssueUserAgentSessionCommand ofRenew(
            int renewalBufferMinutes, int maxBatchSize, long sessionDelayMillis) {
        return new IssueUserAgentSessionCommand(
                SessionIssueType.RENEW, renewalBufferMinutes, maxBatchSize, sessionDelayMillis);
    }

    public static IssueUserAgentSessionCommand ofNew(int maxBatchSize, long sessionDelayMillis) {
        return new IssueUserAgentSessionCommand(
                SessionIssueType.NEW, 0, maxBatchSize, sessionDelayMillis);
    }
}
