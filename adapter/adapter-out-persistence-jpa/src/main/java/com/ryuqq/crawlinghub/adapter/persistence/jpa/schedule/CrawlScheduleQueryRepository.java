package com.ryuqq.crawlinghub.adapter.persistence.jpa.schedule;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Query Repository for CrawlSchedule
 * Handles complex queries using JDBC Template for better performance
 * Follows CQRS pattern by separating query operations from command operations
 */
@Repository
public class CrawlScheduleQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public CrawlScheduleQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Finds schedules by workflow ID
     */
    public List<CrawlScheduleEntity> findByWorkflowId(Long workflowId) {
        String sql = """
                SELECT schedule_id, workflow_id, schedule_name, cron_expression,
                       timezone, is_enabled, eventbridge_rule_name, next_execution_time,
                       created_at, updated_at
                FROM crawl_schedule
                WHERE workflow_id = ?
                ORDER BY schedule_id DESC
                """;

        return jdbcTemplate.query(sql, scheduleRowMapper(), workflowId);
    }

    /**
     * Finds schedules by enabled status
     */
    public List<CrawlScheduleEntity> findByIsEnabled(boolean isEnabled) {
        String sql = """
                SELECT schedule_id, workflow_id, schedule_name, cron_expression,
                       timezone, is_enabled, eventbridge_rule_name, next_execution_time,
                       created_at, updated_at
                FROM crawl_schedule
                WHERE is_enabled = ?
                ORDER BY schedule_id DESC
                """;

        return jdbcTemplate.query(sql, scheduleRowMapper(), isEnabled);
    }

    /**
     * Finds schedules by workflow ID and enabled status
     */
    public List<CrawlScheduleEntity> findByWorkflowIdAndIsEnabled(Long workflowId, boolean isEnabled) {
        String sql = """
                SELECT schedule_id, workflow_id, schedule_name, cron_expression,
                       timezone, is_enabled, eventbridge_rule_name, next_execution_time,
                       created_at, updated_at
                FROM crawl_schedule
                WHERE workflow_id = ? AND is_enabled = ?
                ORDER BY schedule_id DESC
                """;

        return jdbcTemplate.query(sql, scheduleRowMapper(), workflowId, isEnabled);
    }

    /**
     * Finds all schedules
     */
    public List<CrawlScheduleEntity> findAll() {
        String sql = """
                SELECT schedule_id, workflow_id, schedule_name, cron_expression,
                       timezone, is_enabled, eventbridge_rule_name, next_execution_time,
                       created_at, updated_at
                FROM crawl_schedule
                ORDER BY schedule_id DESC
                """;

        return jdbcTemplate.query(sql, scheduleRowMapper());
    }

    /**
     * Counts schedules by workflow ID
     */
    public long countByWorkflowId(Long workflowId) {
        String sql = "SELECT COUNT(*) FROM crawl_schedule WHERE workflow_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, workflowId);
        return count != null ? count : 0L;
    }

    /**
     * Finds a schedule by ID with input parameters (for complex join query)
     * Returns null if schedule not found
     */
    public CrawlScheduleEntity findByIdWithInputParams(Long scheduleId) {
        String sql = """
                SELECT schedule_id, workflow_id, schedule_name, cron_expression,
                       timezone, is_enabled, eventbridge_rule_name, next_execution_time,
                       created_at, updated_at
                FROM crawl_schedule
                WHERE schedule_id = ?
                """;

        List<CrawlScheduleEntity> results = jdbcTemplate.query(sql, scheduleRowMapper(), scheduleId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * RowMapper for CrawlScheduleEntity
     */
    private RowMapper<CrawlScheduleEntity> scheduleRowMapper() {
        return (rs, rowNum) -> CrawlScheduleEntity.builder()
                .scheduleId(rs.getLong("schedule_id"))
                .workflowId(rs.getLong("workflow_id"))
                .scheduleName(rs.getString("schedule_name"))
                .cronExpression(rs.getString("cron_expression"))
                .timezone(rs.getString("timezone"))
                .isEnabled(rs.getBoolean("is_enabled"))
                .eventbridgeRuleName(rs.getString("eventbridge_rule_name"))
                .nextExecutionTime(getLocalDateTime(rs, "next_execution_time"))
                .build();
    }

    /**
     * Helper method to safely extract LocalDateTime from ResultSet
     */
    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        var timestamp = rs.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}
