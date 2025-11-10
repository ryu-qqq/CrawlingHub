package com.ryuqq.crawlinghub.application.schedule.usecase;

/**
 * Exception thrown when invalid parameter type is provided
 */
public class InvalidParamTypeException extends RuntimeException {

    public InvalidParamTypeException(String paramType) {
        super(String.format("Invalid parameter type: '%s'. Allowed values: %s",
                paramType, getAllowedValues()));
    }

    public InvalidParamTypeException(String paramType, Throwable cause) {
        super(String.format("Invalid parameter type: '%s'. Allowed values: %s",
                paramType, getAllowedValues()), cause);
    }

    private static String getAllowedValues() {
        return String.join(", ", "STRING", "INTEGER", "LONG", "DOUBLE", "BOOLEAN", "DATE", "DATETIME");
    }
}
