package com.ryuqq.crawlinghub.adapter.in.web.schedule;

import com.ryuqq.crawlinghub.application.schedule.usecase.*;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleInputParam;
import com.ryuqq.crawlinghub.domain.workflow.WorkflowId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Crawl Schedule management
 * Thin delegation layer following Hexagonal Architecture
 *
 * Architecture Rules:
 * - NO @Transactional (transaction is in application layer)
 * - Depends ONLY on UseCase interfaces
 * - Request/Response must be Java records
 * - NO Lombok allowed
 * - Controller methods should be thin
 */
@RestController
@RequestMapping("/api/v1/schedules")
public class CrawlScheduleController {

    private final RegisterScheduleUseCase registerScheduleUseCase;
    private final GetScheduleUseCase getScheduleUseCase;
    private final UpdateScheduleUseCase updateScheduleUseCase;
    private final DeleteScheduleUseCase deleteScheduleUseCase;
    private final EnableScheduleUseCase enableScheduleUseCase;
    private final DisableScheduleUseCase disableScheduleUseCase;

    public CrawlScheduleController(
            RegisterScheduleUseCase registerScheduleUseCase,
            GetScheduleUseCase getScheduleUseCase,
            UpdateScheduleUseCase updateScheduleUseCase,
            DeleteScheduleUseCase deleteScheduleUseCase,
            EnableScheduleUseCase enableScheduleUseCase,
            DisableScheduleUseCase disableScheduleUseCase) {
        this.registerScheduleUseCase = registerScheduleUseCase;
        this.getScheduleUseCase = getScheduleUseCase;
        this.updateScheduleUseCase = updateScheduleUseCase;
        this.deleteScheduleUseCase = deleteScheduleUseCase;
        this.enableScheduleUseCase = enableScheduleUseCase;
        this.disableScheduleUseCase = disableScheduleUseCase;
    }

    /**
     * Create a new schedule
     *
     * POST /api/v1/schedules
     *
     * @param request the schedule creation request
     * @return the created schedule response with Location header
     */
    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody CreateScheduleRequest request) {
        // Request → Command
        RegisterScheduleCommand command = request.toCommand();

        // Execute UseCase
        ScheduleId scheduleId = registerScheduleUseCase.execute(command);

        // Fetch created schedule for response
        CrawlSchedule schedule = getScheduleUseCase.getById(scheduleId.value());
        List<ScheduleInputParam> inputParams = getScheduleUseCase.getInputParams(scheduleId.value());

        ScheduleResponse response = ScheduleResponse.from(schedule, inputParams);

        // Build Location header
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{scheduleId}")
                .buildAndExpand(response.scheduleId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Get list of schedules
     *
     * GET /api/v1/schedules
     * Query Parameters:
     * - workflowId (optional): filter by workflow ID
     * - isEnabled (optional): filter by enabled status
     *
     * @param workflowId optional workflow ID filter
     * @param isEnabled optional enabled status filter
     * @return list of schedule summaries
     */
    @GetMapping
    public ResponseEntity<List<ScheduleSummaryResponse>> getSchedules(
            @RequestParam(required = false) Long workflowId,
            @RequestParam(required = false) Boolean isEnabled) {

        List<CrawlSchedule> schedules;

        if (workflowId != null && isEnabled != null) {
            // Filter by both workflow ID and enabled status
            schedules = getScheduleUseCase.getByWorkflowIdAndIsEnabled(workflowId, isEnabled);
        } else if (workflowId != null) {
            // Filter by workflow ID only
            schedules = getScheduleUseCase.getByWorkflowId(workflowId);
        } else if (isEnabled != null) {
            // Filter by enabled status only
            schedules = getScheduleUseCase.getByIsEnabled(isEnabled);
        } else {
            // Get all schedules
            schedules = getScheduleUseCase.getAll();
        }

        // Domain → Response
        List<ScheduleSummaryResponse> response = schedules.stream()
                .map(ScheduleSummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get schedule detail by ID
     *
     * GET /api/v1/schedules/{scheduleId}
     *
     * @param scheduleId the schedule ID
     * @return the schedule detail with input parameters
     */
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> getScheduleDetail(@PathVariable Long scheduleId) {
        // Execute UseCase
        CrawlSchedule schedule = getScheduleUseCase.getById(scheduleId);
        List<ScheduleInputParam> inputParams = getScheduleUseCase.getInputParams(scheduleId);

        // Domain → Response
        ScheduleResponse response = ScheduleResponse.from(schedule, inputParams);

        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing schedule
     *
     * PUT /api/v1/schedules/{scheduleId}
     *
     * @param scheduleId the schedule ID
     * @param request the update request
     * @return no content
     */
    @PutMapping("/{scheduleId}")
    public ResponseEntity<Void> updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody UpdateScheduleRequest request) {

        // Request → Command
        UpdateScheduleCommand command = request.toCommand(scheduleId);

        // Execute UseCase
        updateScheduleUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    /**
     * Enable a schedule
     * Creates EventBridge rule and starts scheduling
     *
     * POST /api/v1/schedules/{scheduleId}/enable
     *
     * @param scheduleId the schedule ID
     * @return no content
     */
    @PostMapping("/{scheduleId}/enable")
    public ResponseEntity<Void> enableSchedule(@PathVariable Long scheduleId) {
        // Execute UseCase
        enableScheduleUseCase.execute(scheduleId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Disable a schedule
     * Disables EventBridge rule without deleting it
     *
     * POST /api/v1/schedules/{scheduleId}/disable
     *
     * @param scheduleId the schedule ID
     * @return no content
     */
    @PostMapping("/{scheduleId}/disable")
    public ResponseEntity<Void> disableSchedule(@PathVariable Long scheduleId) {
        // Execute UseCase
        disableScheduleUseCase.execute(scheduleId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a schedule
     * Removes EventBridge rule and deletes schedule data
     *
     * DELETE /api/v1/schedules/{scheduleId}
     *
     * @param scheduleId the schedule ID
     * @return no content
     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
        // Execute UseCase
        deleteScheduleUseCase.execute(scheduleId);

        return ResponseEntity.noContent().build();
    }
}
