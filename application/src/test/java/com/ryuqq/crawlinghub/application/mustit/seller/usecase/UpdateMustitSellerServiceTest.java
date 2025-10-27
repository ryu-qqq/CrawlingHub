package com.ryuqq.crawlinghub.application.mustit.seller.usecase;

import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.UpdateMustitSellerCommand;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.LoadMustitSellerPort;
import com.ryuqq.crawlinghub.application.mustit.seller.port.out.SaveMustitSellerPort;
import com.ryuqq.crawlinghub.application.mustit.seller.service.UpdateMustitSellerService;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlInterval;
import com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType;
import com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller;
import com.ryuqq.crawlinghub.domain.mustit.seller.exception.SellerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UpdateMustitSellerUseCase 단위 테스트
 * <p>
 * Port는 Mocking하여 UseCase의 비즈니스 로직만 검증합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateMustitSellerUseCase 단위 테스트")
class UpdateMustitSellerServiceTest {

    @Mock
    private LoadMustitSellerPort loadMustitSellerPort;

    @Mock
    private SaveMustitSellerPort saveMustitSellerPort;

    private UpdateMustitSellerService useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateMustitSellerService(loadMustitSellerPort, saveMustitSellerPort);
    }

    @Test
    @DisplayName("활성화 상태를 변경할 수 있다")
    void updateActiveStatus() {
        // given
        MustitSeller existingSeller = new MustitSeller(
                "SELLER001",
                "Test Seller",
                new CrawlInterval(CrawlIntervalType.DAILY, 1)
        );

        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "SELLER001",
                false, // 비활성화
                null,
                null
        );

        when(loadMustitSellerPort.findBySellerId("SELLER001"))
                .thenReturn(Optional.of(existingSeller));
        when(saveMustitSellerPort.save(any(MustitSeller.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        MustitSeller result = useCase.execute(command);

        // then
        assertThat(result.isActive()).isFalse();

        // Port 호출 검증
        verify(loadMustitSellerPort, times(1)).findBySellerId("SELLER001");
        verify(saveMustitSellerPort, times(1)).save(any(MustitSeller.class));

        // 저장된 셀러 검증
        ArgumentCaptor<MustitSeller> sellerCaptor = ArgumentCaptor.forClass(MustitSeller.class);
        verify(saveMustitSellerPort).save(sellerCaptor.capture());
        MustitSeller savedSeller = sellerCaptor.getValue();

        assertThat(savedSeller.isActive()).isFalse();
    }

    @Test
    @DisplayName("크롤링 주기를 변경할 수 있다")
    void updateCrawlInterval() {
        // given
        MustitSeller existingSeller = new MustitSeller(
                "SELLER001",
                "Test Seller",
                new CrawlInterval(CrawlIntervalType.DAILY, 1)
        );

        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "SELLER001",
                null,
                CrawlIntervalType.HOURLY,
                6
        );

        when(loadMustitSellerPort.findBySellerId("SELLER001"))
                .thenReturn(Optional.of(existingSeller));
        when(saveMustitSellerPort.save(any(MustitSeller.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        MustitSeller result = useCase.execute(command);

        // then
        assertThat(result.getCrawlInterval().getIntervalType()).isEqualTo(CrawlIntervalType.HOURLY);
        assertThat(result.getCrawlInterval().getIntervalValue()).isEqualTo(6);

        // Port 호출 검증
        verify(loadMustitSellerPort, times(1)).findBySellerId("SELLER001");
        verify(saveMustitSellerPort, times(1)).save(any(MustitSeller.class));
    }

    @Test
    @DisplayName("활성화 상태와 크롤링 주기를 동시에 변경할 수 있다")
    void updateBothActiveStatusAndCrawlInterval() {
        // given
        MustitSeller existingSeller = new MustitSeller(
                "SELLER001",
                "Test Seller",
                new CrawlInterval(CrawlIntervalType.DAILY, 1)
        );

        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "SELLER001",
                false,
                CrawlIntervalType.WEEKLY,
                2
        );

        when(loadMustitSellerPort.findBySellerId("SELLER001"))
                .thenReturn(Optional.of(existingSeller));
        when(saveMustitSellerPort.save(any(MustitSeller.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        MustitSeller result = useCase.execute(command);

        // then
        assertThat(result.isActive()).isFalse();
        assertThat(result.getCrawlInterval().getIntervalType()).isEqualTo(CrawlIntervalType.WEEKLY);
        assertThat(result.getCrawlInterval().getIntervalValue()).isEqualTo(2);

        // Port 호출 검증
        verify(loadMustitSellerPort, times(1)).findBySellerId("SELLER001");
        verify(saveMustitSellerPort, times(1)).save(any(MustitSeller.class));
    }

    @Test
    @DisplayName("존재하지 않는 sellerId로 수정 시도 시 예외가 발생한다")
    void throwExceptionWhenSellerNotFound() {
        // given
        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "NONEXISTENT",
                false,
                null,
                null
        );

        when(loadMustitSellerPort.findBySellerId("NONEXISTENT"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(SellerNotFoundException.class)
                .hasMessageContaining("NONEXISTENT");

        // 저장 Port는 호출되지 않아야 함
        verify(saveMustitSellerPort, never()).save(any(MustitSeller.class));
    }

    @Test
    @DisplayName("Command가 null이면 예외가 발생한다")
    void throwExceptionWhenCommandIsNull() {
        // when & then
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("command must not be null");
    }

    @Test
    @DisplayName("크롤링 주기만 변경 시 Domain Event가 발행된다")
    void publishDomainEventWhenCrawlIntervalChanged() {
        // given - Persistence에서 로드된 Seller 시뮬레이션 (id 있음)
        MustitSeller existingSeller = MustitSeller.reconstitute(
                com.ryuqq.crawlinghub.domain.mustit.seller.SellerBasicInfo.of(
                        1L,
                        "SELLER001",
                        "Test Seller",
                        true
                ),
                new CrawlInterval(CrawlIntervalType.DAILY, 1),
                com.ryuqq.crawlinghub.domain.mustit.seller.SellerTimeInfo.of(
                        java.time.LocalDateTime.now().minusDays(1),
                        java.time.LocalDateTime.now().minusDays(1)
                )
        );

        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "SELLER001",
                null,
                CrawlIntervalType.HOURLY,
                6
        );

        when(loadMustitSellerPort.findBySellerId("SELLER001"))
                .thenReturn(Optional.of(existingSeller));
        when(saveMustitSellerPort.save(any(MustitSeller.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        MustitSeller result = useCase.execute(command);

        // then
        assertThat(result.getDomainEvents()).hasSize(1);
    }

    @Test
    @DisplayName("활성화 상태만 변경 시 Domain Event가 발행되지 않는다")
    void notPublishDomainEventWhenOnlyActiveChanged() {
        // given
        MustitSeller existingSeller = new MustitSeller(
                "SELLER001",
                "Test Seller",
                new CrawlInterval(CrawlIntervalType.DAILY, 1)
        );

        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                "SELLER001",
                false,
                null,
                null
        );

        when(loadMustitSellerPort.findBySellerId("SELLER001"))
                .thenReturn(Optional.of(existingSeller));
        when(saveMustitSellerPort.save(any(MustitSeller.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        MustitSeller result = useCase.execute(command);

        // then
        assertThat(result.getDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("UseCase 생성 시 null Port가 주입되면 예외가 발생한다")
    void throwExceptionWhenPortIsNull() {
        // when & then - loadMustitSellerPort가 null
        assertThatThrownBy(() -> new UpdateMustitSellerService(null, saveMustitSellerPort))
                .isInstanceOf(NullPointerException.class);

        // when & then - saveMustitSellerPort가 null
        assertThatThrownBy(() -> new UpdateMustitSellerService(loadMustitSellerPort, null))
                .isInstanceOf(NullPointerException.class);
    }
}
