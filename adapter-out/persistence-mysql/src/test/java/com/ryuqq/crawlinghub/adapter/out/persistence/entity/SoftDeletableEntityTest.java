package com.ryuqq.crawlinghub.adapter.out.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * SoftDeletableEntity 단위 테스트
 *
 * <p>소프트 딜리트 기능 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("entity")
@DisplayName("SoftDeletableEntity 단위 테스트")
class SoftDeletableEntityTest {

    /** 테스트용 구체 클래스 */
    static class TestSoftDeletableEntity extends SoftDeletableEntity {

        private Long id;
        private String name;

        protected TestSoftDeletableEntity() {
            super();
        }

        public TestSoftDeletableEntity(LocalDateTime createdAt, LocalDateTime updatedAt) {
            super(createdAt, updatedAt);
        }

        public TestSoftDeletableEntity(
                LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
            super(createdAt, updatedAt, deletedAt);
        }

        public static TestSoftDeletableEntity createActive(
                LocalDateTime createdAt, LocalDateTime updatedAt) {
            return new TestSoftDeletableEntity(createdAt, updatedAt);
        }

        public static TestSoftDeletableEntity createDeleted(
                LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
            return new TestSoftDeletableEntity(createdAt, updatedAt, deletedAt);
        }
    }

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("성공 - 기본 생성자로 활성 상태 엔티티 생성")
        void shouldCreateActiveEntityWithDefaultConstructor() {
            // Given & When
            TestSoftDeletableEntity entity = new TestSoftDeletableEntity();

            // Then
            assertThat(entity.getDeletedAt()).isNull();
            assertThat(entity.isDeleted()).isFalse();
            assertThat(entity.isActive()).isTrue();
        }

        @Test
        @DisplayName("성공 - 감사 정보 생성자로 활성 상태 엔티티 생성")
        void shouldCreateActiveEntityWithAuditFields() {
            // Given
            LocalDateTime now = LocalDateTime.now();

            // When
            TestSoftDeletableEntity entity = TestSoftDeletableEntity.createActive(now, now);

            // Then
            assertThat(entity.getDeletedAt()).isNull();
            assertThat(entity.isDeleted()).isFalse();
            assertThat(entity.isActive()).isTrue();
        }

        @Test
        @DisplayName("성공 - 전체 필드 생성자로 삭제된 상태 엔티티 생성")
        void shouldCreateDeletedEntityWithAllFields() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime deletedAt = now.plusMinutes(10);

            // When
            TestSoftDeletableEntity entity =
                    TestSoftDeletableEntity.createDeleted(now, now, deletedAt);

            // Then
            assertThat(entity.getDeletedAt()).isEqualTo(deletedAt);
            assertThat(entity.isDeleted()).isTrue();
            assertThat(entity.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("isDeleted 테스트")
    class IsDeletedTests {

        @Test
        @DisplayName("성공 - deletedAt이 null이면 삭제되지 않음")
        void shouldReturnFalseWhenDeletedAtIsNull() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            TestSoftDeletableEntity entity = TestSoftDeletableEntity.createActive(now, now);

            // When
            boolean result = entity.isDeleted();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("성공 - deletedAt이 있으면 삭제됨")
        void shouldReturnTrueWhenDeletedAtIsNotNull() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            TestSoftDeletableEntity entity = TestSoftDeletableEntity.createDeleted(now, now, now);

            // When
            boolean result = entity.isDeleted();

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("isActive 테스트")
    class IsActiveTests {

        @Test
        @DisplayName("성공 - deletedAt이 null이면 활성 상태")
        void shouldReturnTrueWhenDeletedAtIsNull() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            TestSoftDeletableEntity entity = TestSoftDeletableEntity.createActive(now, now);

            // When
            boolean result = entity.isActive();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - deletedAt이 있으면 비활성 상태")
        void shouldReturnFalseWhenDeletedAtIsNotNull() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            TestSoftDeletableEntity entity = TestSoftDeletableEntity.createDeleted(now, now, now);

            // When
            boolean result = entity.isActive();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getDeletedAt 테스트")
    class GetDeletedAtTests {

        @Test
        @DisplayName("성공 - 삭제 시간 반환")
        void shouldReturnDeletedAt() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime deletedAt = now.plusHours(1);
            TestSoftDeletableEntity entity =
                    TestSoftDeletableEntity.createDeleted(now, now, deletedAt);

            // When
            LocalDateTime result = entity.getDeletedAt();

            // Then
            assertThat(result).isEqualTo(deletedAt);
        }
    }
}
