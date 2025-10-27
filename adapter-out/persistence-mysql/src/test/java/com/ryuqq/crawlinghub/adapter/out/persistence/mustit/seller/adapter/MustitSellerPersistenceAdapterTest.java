package com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.entity.MustitSellerEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.mapper.MustitSellerMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.mustit.seller.repository.MustitSellerJpaRepository;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlInterval;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MustitSellerPersistenceAdapter 단위 테스트
 *
 * @author Claude
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class MustitSellerPersistenceAdapterTest {

    @Mock
    private MustitSellerJpaRepository jpaRepository;

    @Mock
    private MustitSellerMapper mapper;

    @InjectMocks
    private MustitSellerPersistenceAdapter adapter;

    private MustitSeller testSeller;
    private MustitSellerEntity testEntity;

    @BeforeEach
    void setUp() {
        CrawlInterval crawlInterval = new CrawlInterval(CrawlIntervalType.DAILY, 1);
        testSeller = new MustitSeller("SELLER001", "Test Seller", crawlInterval);

        MustitSellerEntity.BasicInfo basicInfo = new MustitSellerEntity.BasicInfo(
                "SELLER001",
                "Test Seller",
                true
        );
        MustitSellerEntity.CrawlInfo crawlInfo = new MustitSellerEntity.CrawlInfo(
                MustitSellerEntity.IntervalType.DAILY,
                1,
                "0 0 0/1 * ? *"
        );
        testEntity = new MustitSellerEntity(basicInfo, crawlInfo);
    }

    @Test
    @DisplayName("셀러 저장 - 성공")
    void save_ShouldSaveSellerSuccessfully() {
        // Given
        when(mapper.toEntity(testSeller)).thenReturn(testEntity);
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testSeller);

        // When
        MustitSeller savedSeller = adapter.save(testSeller);

        // Then
        assertThat(savedSeller).isNotNull();
        assertThat(savedSeller.getSellerId()).isEqualTo("SELLER001");

        verify(mapper).toEntity(testSeller);
        verify(jpaRepository).save(testEntity);
        verify(mapper).toDomain(testEntity);
    }

    @Test
    @DisplayName("셀러 저장 - null 전달 시 예외 발생")
    void save_ShouldThrowExceptionWhenSellerIsNull() {
        // When & Then
        assertThatThrownBy(() -> adapter.save(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("seller must not be null");

        verify(mapper, never()).toEntity(any());
        verify(jpaRepository, never()).save(any());
    }

    @Test
    @DisplayName("sellerId로 셀러 조회 - 존재하는 경우")
    void findBySellerId_ShouldReturnSellerWhenExists() {
        // Given
        when(jpaRepository.findBySellerId("SELLER001")).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testSeller);

        // When
        Optional<MustitSeller> result = adapter.findBySellerId("SELLER001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSellerId()).isEqualTo("SELLER001");

        verify(jpaRepository).findBySellerId("SELLER001");
        verify(mapper).toDomain(testEntity);
    }

    @Test
    @DisplayName("sellerId로 셀러 조회 - 존재하지 않는 경우")
    void findBySellerId_ShouldReturnEmptyWhenNotExists() {
        // Given
        when(jpaRepository.findBySellerId("NONEXISTENT")).thenReturn(Optional.empty());

        // When
        Optional<MustitSeller> result = adapter.findBySellerId("NONEXISTENT");

        // Then
        assertThat(result).isEmpty();

        verify(jpaRepository).findBySellerId("NONEXISTENT");
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("sellerId로 셀러 조회 - null sellerId 시 예외 발생")
    void findBySellerId_ShouldThrowExceptionWhenSellerIdIsNull() {
        // When & Then
        assertThatThrownBy(() -> adapter.findBySellerId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sellerId must not be null or blank");

        verify(jpaRepository, never()).findBySellerId(any());
    }

    @Test
    @DisplayName("sellerId로 셀러 조회 - 빈 문자열 sellerId 시 예외 발생")
    void findBySellerId_ShouldThrowExceptionWhenSellerIdIsBlank() {
        // When & Then
        assertThatThrownBy(() -> adapter.findBySellerId("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sellerId must not be null or blank");

        verify(jpaRepository, never()).findBySellerId(any());
    }

    @Test
    @DisplayName("sellerId 존재 여부 확인 - 존재하는 경우")
    void existsBySellerId_ShouldReturnTrueWhenExists() {
        // Given
        when(jpaRepository.existsBySellerId("SELLER001")).thenReturn(true);

        // When
        boolean exists = adapter.existsBySellerId("SELLER001");

        // Then
        assertThat(exists).isTrue();
        verify(jpaRepository).existsBySellerId("SELLER001");
    }

    @Test
    @DisplayName("sellerId 존재 여부 확인 - 존재하지 않는 경우")
    void existsBySellerId_ShouldReturnFalseWhenNotExists() {
        // Given
        when(jpaRepository.existsBySellerId("NONEXISTENT")).thenReturn(false);

        // When
        boolean exists = adapter.existsBySellerId("NONEXISTENT");

        // Then
        assertThat(exists).isFalse();
        verify(jpaRepository).existsBySellerId("NONEXISTENT");
    }

    @Test
    @DisplayName("sellerId 존재 여부 확인 - null sellerId 시 예외 발생")
    void existsBySellerId_ShouldThrowExceptionWhenSellerIdIsNull() {
        // When & Then
        assertThatThrownBy(() -> adapter.existsBySellerId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sellerId must not be null or blank");

        verify(jpaRepository, never()).existsBySellerId(any());
    }

    @Test
    @DisplayName("sellerId 존재 여부 확인 - 빈 문자열 sellerId 시 예외 발생")
    void existsBySellerId_ShouldThrowExceptionWhenSellerIdIsBlank() {
        // When & Then
        assertThatThrownBy(() -> adapter.existsBySellerId(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sellerId must not be null or blank");

        verify(jpaRepository, never()).existsBySellerId(any());
    }

    @Test
    @DisplayName("저장 후 반환된 Entity가 Domain으로 올바르게 변환됨")
    void save_ShouldConvertSavedEntityToDomain() {
        // Given
        MustitSellerEntity.BasicInfo basicInfo = new MustitSellerEntity.BasicInfo(
                "SELLER001",
                "Test Seller",
                true
        );
        MustitSellerEntity.CrawlInfo crawlInfo = new MustitSellerEntity.CrawlInfo(
                MustitSellerEntity.IntervalType.DAILY,
                1,
                "0 0 0/1 * ? *"
        );
        MustitSellerEntity savedEntity = new MustitSellerEntity(basicInfo, crawlInfo);

        when(mapper.toEntity(testSeller)).thenReturn(testEntity);
        when(jpaRepository.save(testEntity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(testSeller);

        // When
        MustitSeller result = adapter.save(testSeller);

        // Then
        ArgumentCaptor<MustitSellerEntity> entityCaptor = ArgumentCaptor.forClass(MustitSellerEntity.class);
        verify(jpaRepository).save(entityCaptor.capture());
        verify(mapper).toDomain(savedEntity);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testSeller);
    }
}
