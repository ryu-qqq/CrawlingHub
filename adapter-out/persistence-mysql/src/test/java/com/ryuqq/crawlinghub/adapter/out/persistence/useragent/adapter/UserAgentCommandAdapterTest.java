package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.useragent.UserAgentFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.mapper.UserAgentJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.repository.UserAgentJpaRepository;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserAgentCommandAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("UserAgentCommandAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class UserAgentCommandAdapterTest {

    @Mock private UserAgentJpaRepository jpaRepository;

    @Mock private UserAgentJpaEntityMapper mapper;

    private UserAgentCommandAdapter commandAdapter;

    @BeforeEach
    void setUp() {
        commandAdapter = new UserAgentCommandAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("성공 - UserAgent 저장 시 ID 반환")
    void shouldReturnIdWhenPersist() {
        // Given
        UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
        LocalDateTime now = LocalDateTime.now();
        UserAgentJpaEntity entity =
                UserAgentJpaEntity.of(
                        null,
                        "encrypted-token",
                        "Mozilla/5.0",
                        "DESKTOP",
                        UserAgentStatus.AVAILABLE,
                        100,
                        null,
                        0,
                        now,
                        now);
        UserAgentJpaEntity savedEntity =
                UserAgentJpaEntity.of(
                        1L,
                        "encrypted-token",
                        "Mozilla/5.0",
                        "DESKTOP",
                        UserAgentStatus.AVAILABLE,
                        100,
                        null,
                        0,
                        now,
                        now);

        given(mapper.toEntity(userAgent)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(savedEntity);

        // When
        UserAgentId result = commandAdapter.persist(userAgent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.value()).isEqualTo(1L);
        verify(mapper).toEntity(userAgent);
        verify(jpaRepository).save(entity);
    }

    @Test
    @DisplayName("성공 - 기존 UserAgent 수정")
    void shouldUpdateExistingUserAgent() {
        // Given
        UserAgent userAgent = UserAgentFixture.anAvailableUserAgent();
        LocalDateTime now = LocalDateTime.now();
        UserAgentJpaEntity entity =
                UserAgentJpaEntity.of(
                        100L,
                        "encrypted-token",
                        "Mozilla/5.0",
                        "DESKTOP",
                        UserAgentStatus.BLOCKED,
                        50,
                        now,
                        5,
                        now,
                        now);

        given(mapper.toEntity(userAgent)).willReturn(entity);
        given(jpaRepository.save(entity)).willReturn(entity);

        // When
        UserAgentId result = commandAdapter.persist(userAgent);

        // Then
        assertThat(result.value()).isEqualTo(100L);
    }
}
