package com.ryuqq.crawlinghub.application.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("application")
@DisplayName("JsonNodeReader 단위 테스트")
class JsonNodeReaderTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Nested
    @DisplayName("getAsText()")
    class GetAsText {

        @Test
        @DisplayName("[성공] 텍스트 필드 읽기")
        void shouldReturnTextValue() {
            ObjectNode node = MAPPER.createObjectNode().put("name", "테스트");
            assertThat(JsonNodeReader.getAsText(node, "name")).isEqualTo("테스트");
        }

        @Test
        @DisplayName("[null] 필드 없음 → null")
        void shouldReturnNullWhenFieldMissing() {
            ObjectNode node = MAPPER.createObjectNode();
            assertThat(JsonNodeReader.getAsText(node, "name")).isNull();
        }

        @Test
        @DisplayName("[null] null 값 → null")
        void shouldReturnNullWhenValueIsNull() {
            ObjectNode node = MAPPER.createObjectNode();
            node.putNull("name");
            assertThat(JsonNodeReader.getAsText(node, "name")).isNull();
        }
    }

    @Nested
    @DisplayName("getAsTextOrDefault()")
    class GetAsTextOrDefault {

        @Test
        @DisplayName("[성공] 값 있으면 해당 값")
        void shouldReturnValue() {
            ObjectNode node = MAPPER.createObjectNode().put("brand", "Nike");
            assertThat(JsonNodeReader.getAsTextOrDefault(node, "brand", "Unknown"))
                    .isEqualTo("Nike");
        }

        @Test
        @DisplayName("[기본값] 필드 없으면 기본값")
        void shouldReturnDefaultWhenMissing() {
            ObjectNode node = MAPPER.createObjectNode();
            assertThat(JsonNodeReader.getAsTextOrDefault(node, "brand", "Unknown"))
                    .isEqualTo("Unknown");
        }
    }

    @Nested
    @DisplayName("getAsLong()")
    class GetAsLong {

        @Test
        @DisplayName("[성공] 숫자 필드 읽기")
        void shouldReturnLongFromNumber() {
            ObjectNode node = MAPPER.createObjectNode().put("id", 12345L);
            assertThat(JsonNodeReader.getAsLong(node, "id")).isEqualTo(12345L);
        }

        @Test
        @DisplayName("[성공] 문자열 숫자 → Long 변환")
        void shouldParseLongFromString() {
            ObjectNode node = MAPPER.createObjectNode().put("id", "67890");
            assertThat(JsonNodeReader.getAsLong(node, "id")).isEqualTo(67890L);
        }

        @Test
        @DisplayName("[null] 유효하지 않은 문자열 → null")
        void shouldReturnNullForInvalidString() {
            ObjectNode node = MAPPER.createObjectNode().put("id", "abc");
            assertThat(JsonNodeReader.getAsLong(node, "id")).isNull();
        }

        @Test
        @DisplayName("[null] 필드 없음 → null")
        void shouldReturnNullWhenMissing() {
            ObjectNode node = MAPPER.createObjectNode();
            assertThat(JsonNodeReader.getAsLong(node, "id")).isNull();
        }
    }

    @Nested
    @DisplayName("getAsLongOrDefault()")
    class GetAsLongOrDefault {

        @Test
        @DisplayName("[성공] 값 있으면 해당 값")
        void shouldReturnValue() {
            ObjectNode node = MAPPER.createObjectNode().put("count", 100L);
            assertThat(JsonNodeReader.getAsLongOrDefault(node, "count", 0)).isEqualTo(100L);
        }

        @Test
        @DisplayName("[기본값] 필드 없으면 기본값")
        void shouldReturnDefaultWhenMissing() {
            ObjectNode node = MAPPER.createObjectNode();
            assertThat(JsonNodeReader.getAsLongOrDefault(node, "count", -1)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("getAsIntOrDefault()")
    class GetAsIntOrDefault {

        @Test
        @DisplayName("[성공] 숫자 필드 읽기")
        void shouldReturnIntFromNumber() {
            ObjectNode node = MAPPER.createObjectNode().put("price", 50000);
            assertThat(JsonNodeReader.getAsIntOrDefault(node, "price", 0)).isEqualTo(50000);
        }

        @Test
        @DisplayName("[성공] 문자열 숫자 → int 변환")
        void shouldParseIntFromString() {
            ObjectNode node = MAPPER.createObjectNode().put("price", "12345");
            assertThat(JsonNodeReader.getAsIntOrDefault(node, "price", 0)).isEqualTo(12345);
        }

        @Test
        @DisplayName("[기본값] 유효하지 않은 문자열 → 기본값")
        void shouldReturnDefaultForInvalidString() {
            ObjectNode node = MAPPER.createObjectNode().put("price", "abc");
            assertThat(JsonNodeReader.getAsIntOrDefault(node, "price", -1)).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("getAsBooleanOrDefault()")
    class GetAsBooleanOrDefault {

        @Test
        @DisplayName("[성공] boolean 필드 읽기")
        void shouldReturnBooleanValue() {
            ObjectNode node = MAPPER.createObjectNode().put("active", true);
            assertThat(JsonNodeReader.getAsBooleanOrDefault(node, "active", false)).isTrue();
        }

        @Test
        @DisplayName("[기본값] 필드 없으면 기본값")
        void shouldReturnDefaultWhenMissing() {
            ObjectNode node = MAPPER.createObjectNode();
            assertThat(JsonNodeReader.getAsBooleanOrDefault(node, "active", true)).isTrue();
        }
    }

    @Nested
    @DisplayName("getAsStringList()")
    class GetAsStringList {

        @Test
        @DisplayName("[성공] 문자열 배열 읽기")
        void shouldReturnStringList() {
            ObjectNode node = MAPPER.createObjectNode();
            ArrayNode urls = node.putArray("imageUrls");
            urls.add("https://img1.jpg");
            urls.add("https://img2.jpg");

            List<String> result = JsonNodeReader.getAsStringList(node, "imageUrls");
            assertThat(result).containsExactly("https://img1.jpg", "https://img2.jpg");
        }

        @Test
        @DisplayName("[빈 리스트] 필드 없으면 빈 리스트")
        void shouldReturnEmptyListWhenMissing() {
            ObjectNode node = MAPPER.createObjectNode();
            assertThat(JsonNodeReader.getAsStringList(node, "imageUrls")).isEmpty();
        }

        @Test
        @DisplayName("[필터링] null/빈 문자열 제외")
        void shouldFilterBlankEntries() {
            ObjectNode node = MAPPER.createObjectNode();
            ArrayNode urls = node.putArray("imageUrls");
            urls.add("https://img1.jpg");
            urls.add("");
            urls.add("https://img2.jpg");
            urls.addNull();

            List<String> result = JsonNodeReader.getAsStringList(node, "imageUrls");
            assertThat(result).containsExactly("https://img1.jpg", "https://img2.jpg");
        }

        @Test
        @DisplayName("[빈 리스트] 배열이 아니면 빈 리스트")
        void shouldReturnEmptyListWhenNotArray() {
            ObjectNode node = MAPPER.createObjectNode().put("imageUrls", "not-an-array");
            assertThat(JsonNodeReader.getAsStringList(node, "imageUrls")).isEmpty();
        }
    }
}
