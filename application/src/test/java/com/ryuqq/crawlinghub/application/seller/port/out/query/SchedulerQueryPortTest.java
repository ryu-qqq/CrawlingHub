package com.ryuqq.crawlinghub.application.seller.port.out.query;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SchedulerQueryPort 명세")
class SchedulerQueryPortTest {

    @Test
    @DisplayName("SchedulerQueryPort는 인터페이스여야 한다")
    void shouldBeInterface() {
        assertThat(SchedulerQueryPort.class.isInterface())
            .as("SchedulerQueryPort must be declared as interface")
            .isTrue();
    }

    @Nested
    @DisplayName("필수 조회 메서드 시그니처")
    class MethodSignatureTest {

        @Test
        @DisplayName("활성 스케줄러 수를 조회할 수 있어야 한다")
        void shouldCountActiveSchedulersBySellerId() throws NoSuchMethodException {
            Method method = SchedulerQueryPort.class.getMethod("countActiveSchedulersBySellerId", Long.class);

            assertThat(method.getReturnType())
                .as("countActiveSchedulersBySellerId must return primitive int")
                .isEqualTo(Integer.TYPE);
        }

        @Test
        @DisplayName("전체 스케줄러 수를 조회할 수 있어야 한다")
        void shouldCountTotalSchedulersBySellerId() throws NoSuchMethodException {
            Method method = SchedulerQueryPort.class.getMethod("countTotalSchedulersBySellerId", Long.class);

            assertThat(method.getReturnType())
                .as("countTotalSchedulersBySellerId must return primitive int")
                .isEqualTo(Integer.TYPE);
        }

        @Test
        @DisplayName("활성 스케줄러 ID 목록을 조회할 수 있어야 한다")
        void shouldFindActiveSchedulerIdsBySellerId() throws NoSuchMethodException {
            Method method = SchedulerQueryPort.class.getMethod("findActiveSchedulerIdsBySellerId", Long.class);

            assertThat(method.getReturnType())
                .as("findActiveSchedulerIdsBySellerId must return List<Long>")
                .isEqualTo(List.class);
        }
    }
}

