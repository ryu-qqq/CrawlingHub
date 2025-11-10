package com.ryuqq.crawlinghub.application.schedule.usecase;

import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ScheduleFilter
 *
 * @author Sangwon Ryu (ryuqq@company.com)
 * @since 2025-10-14
 */
class ScheduleFilterTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTest {

        @Test
        @DisplayName("empty() creates filter with no criteria")
        void empty_CreatesEmptyFilter() {
            // when
            ScheduleFilter filter = ScheduleFilter.empty();

            // then
            assertThat(filter.workflowId()).isNull();
            assertThat(filter.isEnabled()).isNull();
            assertThat(filter.hasAnyFilter()).isFalse();
        }

        @Test
        @DisplayName("byWorkflowId() creates filter with workflow ID only")
        void byWorkflowId_CreatesWorkflowFilter() {
            // given
            Long workflowId = 100L;

            // when
            ScheduleFilter filter = ScheduleFilter.byWorkflowId(workflowId);

            // then
            assertThat(filter.workflowId()).isEqualTo(workflowId);
            assertThat(filter.isEnabled()).isNull();
            assertThat(filter.hasWorkflowId()).isTrue();
            assertThat(filter.hasIsEnabled()).isFalse();
            assertThat(filter.hasAnyFilter()).isTrue();
            assertThat(filter.hasBothFilters()).isFalse();
        }

        @Test
        @DisplayName("byIsEnabled() creates filter with enabled status only")
        void byIsEnabled_CreatesEnabledStatusFilter() {
            // given
            Boolean isEnabled = true;

            // when
            ScheduleFilter filter = ScheduleFilter.byIsEnabled(isEnabled);

            // then
            assertThat(filter.workflowId()).isNull();
            assertThat(filter.isEnabled()).isEqualTo(isEnabled);
            assertThat(filter.hasWorkflowId()).isFalse();
            assertThat(filter.hasIsEnabled()).isTrue();
            assertThat(filter.hasAnyFilter()).isTrue();
            assertThat(filter.hasBothFilters()).isFalse();
        }

