package com.ryuqq.crawlinghub.application.scheduler.dto.command;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.scheduler.fixture.command.RegisterSchedulerCommandFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegisterSchedulerCommandTest {

    @Test
    @DisplayName("should create command with valid data")
    void shouldCreateCommandWithValidData() {
        RegisterSchedulerCommand command = RegisterSchedulerCommandFixture.create();

        assertThat(command.sellerId()).isEqualTo(1L);
        assertThat(command.schedulerName()).isEqualTo("daily-crawling");
        assertThat(command.cronExpression()).isEqualTo("cron(0 0 * * ? *)");
    }
}

