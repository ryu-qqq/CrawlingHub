package com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto;

public record OutboxStatusCountDto(String outboxType, String status, long count) {}