        @Test
        @DisplayName("constructor with both parameters creates complete filter")
        void constructor_CreatesBothFilter() {
            // given
            Long workflowId = 100L;
            Boolean isEnabled = true;

            // when
            ScheduleFilter filter = new ScheduleFilter(workflowId, isEnabled);

            // then
            assertThat(filter.workflowId()).isEqualTo(workflowId);
            assertThat(filter.isEnabled()).isEqualTo(isEnabled);
            assertThat(filter.hasWorkflowId()).isTrue();
            assertThat(filter.hasIsEnabled()).isTrue();
            assertThat(filter.hasAnyFilter()).isTrue();
            assertThat(filter.hasBothFilters()).isTrue();
        }
    }

    @Nested
    @DisplayName("Filter Checking Methods")
    class FilterCheckingMethodsTest {

        @Test
        @DisplayName("hasWorkflowId() returns true when workflow ID is set")
        void hasWorkflowId_ReturnsTrueWhenSet() {
            // given
            ScheduleFilter filter = new ScheduleFilter(100L, null);

            // when & then
            assertThat(filter.hasWorkflowId()).isTrue();
        }

        @Test
        @DisplayName("hasWorkflowId() returns false when workflow ID is null")
        void hasWorkflowId_ReturnsFalseWhenNull() {
            // given
            ScheduleFilter filter = new ScheduleFilter(null, true);

            // when & then
            assertThat(filter.hasWorkflowId()).isFalse();
        }

        @Test
        @DisplayName("hasIsEnabled() returns true when enabled status is set")
        void hasIsEnabled_ReturnsTrueWhenSet() {
            // given
            ScheduleFilter filter = new ScheduleFilter(null, false);

            // when & then
            assertThat(filter.hasIsEnabled()).isTrue();
        }

        @Test
        @DisplayName("hasIsEnabled() returns false when enabled status is null")
        void hasIsEnabled_ReturnsFalseWhenNull() {
            // given
            ScheduleFilter filter = new ScheduleFilter(100L, null);

            // when & then
            assertThat(filter.hasIsEnabled()).isFalse();
        }

        @Test
        @DisplayName("hasAnyFilter() returns true when at least one filter is set")
        void hasAnyFilter_ReturnsTrueWhenAtLeastOneSet() {
            // when & then
            assertThat(new ScheduleFilter(100L, null).hasAnyFilter()).isTrue();
            assertThat(new ScheduleFilter(null, true).hasAnyFilter()).isTrue();
            assertThat(new ScheduleFilter(100L, true).hasAnyFilter()).isTrue();
        }

        @Test
        @DisplayName("hasAnyFilter() returns false when no filter is set")
        void hasAnyFilter_ReturnsFalseWhenNoneSet() {
            // given
            ScheduleFilter filter = new ScheduleFilter(null, null);

            // when & then
            assertThat(filter.hasAnyFilter()).isFalse();
        }

        @Test
        @DisplayName("hasBothFilters() returns true only when both filters are set")
        void hasBothFilters_ReturnsTrueWhenBothSet() {
            // when & then
            assertThat(new ScheduleFilter(100L, true).hasBothFilters()).isTrue();
            assertThat(new ScheduleFilter(100L, null).hasBothFilters()).isFalse();
            assertThat(new ScheduleFilter(null, true).hasBothFilters()).isFalse();
            assertThat(new ScheduleFilter(null, null).hasBothFilters()).isFalse();
        }
    }

    @Nested
    @DisplayName("Getter Methods")
    class GetterMethodsTest {

        @Test
        @DisplayName("getWorkflowId() returns Optional with WorkflowId when set")
        void getWorkflowId_ReturnsOptionalWhenSet() {
            // given
            Long workflowIdValue = 100L;
            ScheduleFilter filter = new ScheduleFilter(workflowIdValue, null);

            // when & then
            assertThat(filter.getWorkflowId())
                    .isPresent()
                    .hasValueSatisfying(workflowId ->
                            assertThat(workflowId.value()).isEqualTo(workflowIdValue)
                    );
        }

        @Test
        @DisplayName("getWorkflowId() returns empty Optional when null")
        void getWorkflowId_ReturnsEmptyWhenNull() {
            // given
            ScheduleFilter filter = new ScheduleFilter(null, true);

            // when & then
            assertThat(filter.getWorkflowId()).isEmpty();
        }

        @Test
        @DisplayName("getIsEnabled() returns Optional with boolean when set")
        void getIsEnabled_ReturnsOptionalWhenSet() {
            // given
            Boolean isEnabled = true;
            ScheduleFilter filter = new ScheduleFilter(null, isEnabled);

            // when & then
            assertThat(filter.getIsEnabled())
                    .isPresent()
                    .hasValue(isEnabled);
        }

        @Test
        @DisplayName("getIsEnabled() returns empty Optional when null")
        void getIsEnabled_ReturnsEmptyWhenNull() {
            // given
            ScheduleFilter filter = new ScheduleFilter(100L, null);

            // when & then
            assertThat(filter.getIsEnabled()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTest {

        @Test
        @DisplayName("handles large workflow ID")
        void handlesLargeWorkflowId() {
            // given
            Long largeId = Long.MAX_VALUE;
            ScheduleFilter filter = new ScheduleFilter(largeId, null);

            // when & then
            assertThat(filter.hasWorkflowId()).isTrue();
            assertThat(filter.getWorkflowId())
                    .isPresent()
                    .hasValueSatisfying(workflowId ->
                            assertThat(workflowId.value()).isEqualTo(largeId)
                    );
        }

        @Test
        @DisplayName("handles false enabled status")
        void handlesFalseEnabledStatus() {
            // given
            ScheduleFilter filter = new ScheduleFilter(null, false);

            // when & then
            assertThat(filter.hasIsEnabled()).isTrue();
            assertThat(filter.getIsEnabled())
                    .isPresent()
                    .hasValue(false);
        }

        @Test
        @DisplayName("handles valid workflow ID with false enabled status")
        void handlesValidWorkflowIdWithFalseEnabledStatus() {
            // given
            ScheduleFilter filter = new ScheduleFilter(100L, false);

            // when & then
            assertThat(filter.hasWorkflowId()).isTrue();
            assertThat(filter.hasIsEnabled()).isTrue();
            assertThat(filter.hasBothFilters()).isTrue();
            assertThat(filter.getIsEnabled())
                    .isPresent()
                    .hasValue(false);
        }
    }
}
