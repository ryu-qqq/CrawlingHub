package com.ryuqq.crawlinghub.domain.crawl.task;

/**
 * 크롤링 작업 상태
 */
public enum TaskStatus {
    WAITING(1, "대기"),
    PUBLISHED(2, "발행됨"),
    RUNNING(3, "실행중"),
    SUCCESS(4, "성공"),
    FAILED(5, "실패"),
    RETRY(6, "재시도");

    private final int priority;
    private final String description;

    TaskStatus(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return this == SUCCESS;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isRunning() {
        return this == RUNNING;
    }

    public static TaskStatus fromString(String statusStr) {
        if (statusStr == null || statusStr.isBlank()) {
            throw new IllegalArgumentException("TaskStatus는 필수입니다");
        }

        try {
            return TaskStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 TaskStatus입니다: " + statusStr);
        }
    }
}
