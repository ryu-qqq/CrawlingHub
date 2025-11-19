package com.ryuqq.crawlinghub.application.port.out.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventBridgeClientPortTest {

    @Test
    @DisplayName("should expose EventBridge operations")
    void shouldDefineAwsOperations() {
        List<String> methodNames = extractMethodNames(EventBridgeClientPort.class);

        assertThat(methodNames).containsExactlyInAnyOrder(
            "createRule",
            "updateRule",
            "disableRule"
        );
    }

    private List<String> extractMethodNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
            .map(Method::getName)
            .toList();
    }
}

