package com.ryuqq.crawlinghub.application.port.out.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchedulerCommandPortTest {

    @Test
    @DisplayName("SchedulerCommandPort should expose save/delete operations")
    void shouldDefineSchedulerCommandPortMethods() {
        List<String> methodNames = extractMethodNames(SchedulerPersistencePort.class);

        assertThat(methodNames).contains("saveScheduler", "deleteScheduler");
    }

    @Test
    @DisplayName("OutboxEventCommandPort should manage outbox lifecycle")
    void shouldDefineOutboxEventCommandPortMethods() {
        List<String> methodNames = extractMethodNames(OutboxEventPersistencePort.class);

        assertThat(methodNames).contains("saveOutboxEvent", "deleteOutboxEvents");
    }

    @Test
    @DisplayName("SchedulerHistoryCommandPort should persist scheduler history")
    void shouldDefineSchedulerHistoryCommandPortMethods() {
        List<String> methodNames = extractMethodNames(SchedulerHistoryPersistencePort.class);

        assertThat(methodNames).contains("saveSchedulerHistory");
    }

    private List<String> extractMethodNames(Class<?> portType) {
        return Arrays.stream(portType.getDeclaredMethods())
            .map(Method::getName)
            .toList();
    }
}

