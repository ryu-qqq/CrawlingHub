package com.ryuqq.crawlinghub.adapter.in.web.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Utility class for duration formatting
 * Provides consistent time formatting across all DTOs
 */
public final class DurationFormatter {

    private DurationFormatter() {
        // Prevent instantiation
    }

    /**
     * Calculate duration between start and end times
     * Returns formatted string like "5m 30s" or "2h 15m 30s"
     *
     * @param startedAt start time
     * @param completedAt end time (uses current time if null)
     * @return formatted duration string or null if startedAt is null
     */
    public static String formatDuration(LocalDateTime startedAt, LocalDateTime completedAt) {
        if (startedAt == null) {
            return null;
        }

        LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
        Duration duration = Duration.between(startedAt, endTime);

        long totalSeconds = duration.getSeconds();
        if (totalSeconds < 0) {
            return "0s";
        }

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return seconds + "s";
        }
    }

    /**
     * Calculate short duration format (minutes and seconds only)
     * Returns formatted string like "5m 30s"
     *
     * @param startedAt start time
     * @param completedAt end time (uses current time if null)
     * @return formatted duration string or null if startedAt is null
     */
    public static String formatShortDuration(LocalDateTime startedAt, LocalDateTime completedAt) {
        if (startedAt == null) {
            return null;
        }

        LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
        Duration duration = Duration.between(startedAt, endTime);

        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();

        return String.format("%dm %ds", minutes, seconds);
    }
}
