package com.ryuqq.crawlinghub.domain.task;

import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.seller.SellerName;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 크롤링 작업 Aggregate Root
 *
 * <p>비즈니스 규칙:
 * <ul>
 *   <li>최대 재시도 횟수: 3회</li>
 *   <li>타임아웃: 10분</li>
 *   <li>RUNNING 상태 10분 초과 시 자동 RETRY</li>
 *   <li>멱등성 키로 중복 방지</li>
 * </ul>
 */
public class Task {

    private static final int MAX_RETRY_COUNT = 3;

    private final TaskId id;
    private final MustitSellerId sellerId;
    private final SellerName sellerName;
    private final TaskType taskType;
    private TaskStatus status;
    private final RequestUrl requestUrl;
    private final Integer pageNumber;
    private Integer retryCount;
    private final String idempotencyKey;
    private final Long crawlScheduleId;
    private final TriggerType triggerType;
    private final LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private final Clock clock;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private 전체 생성자 (reconstitute 전용)
     */
    private Task(
        TaskId id,
        MustitSellerId sellerId,
        SellerName sellerName,
        TaskType taskType,
        TaskStatus status,
        RequestUrl requestUrl,
        Integer pageNumber,
        Integer retryCount,
        String idempotencyKey,
        Long crawlScheduleId,
        TriggerType triggerType,
        LocalDateTime scheduledAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        Clock clock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.taskType = taskType;
        this.status = status;
        this.requestUrl = requestUrl;
        this.pageNumber = pageNumber;
        this.retryCount = retryCount;
        this.idempotencyKey = idempotencyKey;
        this.crawlScheduleId = crawlScheduleId;
        this.triggerType = triggerType;
        this.scheduledAt = scheduledAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.clock = clock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Package-private 주요 생성자 (검증 포함)
     */
    Task(
        TaskId id,
        MustitSellerId sellerId,
        SellerName sellerName,
        TaskType taskType,
        RequestUrl requestUrl,
        Integer pageNumber,
        String idempotencyKey,
        Long crawlScheduleId,
        TriggerType triggerType,
        LocalDateTime scheduledAt,
        Clock clock
    ) {
        validateRequiredFields(sellerId, sellerName, taskType, requestUrl, idempotencyKey, scheduledAt);

        this.id = id;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.taskType = taskType;
        this.status = TaskStatus.WAITING;
        this.requestUrl = requestUrl;
        this.pageNumber = pageNumber;
        this.retryCount = 0;
        this.idempotencyKey = idempotencyKey;
        this.crawlScheduleId = crawlScheduleId;
        this.triggerType = triggerType != null ? triggerType : TriggerType.MANUAL;
        this.scheduledAt = scheduledAt;
        this.startedAt = null;
        this.completedAt = null;
        this.clock = clock;
        this.createdAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 신규 작업 생성 (ID 없음)
     */
    public static Task forNew(
        MustitSellerId sellerId,
        SellerName sellerName,
        TaskType taskType,
        RequestUrl requestUrl,
        Integer pageNumber,
        String idempotencyKey,
        Long crawlScheduleId,
        TriggerType triggerType,
        LocalDateTime scheduledAt
    ) {
        return new Task(
            null,
            sellerId,
            sellerName,
            taskType,
            requestUrl,
            pageNumber,
            idempotencyKey,
            crawlScheduleId,
            triggerType,
            scheduledAt,
            Clock.systemDefaultZone()
        );
    }

    /**
     * 기존 작업 생성 (ID 있음)
     */
    public static Task of(
        TaskId id,
        MustitSellerId sellerId,
        SellerName sellerName,
        TaskType taskType,
        RequestUrl requestUrl,
        Integer pageNumber,
        String idempotencyKey,
        LocalDateTime scheduledAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("Task ID는 필수입니다");
        }
        return new Task(
            id,
            sellerId,
            sellerName,
            taskType,
            requestUrl,
            pageNumber,
            idempotencyKey,
            null,  // crawlScheduleId
            TriggerType.MANUAL,  // triggerType
            scheduledAt,
            Clock.systemDefaultZone()
        );
    }

    /**
     * META Task 생성 (전체 상품 개수 조회용)
     *
     * <p>API 엔드포인트: GET /mustIt-api/facade-api/v1/searchmini-shop-search (pageSize=1)
     *
     * @param sellerId 판매자 ID
     * @param sellerName 판매자 이름 (예: LIKEASTAR)
     * @param crawlScheduleId 크롤 스케줄 ID (null 가능)
     * @param triggerType 트리거 타입 (MANUAL: REST API, AUTO: Event)
     * @param scheduledAt 예약 시각
     * @return META Task 인스턴스
     */
    public static Task forMeta(
        MustitSellerId sellerId,
        SellerName sellerName,
        Long crawlScheduleId,
        TriggerType triggerType,
        LocalDateTime scheduledAt
    ) {
        String url = String.format(
            "https://m.web.mustIt.co.kr/mustit-api/facade-api/v1/searchmini-shop-search?sellerId=%s&pageNo=0&pageSize=1&order=LATEST",
            sellerName.getValue()
        );

        String idempotencyKey = String.format("META_%s", sellerName.getValue());

        return forNew(
            sellerId,
            sellerName,
            TaskType.META,
            RequestUrl.of(url),
            0,
            idempotencyKey,
            crawlScheduleId,
            triggerType,
            scheduledAt
        );
    }

    /**
     * MINI_SHOP Task 생성 (상품 목록 조회용)
     *
     * <p>API 엔드포인트: GET /mustit-api/facade-api/v1/searchmini-shop-search
     *
     * @param sellerId 판매자 ID
     * @param sellerName 판매자 이름 (예: LIKEASTAR)
     * @param pageNo 페이지 번호 (0부터 시작)
     * @param pageSize 페이지당 항목 수
     * @param crawlScheduleId 크롤 스케줄 ID (null 가능)
     * @param triggerType 트리거 타입 (MANUAL: REST API, AUTO: Event)
     * @param scheduledAt 예약 시각
     * @return MINI_SHOP Task 인스턴스
     */
    public static Task forMiniShop(
        MustitSellerId sellerId,
        SellerName sellerName,
        int pageNo,
        int pageSize,
        Long crawlScheduleId,
        TriggerType triggerType,
        LocalDateTime scheduledAt
    ) {
        String url = String.format(
            "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/searchmini-shop-search?sellerId=%s&pageNo=%d&pageSize=%d&order=LATEST",
            sellerName.getValue(),
            pageNo,
            pageSize
        );

        String idempotencyKey = String.format("MINI_SHOP_%s_%d_%d",
            sellerName.getValue(), pageNo, pageSize);

        return forNew(
            sellerId,
            sellerName,
            TaskType.MINI_SHOP,
            RequestUrl.of(url),
            pageNo,
            idempotencyKey,
            crawlScheduleId,
            triggerType,
            scheduledAt
        );
    }

    /**
     * PRODUCT_DETAIL Task 생성 (개별 상품 상세 정보 조회용)
     *
     * <p>API 엔드포인트: GET /mustit-api/facade-api/v1/searchitem/{itemNo}
     *
     * @param sellerId 판매자 ID
     * @param sellerName 판매자 이름
     * @param itemNo 상품 번호
     * @param crawlScheduleId 크롤 스케줄 ID (null 가능)
     * @param triggerType 트리거 타입 (MANUAL: REST API, AUTO: Event)
     * @param scheduledAt 예약 시각
     * @return PRODUCT_DETAIL Task 인스턴스
     */
    public static Task forProductDetail(
        MustitSellerId sellerId,
        SellerName sellerName,
        Long itemNo,
        Long crawlScheduleId,
        TriggerType triggerType,
        LocalDateTime scheduledAt
    ) {
        String url = String.format(
            "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/searchitem/%d",
            itemNo
        );

        String idempotencyKey = String.format("PRODUCT_DETAIL_%s_%d",
            sellerName.getValue(), itemNo);

        return forNew(
            sellerId,
            sellerName,
            TaskType.PRODUCT_DETAIL,
            RequestUrl.of(url),
            null,
            idempotencyKey,
            crawlScheduleId,
            triggerType,
            scheduledAt
        );
    }

    /**
     * PRODUCT_OPTION Task 생성 (개별 상품 옵션 정보 조회용)
     *
     * <p>API 엔드포인트: GET /mustit-api/facade-api/v1/searchitem/{itemNo}/option
     *
     * @param sellerId 판매자 ID
     * @param sellerName 판매자 이름
     * @param itemNo 상품 번호
     * @param crawlScheduleId 크롤 스케줄 ID (null 가능)
     * @param triggerType 트리거 타입 (MANUAL: REST API, AUTO: Event)
     * @param scheduledAt 예약 시각
     * @return PRODUCT_OPTION Task 인스턴스
     */
    public static Task forProductOption(
        MustitSellerId sellerId,
        SellerName sellerName,
        Long itemNo,
        Long crawlScheduleId,
        TriggerType triggerType,
        LocalDateTime scheduledAt
    ) {
        String url = String.format(
            "https://m.web.mustit.co.kr/mustit-api/facade-api/v1/searchitem/%d/option",
            itemNo
        );

        String idempotencyKey = String.format("PRODUCT_OPTION_%s_%d",
            sellerName.getValue(), itemNo);

        return forNew(
            sellerId,
            sellerName,
            TaskType.PRODUCT_OPTION,
            RequestUrl.of(url),
            null,
            idempotencyKey,
            crawlScheduleId,
            triggerType,
            scheduledAt
        );
    }

    /**
     * DB reconstitute (모든 필드 포함)
     */
    public static Task reconstitute(
        TaskId id,
        MustitSellerId sellerId,
        SellerName sellerName,
        TaskType taskType,
        TaskStatus status,
        RequestUrl requestUrl,
        Integer pageNumber,
        Integer retryCount,
        String idempotencyKey,
        Long crawlScheduleId,
        TriggerType triggerType,
        LocalDateTime scheduledAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException("DB reconstitute는 ID가 필수입니다");
        }
        return new Task(
            id,
            sellerId,
            sellerName,
            taskType,
            status,
            requestUrl,
            pageNumber,
            retryCount,
            idempotencyKey,
            crawlScheduleId,
            triggerType,
            scheduledAt,
            startedAt,
            completedAt,
            Clock.systemDefaultZone(),
            createdAt,
            updatedAt
        );
    }

