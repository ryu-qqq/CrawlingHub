package com.ryuqq.crawlinghub.adapter.in.rest.task.error;

import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.net.URI;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * CrawlTask Error Mapper
 *
 * <p>CrawlTask 도메인 예외를 RFC 7807 Problem Details로 변환하는 ErrorMapper 구현체입니다.
 *
 * <p><strong>PREFIX 기반 선택 전략:</strong>
 *
 * <ul>
 *   <li>Prefix: "CRAWL-TASK-" (CrawlTaskErrorCode의 모든 에러 코드 접두사)
 *   <li>대상 예외: CrawlTaskNotFoundException, DuplicateCrawlTaskException 등
 * </ul>
 *
 * <p><strong>HTTP Status 매핑:</strong>
 *
 * <ul>
 *   <li>CRAWL-TASK-001 (CRAWL_TASK_NOT_FOUND) → 404 Not Found
 *   <li>CRAWL-TASK-002 (INVALID_CRAWL_TASK_STATE) → 400 Bad Request
 *   <li>CRAWL-TASK-003 (DUPLICATE_CRAWL_TASK) → 409 Conflict
 *   <li>CRAWL-TASK-004 (RETRY_LIMIT_EXCEEDED) → 400 Bad Request
 *   <li>CRAWL-TASK-005 (CRAWL_TASK_EXECUTION_FAILED) → 500 Internal Server Error
 * </ul>
 *
 * <p><strong>I18N 메시지 키 규칙:</strong>
 *
 * <ul>
 *   <li>CRAWL-TASK-001 → error.crawl.task.001
 * </ul>
 *
 * <p><strong>RFC 7807 Type URI:</strong>
 *
 * <ul>
 *   <li>Base: https://api.example.com/problems/crawl-task/
 *   <li>예시: https://api.example.com/problems/crawl-task/crawl-task-001
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskErrorMapper implements ErrorMapper {

    private static final String PREFIX = "CRAWL-TASK-";
    private static final String TYPE_BASE = "https://api.example.com/problems/crawl-task/";

    private final MessageSource messageSource;

    /**
     * CrawlTaskErrorMapper 생성자
     *
     * @param messageSource Spring MessageSource for I18N support
     */
    public CrawlTaskErrorMapper(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * PREFIX 기반 지원 여부 판단
     *
     * <p>에러 코드가 "CRAWL-TASK-"로 시작하는 경우 이 Mapper가 처리합니다.
     *
     * @param code 에러 코드 (예: "CRAWL-TASK-001")
     * @return "CRAWL-TASK-" prefix를 가진 경우 true
     */
    @Override
    public boolean supports(String code) {
        return code != null && code.startsWith(PREFIX);
    }

    /**
     * DomainException → MappedError 변환
     *
     * <p>CrawlTask 도메인 예외를 HTTP 응답용 MappedError로 변환합니다.
     *
     * @param exception CrawlTask 도메인 예외
     * @param locale I18N 로케일
     * @return MappedError (HttpStatus, title, detail, type URI)
     */
    @Override
    public MappedError map(DomainException exception, Locale locale) {
        var code = exception.code();
        var httpStatus = mapHttpStatus(code);

        // 1. Type URI 생성 (CRAWL-TASK-001 → crawl-task-001)
        URI type = URI.create(TYPE_BASE + code.toLowerCase());

        // 2. MessageSource에서 I18N 메시지 조회
        //    CRAWL-TASK-001 → error.crawl.task.001
        String messageKey = toMessageKey(code);
        Object[] args = extractArgs(code, exception);
        String title = messageSource.getMessage(messageKey, args, exception.getMessage(), locale);

        // 3. Detail은 도메인에서 정의한 기본 메시지
        String detail = exception.getMessage();

        return new MappedError(httpStatus, title, detail, type);
    }

    /**
     * DomainException에서 메시지 파라미터 추출
     *
     * <p>에러 코드별로 올바른 순서의 파라미터 배열을 생성합니다.
     *
     * @param code 에러 코드
     * @param exception DomainException
     * @return 메시지 파라미터 배열
     */
    private Object[] extractArgs(String code, DomainException exception) {
        var argsMap = exception.args();

        return switch (code) {
            case "CRAWL-TASK-001" -> new Object[] {argsMap.get("crawlTaskId")};
            case "CRAWL-TASK-002" -> new Object[] {argsMap.get("currentStatus"), argsMap.get("targetStatus")};
            case "CRAWL-TASK-003" -> new Object[] {argsMap.get("crawlSchedulerId"), argsMap.get("requestUrl")};
            case "CRAWL-TASK-004" -> new Object[] {argsMap.get("crawlTaskId"), argsMap.get("retryCount")};
            case "CRAWL-TASK-005" -> new Object[] {argsMap.get("crawlTaskId")};
            default -> new Object[0];
        };
    }

    /**
     * ErrorCode → HttpStatus 매핑
     *
     * <p>CrawlTask 에러 코드를 HTTP Status로 매핑합니다.
     *
     * @param code 에러 코드 (예: "CRAWL-TASK-001")
     * @return HttpStatus
     */
    private HttpStatus mapHttpStatus(String code) {
        return switch (code) {
            case "CRAWL-TASK-001" -> HttpStatus.NOT_FOUND; // CRAWL_TASK_NOT_FOUND
            case "CRAWL-TASK-002" -> HttpStatus.BAD_REQUEST; // INVALID_CRAWL_TASK_STATE
            case "CRAWL-TASK-003" -> HttpStatus.CONFLICT; // DUPLICATE_CRAWL_TASK
            case "CRAWL-TASK-004" -> HttpStatus.BAD_REQUEST; // RETRY_LIMIT_EXCEEDED
            case "CRAWL-TASK-005" -> HttpStatus.INTERNAL_SERVER_ERROR; // CRAWL_TASK_EXECUTION_FAILED
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    /**
     * ErrorCode → MessageSource Key 변환
     *
     * <p>에러 코드를 MessageSource 키로 변환합니다.
     *
     * <p><strong>변환 규칙:</strong>
     *
     * <ul>
     *   <li>CRAWL-TASK-001 → error.crawl.task.001
     *   <li>소문자 변환 및 하이픈을 점(.)으로 변환
     *   <li>error. prefix 추가
     * </ul>
     *
     * @param code 에러 코드 (예: CRAWL-TASK-001)
     * @return MessageSource 키 (예: error.crawl.task.001)
     */
    private String toMessageKey(String code) {
        // CRAWL-TASK-001 → error.crawl.task.001
        return "error." + code.toLowerCase().replace("-", ".");
    }
}
