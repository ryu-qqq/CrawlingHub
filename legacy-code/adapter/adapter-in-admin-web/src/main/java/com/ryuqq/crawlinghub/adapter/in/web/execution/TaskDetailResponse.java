package com.ryuqq.crawlinghub.adapter.in.web.execution;

import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import com.ryuqq.crawlinghub.domain.task.CrawlTask;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for task detail
 * Contains comprehensive task information including parameters and results
 */
public record TaskDetailResponse(
        Long taskId,
        String taskName,
        Long stepId,
        String stepName,
        TaskStatus status,
        Integer retryCount,
        Integer maxRetryCount,
        List<InputParam> inputParams,
        List<OutputData> outputData,
        ResultMetadata resultMetadata,
        List<AttemptInfo> attempts,
        LocalDateTime queuedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String errorMessage
) {
    public record InputParam(
            String paramKey,
            String paramValue,
            String paramType
    ) {}

    public record OutputData(
            String outputKey,
            String outputValue,
            String outputType
    ) {}

    public record ResultMetadata(
            Integer apiResponseTime,
            Integer apiStatusCode,
            Long dataSizeBytes,
            Integer itemsCount,
            String s3Bucket,
            String s3Key
    ) {}

    public record AttemptInfo(
            Integer attemptNumber,
            TaskStatus status,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            Integer apiResponseTime,
            String errorMessage
    ) {}

    public static TaskDetailResponse from(
            CrawlTask task,
            String stepName,
            List<InputParam> inputParams,
            List<OutputData> outputData,
            ResultMetadata resultMetadata,
            List<AttemptInfo> attempts
    ) {
        return new TaskDetailResponse(
                task.getTaskId() != null ? task.getTaskId().value() : null,
                task.getTaskName(),
                task.getStepId() != null ? task.getStepId().value() : null,
                stepName,
                task.getStatus(),
                task.getRetryCount(),
                task.getMaxRetryCount(),
                inputParams,
                outputData,
                resultMetadata,
                attempts,
                task.getQueuedAt(),
                task.getStartedAt(),
                task.getCompletedAt(),
                task.getErrorMessage()
        );
    }
}