    private static void validateRequiredFields(
        MustitSellerId sellerId,
        SellerName sellerName,
        TaskType taskType,
        RequestUrl requestUrl,
        String idempotencyKey,
        LocalDateTime scheduledAt
    ) {
        if (sellerId == null) {
            throw new IllegalArgumentException("셀러 ID는 필수입니다");
        }
        if (sellerName == null) {
            throw new IllegalArgumentException("셀러 이름은 필수입니다");
        }
        if (taskType == null) {
            throw new IllegalArgumentException("작업 유형은 필수입니다");
        }
        if (requestUrl == null) {
            throw new IllegalArgumentException("요청 URL은 필수입니다");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("멱등성 키는 필수입니다");
        }
        if (scheduledAt == null) {
            throw new IllegalArgumentException("예약 시간은 필수입니다");
        }
    }

    /**
     * 작업 발행
     */
    public void publish() {
        if (this.status != TaskStatus.WAITING) {
            throw new IllegalStateException("WAITING 상태에서만 발행할 수 있습니다");
        }
        this.status = TaskStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 작업 시작
     */
    public void startProcessing() {
        if (this.status != TaskStatus.PUBLISHED && this.status != TaskStatus.RETRY) {
            throw new IllegalStateException("PUBLISHED 또는 RETRY 상태에서만 시작할 수 있습니다");
        }
        this.status = TaskStatus.RUNNING;
        this.startedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 작업 성공 완료
     */
    public void completeSuccessfully() {
        if (this.status != TaskStatus.RUNNING) {
            throw new IllegalStateException("RUNNING 상태에서만 완료할 수 있습니다");
        }
        this.status = TaskStatus.SUCCESS;
        this.completedAt = LocalDateTime.now(clock);
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 작업 실패
     */
    public void failWithError(String errorMessage) {
        if (this.status != TaskStatus.RUNNING) {
            throw new IllegalStateException("RUNNING 상태에서만 실패 처리할 수 있습니다");
        }

        if (canRetry()) {
            this.status = TaskStatus.RETRY;
            this.retryCount++;
        } else {
            this.status = TaskStatus.FAILED;
            this.completedAt = LocalDateTime.now(clock);
        }
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 재시도 가능 여부
     */
    public boolean canRetry() {
        return retryCount < MAX_RETRY_COUNT;
    }

    /**
     * 재시도 횟수 증가
     */
    public void incrementRetry() {
        if (!canRetry()) {
            throw new IllegalStateException("최대 재시도 횟수를 초과했습니다");
        }
        this.retryCount++;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 타임아웃 확인 (10분)
     */
    public boolean isTimeout() {
        if (startedAt == null || !status.isRunning()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        return startedAt.plusMinutes(10).isBefore(now);
    }

    /**
     * 특정 상태인지 확인
     */
    public boolean hasStatus(TaskStatus targetStatus) {
        return this.status == targetStatus;
    }

    /**
     * 완료 여부
     */
    public boolean isCompleted() {
        return status.isCompleted();
    }

    /**
     * 실패 여부
     */
    public boolean isFailed() {
        return status.isFailed();
    }

    // Law of Demeter 준수 메서드
    public Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public Long getSellerIdValue() {
        return sellerId != null ? sellerId.value() : null;
    }

    public String getSellerNameValue() {
        return sellerName != null ? sellerName.getValue() : null;
    }

    public SellerName getSellerName() {
        return sellerName;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getRequestUrlValue() {
        return requestUrl.getValue();
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Long getCrawlScheduleId() {
        return crawlScheduleId;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task that = (Task) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
            "id=" + id +
            ", sellerId=" + sellerId +
            ", taskType=" + taskType +
            ", status=" + status +
            ", requestUrl=" + requestUrl +
            ", pageNumber=" + pageNumber +
            ", retryCount=" + retryCount +
            '}';
    }
}
