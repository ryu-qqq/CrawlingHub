package com.ryuqq.crawlinghub.application.scheduler.dto.command;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.scheduler.fixture.command.UpdateSchedulerCommandFixture;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UpdateSchedulerCommandTest {

    @Test
    @DisplayName("should create update command with optional fields")
    void shouldCreateUpdateCommand() {
        UpdateSchedulerCommand command = UpdateSchedulerCommandFixture.create();

        assertThat(command.schedulerId()).isEqualTo(10L);
        assertThat(command.schedulerName()).isEqualTo("weekly-crawling");
        assertThat(command.cronExpression()).isEqualTo("cron(0 0 * * ? *)");
        assertThat(command.status()).isEqualTo(SchedulerStatus.ACTIVE);
    }
}

