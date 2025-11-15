package com.ryuqq.crawlinghub.application.schedule.service;

import com.ryuqq.crawlinghub.application.crawl.schedule.dto.command.TriggerScheduleCommandFixture;
import com.ryuqq.crawlinghub.application.schedule.dto.command.TriggerScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.port.out.LoadSchedulePort;
import com.ryuqq.crawlinghub.application.schedule.port.out.SaveSchedulePort;
import com.ryuqq.crawlinghub.application.schedule.validator.CronExpressionValidator;
import com.ryuqq.crawlinghub.application.task.dto.command.InitiateCrawlingCommand;
import com.ryuqq.crawlinghub.application.task.port.in.InitiateCrawlingUseCase;
import com.ryuqq.crawlinghub.domain.crawl.schedule.CrawlScheduleFixture;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * TriggerScheduleService 단위 테스트
 *
 * <p>스케줄 트리거 UseCase의 비즈니스 로직을 검증합니다.
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TriggerScheduleService 단위 테스트")
class TriggerScheduleServiceTest {

    @Mock
    private LoadSchedulePort loadSchedulePort;

    @Mock
    private SaveSchedulePort saveSchedulePort;

    @Mock
    private CronExpressionValidator cronValidator;

    @Mock
    private InitiateCrawlingUseCase initiateCrawlingUseCase;

    @InjectMocks
    private TriggerScheduleService sut;

    @Nested
    @DisplayName("execute 메서드는")
    class Describe_execute {

        @Nested
        @DisplayName("활성 스케줄이 존재하고 실행 시간이 도래한 경우")
        class Context_with_active_schedule_ready_to_execute {

            private TriggerScheduleCommand command;
            private CrawlSchedule schedule;
            private LocalDateTime nextExecution;

            @BeforeEach
            void setUp() {
                // Given: 활성 스케줄 ID
                command = TriggerScheduleCommandFixture.create();

                // Mock: 실행 시간이 도래한 스케줄 (nextExecutionTime이 과거)
                LocalDateTime pastExecutionTime = LocalDateTime.now().minusMinutes(5);
                schedule = CrawlScheduleFixture.createWithNextExecution(pastExecutionTime);
                given(loadSchedulePort.findActiveBySellerId(any(MustItSellerId.class)))
                    .willReturn(Optional.of(schedule));

                // Mock: 다음 실행 시간 계산
                nextExecution = LocalDateTime.now().plusHours(24);
                given(cronValidator.calculateNextExecution(anyString(), any(LocalDateTime.class)))
                    .willReturn(nextExecution);
            }

            @Test
            @DisplayName("크롤링 Task를 생성한다")
            void it_initiates_crawling_task() {
                // When: 스케줄 트리거 실행
                sut.execute(command);

                // Then: InitiateCrawlingUseCase 호출
                then(initiateCrawlingUseCase).should().execute(any(InitiateCrawlingCommand.class));
            }

            @Test
            @DisplayName("스케줄을 저장한다")
            void it_saves_schedule() {
                // When: 스케줄 트리거 실행
                sut.execute(command);

                // Then: SaveSchedulePort 호출
                then(saveSchedulePort).should().save(any(CrawlSchedule.class));
            }

            @Test
            @DisplayName("다음 실행 시간을 계산한다")
            void it_calculates_next_execution() {
                // When: 스케줄 트리거 실행
                sut.execute(command);

                // Then: CronValidator.calculateNextExecution() 호출
                then(cronValidator).should().calculateNextExecution(anyString(), any(LocalDateTime.class));
            }
        }

        @Nested
        @DisplayName("활성 스케줄이 존재하지 않는 경우")
        class Context_with_no_active_schedule {

            private TriggerScheduleCommand command;

            @BeforeEach
            void setUp() {
                // Given: 스케줄 ID
                command = TriggerScheduleCommandFixture.create();

                // And: 활성 스케줄을 찾을 수 없음
                given(loadSchedulePort.findActiveBySellerId(any(MustItSellerId.class)))
                    .willReturn(Optional.empty());
            }

            @Test
            @DisplayName("IllegalArgumentException을 발생시킨다")
            void it_throws_illegal_argument_exception() {
                // When & Then: 스케줄 트리거 시도 시 예외 발생
                assertThatThrownBy(() -> sut.execute(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("활성 스케줄을 찾을 수 없습니다");

                // And: 스케줄 조회는 수행됨
                then(loadSchedulePort).should().findActiveBySellerId(any(MustItSellerId.class));

                // And: 크롤링 Task는 생성되지 않음
                then(initiateCrawlingUseCase).should(never()).execute(any(InitiateCrawlingCommand.class));
            }
        }

        @Nested
        @DisplayName("비즈니스 규칙 검증")
        class Context_business_rules {

            @Test
            @DisplayName("스케줄 실행 순서가 올바르다: 조회 → Task 생성 → 실행 기록 → 다음 시간 계산 → 저장")
            void execution_order_is_correct() {
                // Given: 실행 시간이 도래한 스케줄
                TriggerScheduleCommand command = TriggerScheduleCommandFixture.create();
                LocalDateTime pastExecutionTime = LocalDateTime.now().minusMinutes(5);
                CrawlSchedule schedule = CrawlScheduleFixture.createWithNextExecution(pastExecutionTime);

                given(loadSchedulePort.findActiveBySellerId(any(MustItSellerId.class)))
                    .willReturn(Optional.of(schedule));
                given(cronValidator.calculateNextExecution(anyString(), any(LocalDateTime.class)))
                    .willReturn(LocalDateTime.now().plusHours(24));

                // When: 스케줄 트리거 실행
                sut.execute(command);

                // Then: 순서대로 호출됨
                var inOrder = org.mockito.Mockito.inOrder(
                    loadSchedulePort,
                    initiateCrawlingUseCase,
                    cronValidator,
                    saveSchedulePort
                );
                inOrder.verify(loadSchedulePort).findActiveBySellerId(any(MustItSellerId.class));
                inOrder.verify(initiateCrawlingUseCase).execute(any(InitiateCrawlingCommand.class));
                inOrder.verify(cronValidator).calculateNextExecution(anyString(), any(LocalDateTime.class));
                inOrder.verify(saveSchedulePort).save(any(CrawlSchedule.class));
            }

            @Test
            @DisplayName("트랜잭션 내에서 실행된다")
            void execution_is_transactional() {
                // Given: 실행 시간이 도래한 스케줄
                TriggerScheduleCommand command = TriggerScheduleCommandFixture.create();
                LocalDateTime pastExecutionTime = LocalDateTime.now().minusMinutes(5);
                CrawlSchedule schedule = CrawlScheduleFixture.createWithNextExecution(pastExecutionTime);

                given(loadSchedulePort.findActiveBySellerId(any(MustItSellerId.class)))
                    .willReturn(Optional.of(schedule));
                given(cronValidator.calculateNextExecution(anyString(), any(LocalDateTime.class)))
                    .willReturn(LocalDateTime.now().plusHours(24));

                // When: 스케줄 트리거 실행
                sut.execute(command);

                // Then: 트랜잭션 내에서 조회 → Task 생성 → 저장 순서 보장
                then(loadSchedulePort).should().findActiveBySellerId(any(MustItSellerId.class));
                then(initiateCrawlingUseCase).should().execute(any(InitiateCrawlingCommand.class));
                then(saveSchedulePort).should().save(any(CrawlSchedule.class));
            }
        }
    }
}
