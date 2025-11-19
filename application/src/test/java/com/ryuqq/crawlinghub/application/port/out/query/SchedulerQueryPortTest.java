package com.ryuqq.crawlinghub.application.port.out.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchedulerQueryPortTest {

    @Test
    @DisplayName("SchedulerQueryPort should expose scheduler lookups")
    void shouldDefineSchedulerQueryPortMethods() {
        List<String> methodNames = extractMethodNames(SchedulerQueryPort.class);

        assertThat(methodNames).contains(
            "findById",
            "findBySellerIdAndSchedulerName",
            "findBySellerIdAndStatus",
            "findAllBySellerIdAndStatus",
            "countActiveSchedulersBySellerId"
        );
    }

    @Test
    @DisplayName("OutboxEventQueryPort should expose retry related finders")
    void shouldDefineOutboxEventQueryPortMethods() {
        List<String> methodNames = extractMethodNames(OutboxEventQueryPort.class);

        assertThat(methodNames).contains(
            "findByStatusAndRetryCountLessThan",
            "findByStatusAndCreatedAtBefore"
        );
    }

    @Test
    @DisplayName("SchedulerHistoryQueryPort should expose history pagination")
    void shouldDefineSchedulerHistoryQueryPortMethods() {
        List<String> methodNames = extractMethodNames(SchedulerHistoryQueryPort.class);

        assertThat(methodNames).contains("findBySchedulerId");
    }

    @Test
    @DisplayName("SellerQueryPort should expose seller lookup")
    void shouldDefineSellerQueryPortMethods() {
        List<String> methodNames = extractMethodNames(SellerQueryPort.class);

        assertThat(methodNames).contains("findById");
    }

    private List<String> extractMethodNames(Class<?> portType) {
        return Arrays.stream(portType.getDeclaredMethods())
            .map(Method::getName)
            .toList();
    }
}

