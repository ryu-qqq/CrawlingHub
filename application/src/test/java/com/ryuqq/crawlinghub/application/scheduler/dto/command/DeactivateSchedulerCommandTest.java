package com.ryuqq.crawlinghub.application.scheduler.dto.command;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.scheduler.fixture.command.DeactivateSchedulerCommandFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeactivateSchedulerCommandTest {

    @Test
    @DisplayName("should hold scheduler id for deactivation")
    void shouldCreateDeactivateCommand() {
        DeactivateSchedulerCommand command = DeactivateSchedulerCommandFixture.create();

        assertThat(command.schedulerId()).isEqualTo(15L);
    }
}

