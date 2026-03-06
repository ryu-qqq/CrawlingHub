package com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto;

public record FailureDetailDto(String syncType, String errorMessage, long count) {}
