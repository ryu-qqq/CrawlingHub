package com.ryuqq.crawlinghub.adapter.in.rest.schedule.error;

import com.ryuqq.crawlinghub.adapter.in.rest.common.mapper.ErrorMapper;
import com.ryuqq.crawlinghub.domain.common.exception.DomainException;
import java.net.URI;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * CrawlScheduler Error Mapper
 *
 * <p>CrawlScheduler 도메인 예외를 RFC 7807 Problem Details로 변환하는 ErrorMapper 구현체입니다.
 *
 * <p><strong>PREFIX 기반 선택 전략:</strong>
 *
 * <ul>
 *   <li>Prefix: "SCHEDULE-" (ScheduleErrorCode의 모든 에러 코드 접두사)
 *   <li>대상 예외: CrawlSchedulerNotFoundException, DuplicateSchedulerNameException 등
 * </ul>
 *
 * <p><strong>HTTP Status 매핑:</strong>
 *
 * <ul>
 *   <li>SCHEDULE-001 (CRAWL_SCHEDULER_NOT_FOUND) → 404 Not Found
 *   <li>SCHEDULE-002 (DUPLICATE_SCHEDULER_NAME) → 409 Conflict
 * </ul>
 *
 * <p><strong>I18N 메시지 키 규칙:</strong>
 *
 * <ul>
 *   <li>SCHEDULE-001 → error.schedule.001
 *   <li>SCHEDULE-002 → error.schedule.002
 * </ul>
 *
 * <p><strong>RFC 7807 Type URI:</strong>
 *
 * <ul>
 *   <li>Base: https://api.example.com/problems/schedule/
 *   <li>예시: https://api.example.com/problems/schedule/schedule-001
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerErrorMapper implements ErrorMapper {

    private static final String PREFIX = "SCHEDULE-";
    private static final String TYPE_BASE = "https://api.example.com/problems/schedule/";

    private final MessageSource messageSource;

    /**
     * CrawlSchedulerErrorMapper 생성자
     *
     * @param messageSource Spring MessageSource for I18N support
     */
    public CrawlSchedulerErrorMapper(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * PREFIX 기반 지원 여부 판단
     *
     * <p>에러 코드가 "SCHEDULE-"로 시작하는 경우 이 Mapper가 처리합니다.
     *
     * @param code 에러 코드 (예: "SCHEDULE-001")
     * @return "SCHEDULE-" prefix를 가진 경우 true
     */
    @Override
    public boolean supports(String code) {
        return code != null && code.startsWith(PREFIX);
    }

    /**
     * DomainException → MappedError 변환
     *
     * <p>CrawlScheduler 도메인 예외를 HTTP 응답용 MappedError로 변환합니다.
     *
     * @param exception CrawlScheduler 도메인 예외
     * @param locale I18N 로케일
     * @return MappedError (HttpStatus, title, detail, type URI)
     */
    @Override
    public MappedError map(DomainException exception, Locale locale) {
        var code = exception.code();
        var httpStatus = mapHttpStatus(code);

        // 1. Type URI 생성 (SCHEDULE-001 → schedule-001)
        URI type = URI.create(TYPE_BASE + code.toLowerCase());

        // 2. MessageSource에서 I18N 메시지 조회
        //    SCHEDULE-001 → error.schedule.001
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
            case "SCHEDULE-001" -> new Object[] {argsMap.get("crawlSchedulerId")};
            case "SCHEDULE-002" ->
                    new Object[] {argsMap.get("sellerId"), argsMap.get("schedulerName")};
            default -> new Object[0];
        };
    }

    /**
     * ErrorCode → HttpStatus 매핑
     *
     * <p>Schedule 에러 코드를 HTTP Status로 매핑합니다.
     *
     * @param code 에러 코드 (예: "SCHEDULE-001")
     * @return HttpStatus
     */
    private HttpStatus mapHttpStatus(String code) {
        return switch (code) {
            case "SCHEDULE-001" -> HttpStatus.NOT_FOUND; // CRAWL_SCHEDULER_NOT_FOUND
            case "SCHEDULE-002" -> HttpStatus.CONFLICT; // DUPLICATE_SCHEDULER_NAME
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
     *   <li>SCHEDULE-001 → error.schedule.001
     *   <li>소문자 변환 및 하이픈을 점(.)으로 변환
     *   <li>error. prefix 추가
     * </ul>
     *
     * @param code 에러 코드 (예: SCHEDULE-001)
     * @return MessageSource 키 (예: error.schedule.001)
     */
    private String toMessageKey(String code) {
        // SCHEDULE-001 → error.schedule.001
        return "error." + code.toLowerCase().replace("-", ".");
    }
}
