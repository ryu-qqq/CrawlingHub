package com.ryuqq.crawlinghub.adapter.in.rest.execution.error;

import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.net.URI;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * CrawlExecution Error Mapper
 *
 * <p>CrawlExecution 도메인 예외를 RFC 7807 Problem Details로 변환하는 ErrorMapper 구현체입니다.
 *
 * <p><strong>PREFIX 기반 선택 전략:</strong>
 *
 * <ul>
 *   <li>Prefix: "CRAWL-EXEC-" (CrawlExecutionErrorCode의 모든 에러 코드 접두사)
 *   <li>대상 예외: CrawlExecutionNotFoundException, InvalidCrawlExecutionStateException 등
 * </ul>
 *
 * <p><strong>HTTP Status 매핑:</strong>
 *
 * <ul>
 *   <li>CRAWL-EXEC-001 (CRAWL_EXECUTION_NOT_FOUND) → 404 Not Found
 *   <li>CRAWL-EXEC-002 (INVALID_CRAWL_EXECUTION_STATE) → 400 Bad Request
 * </ul>
 *
 * <p><strong>I18N 메시지 키 규칙:</strong>
 *
 * <ul>
 *   <li>CRAWL-EXEC-001 → error.crawl.exec.001
 * </ul>
 *
 * <p><strong>RFC 7807 Type URI:</strong>
 *
 * <ul>
 *   <li>Base: https://api.example.com/problems/execution/
 *   <li>예시: https://api.example.com/problems/execution/crawl-exec-001
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlExecutionErrorMapper implements ErrorMapper {

    private static final String PREFIX = "CRAWL-EXEC-";
    private static final String TYPE_BASE = "https://api.example.com/problems/execution/";

    private final MessageSource messageSource;

    /**
     * CrawlExecutionErrorMapper 생성자
     *
     * @param messageSource Spring MessageSource for I18N support
     */
    public CrawlExecutionErrorMapper(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * PREFIX 기반 지원 여부 판단
     *
     * <p>에러 코드가 "CRAWL-EXEC-"로 시작하는 경우 이 Mapper가 처리합니다.
     *
     * @param ex DomainException (에러 코드 예: "CRAWL-EXEC-001")
     * @return "CRAWL-EXEC-" prefix를 가진 경우 true
     */
    @Override
    public boolean supports(DomainException ex) {
        return ex != null && ex.code() != null && ex.code().startsWith(PREFIX);
    }

    /**
     * DomainException → MappedError 변환
     *
     * <p>CrawlExecution 도메인 예외를 HTTP 응답용 MappedError로 변환합니다.
     *
     * @param exception CrawlExecution 도메인 예외
     * @param locale I18N 로케일
     * @return MappedError (HttpStatus, title, detail, type URI)
     */
    @Override
    public MappedError map(DomainException exception, Locale locale) {
        var code = exception.code();
        var httpStatus = mapHttpStatus(code);

        // 1. Type URI 생성 (CRAWL-EXEC-001 → crawl-exec-001)
        URI type = URI.create(TYPE_BASE + code.toLowerCase());

        // 2. MessageSource에서 I18N 메시지 조회
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
     * @param code 에러 코드
     * @param exception DomainException
     * @return 메시지 파라미터 배열
     */
    private Object[] extractArgs(String code, DomainException exception) {
        var argsMap = exception.args();

        return switch (code) {
            case "CRAWL-EXEC-001" -> new Object[] {argsMap.get("crawlExecutionId")};
            case "CRAWL-EXEC-002" -> new Object[] {argsMap.get("currentStatus")};
            default -> new Object[0];
        };
    }

    /**
     * ErrorCode → HttpStatus 매핑
     *
     * @param code 에러 코드 (예: "CRAWL-EXEC-001")
     * @return HttpStatus
     */
    private HttpStatus mapHttpStatus(String code) {
        return switch (code) {
            case "CRAWL-EXEC-001" -> HttpStatus.NOT_FOUND; // CRAWL_EXECUTION_NOT_FOUND
            case "CRAWL-EXEC-002" -> HttpStatus.BAD_REQUEST; // INVALID_CRAWL_EXECUTION_STATE
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * ErrorCode → MessageSource Key 변환
     *
     * @param code 에러 코드 (예: CRAWL-EXEC-001)
     * @return MessageSource 키 (예: error.crawl.exec.001)
     */
    private String toMessageKey(String code) {
        // CRAWL-EXEC-001 → error.crawl.exec.001
        return "error." + code.toLowerCase().replace("-", ".");
    }
}
