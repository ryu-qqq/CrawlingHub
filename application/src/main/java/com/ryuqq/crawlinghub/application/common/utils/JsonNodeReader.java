package com.ryuqq.crawlinghub.application.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;

/**
 * JsonNode 필드 읽기 유틸리티
 *
 * <p>Jackson {@link JsonNode}에서 필드 값을 안전하게 추출하는 정적 메서드 모음. null/missing 노드를 안전하게 처리하고, 문자열-숫자 간 자동
 * 변환을 지원합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public final class JsonNodeReader {

    private JsonNodeReader() {}

    /**
     * 텍스트 필드 읽기
     *
     * @param node JSON 노드
     * @param fieldName 필드명
     * @return 필드 값 (null/missing 시 null)
     */
    public static String getAsText(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        return fieldNode.asText();
    }

    /**
     * 텍스트 필드 읽기 (기본값 지원)
     *
     * @param node JSON 노드
     * @param fieldName 필드명
     * @param defaultValue 기본값
     * @return 필드 값 (null/missing 시 기본값)
     */
    public static String getAsTextOrDefault(JsonNode node, String fieldName, String defaultValue) {
        String value = getAsText(node, fieldName);
        return value != null ? value : defaultValue;
    }

    /**
     * Long 필드 읽기 (문자열 자동 변환 지원)
     *
     * @param node JSON 노드
     * @param fieldName 필드명
     * @return 필드 값 (null/missing/변환실패 시 null)
     */
    public static Long getAsLong(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        if (fieldNode.isNumber()) {
            return fieldNode.asLong();
        }
        if (fieldNode.isTextual()) {
            try {
                return Long.parseLong(fieldNode.asText());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * long 필드 읽기 (기본값 지원, 문자열 자동 변환)
     *
     * @param node JSON 노드
     * @param fieldName 필드명
     * @param defaultValue 기본값
     * @return 필드 값 (null/missing/변환실패 시 기본값)
     */
    public static long getAsLongOrDefault(JsonNode node, String fieldName, long defaultValue) {
        Long value = getAsLong(node, fieldName);
        return value != null ? value : defaultValue;
    }

    /**
     * int 필드 읽기 (기본값 지원, 문자열 자동 변환)
     *
     * @param node JSON 노드
     * @param fieldName 필드명
     * @param defaultValue 기본값
     * @return 필드 값 (null/missing/변환실패 시 기본값)
     */
    public static int getAsIntOrDefault(JsonNode node, String fieldName, int defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return defaultValue;
        }
        if (fieldNode.isNumber()) {
            return fieldNode.asInt();
        }
        if (fieldNode.isTextual()) {
            try {
                return Integer.parseInt(fieldNode.asText());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * boolean 필드 읽기 (기본값 지원)
     *
     * @param node JSON 노드
     * @param fieldName 필드명
     * @param defaultValue 기본값
     * @return 필드 값 (null/missing 시 기본값)
     */
    public static boolean getAsBooleanOrDefault(
            JsonNode node, String fieldName, boolean defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return defaultValue;
        }
        return fieldNode.asBoolean(defaultValue);
    }

    /**
     * 문자열 배열 필드 읽기
     *
     * <p>배열 내 null/빈 문자열은 필터링합니다.
     *
     * @param node JSON 노드
     * @param fieldName 배열 필드명
     * @return 문자열 리스트 (배열 없으면 빈 리스트)
     */
    public static List<String> getAsStringList(JsonNode node, String fieldName) {
        List<String> result = new ArrayList<>();
        JsonNode arrayNode = node.get(fieldName);
        if (arrayNode != null && arrayNode.isArray()) {
            for (JsonNode itemNode : arrayNode) {
                if (itemNode.isTextual() && !itemNode.asText().isBlank()) {
                    result.add(itemNode.asText());
                }
            }
        }
        return result;
    }
}
