package com.ryuqq.crawlinghub.domain.task;

public class TaskResultMetadata {

    private final Long metadataId;
    private final Long taskId;
    private final Long apiResponseTimeMs;
    private final Integer apiStatusCode;
    private Long dataSizeBytes;
    private Long itemsCount;
    private String s3Bucket;
    private String s3Key;

    private TaskResultMetadata(Long metadataId, Long taskId, Long apiResponseTimeMs, Integer apiStatusCode,
                              Long dataSizeBytes, Long itemsCount, String s3Bucket, String s3Key) {
        this.metadataId = metadataId;
        this.taskId = taskId;
        this.apiResponseTimeMs = apiResponseTimeMs;
        this.apiStatusCode = apiStatusCode;
        this.dataSizeBytes = dataSizeBytes;
        this.itemsCount = itemsCount;
        this.s3Bucket = s3Bucket;
        this.s3Key = s3Key;
    }

    public void updateS3Location(String bucket, String key) {
        this.s3Bucket = bucket;
        this.s3Key = key;
    }

    public void updateDataMetrics(Long sizeBytes, Long count) {
        this.dataSizeBytes = sizeBytes;
        this.itemsCount = count;
    }

    public Long getMetadataId() {
        return metadataId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Long getApiResponseTimeMs() {
        return apiResponseTimeMs;
    }

    public Integer getApiStatusCode() {
        return apiStatusCode;
    }

    public Long getDataSizeBytes() {
        return dataSizeBytes;
    }

    public Long getItemsCount() {
        return itemsCount;
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public static TaskResultMetadata create(Long taskId, Long apiResponseTimeMs, Integer apiStatusCode) {
        validateCreate(taskId, apiResponseTimeMs, apiStatusCode);
        return new TaskResultMetadata(null, taskId, apiResponseTimeMs, apiStatusCode, null, null, null, null);
    }

    public static TaskResultMetadata reconstitute(Long metadataId, Long taskId, Long apiResponseTimeMs,
                                                 Integer apiStatusCode, Long dataSizeBytes, Long itemsCount,
                                                 String s3Bucket, String s3Key) {
        return new TaskResultMetadata(metadataId, taskId, apiResponseTimeMs, apiStatusCode,
                dataSizeBytes, itemsCount, s3Bucket, s3Key);
    }

    private static void validateCreate(Long taskId, Long apiResponseTimeMs, Integer apiStatusCode) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        if (apiResponseTimeMs == null) {
            throw new IllegalArgumentException("API response time cannot be null");
        }
        if (apiStatusCode == null) {
            throw new IllegalArgumentException("API status code cannot be null");
        }
    }

}
