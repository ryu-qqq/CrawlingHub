package com.ryuqq.crawlinghub.application.seller.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.facade.SellerCommandFacade;
import com.ryuqq.crawlinghub.application.seller.factory.command.SellerCommandFactory;
import com.ryuqq.crawlinghub.application.seller.manager.query.SellerReadManager;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateMustItSellerIdException;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerNameException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UpdateSellerService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Manager/Factory 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSellerService 테스트")
class UpdateSellerServiceTest {

    @Mock private SellerCommandFacade sellerCommandFacade;

    @Mock private SellerReadManager sellerReadManager;

    @Mock private SellerCommandFactory commandFactory;

    @Mock private SellerAssembler assembler;

    @InjectMocks private UpdateSellerService service;

    @Nested
    @DisplayName("execute() 셀러 수정 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 셀러 정보 수정 시 SellerResponse 반환")
        void shouldUpdateSellerAndReturnResponse() {
            // Given
            Long sellerId = 1L;
            UpdateSellerCommand command =
                    new UpdateSellerCommand(sellerId, "new-mustit", "new-name", true);
            Seller existingSeller = SellerFixture.anActiveSeller();
            Seller requestedSeller = SellerFixture.anActiveSeller();
            Instant now = Instant.now();
            SellerResponse expectedResponse =
                    new SellerResponse(sellerId, "new-mustit", "new-name", true, now, now);

            given(commandFactory.createForComparison(command)).willReturn(requestedSeller);
            given(sellerReadManager.findById(any(SellerId.class)))
                    .willReturn(Optional.of(existingSeller));
            given(
                            sellerCommandFacade.update(
                                    any(Seller.class),
                                    any(MustItSellerName.class),
                                    any(SellerName.class),
                                    any(SellerStatus.class)))
                    .willReturn(existingSeller);
            given(assembler.toResponse(any(Seller.class))).willReturn(expectedResponse);

            // When
            SellerResponse result = service.execute(command);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            then(commandFactory).should().createForComparison(command);
            then(sellerReadManager).should().findById(SellerId.of(sellerId));
            then(sellerCommandFacade)
                    .should()
                    .update(
                            any(Seller.class),
                            any(MustItSellerName.class),
                            any(SellerName.class),
                            any(SellerStatus.class));
            then(assembler).should().toResponse(any(Seller.class));
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 셀러 수정 시 SellerNotFoundException 발생")
        void shouldThrowExceptionWhenSellerNotFound() {
            // Given
            Long sellerId = 999L;
            UpdateSellerCommand command =
                    new UpdateSellerCommand(sellerId, "new-mustit", "new-name", true);
            Seller requestedSeller = SellerFixture.anActiveSeller();

            given(commandFactory.createForComparison(command)).willReturn(requestedSeller);
            given(sellerReadManager.findById(any(SellerId.class))).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(SellerNotFoundException.class);

            then(sellerCommandFacade).should(never()).update(any(), any(), any(), any());
        }

        @Test
        @DisplayName("[실패] MustItSellerName 중복 시 DuplicateMustItSellerIdException 발생")
        void shouldThrowExceptionWhenMustItSellerNameDuplicated() {
            // Given
            Long sellerId = 1L;
            UpdateSellerCommand command =
                    new UpdateSellerCommand(sellerId, "duplicate-mustit", null, null);
            Seller existingSeller = SellerFixture.anActiveSeller();
            Seller requestedSeller = SellerFixture.aNewActiveSeller("duplicate-mustit", "seller");

            given(commandFactory.createForComparison(command)).willReturn(requestedSeller);
            given(sellerReadManager.findById(any(SellerId.class)))
                    .willReturn(Optional.of(existingSeller));
            given(
                            sellerReadManager.existsByMustItSellerNameExcludingId(
                                    any(MustItSellerName.class), any(SellerId.class)))
                    .willReturn(true);

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(DuplicateMustItSellerIdException.class);

            then(sellerCommandFacade).should(never()).update(any(), any(), any(), any());
        }

        @Test
        @DisplayName("[실패] SellerName 중복 시 DuplicateSellerNameException 발생")
        void shouldThrowExceptionWhenSellerNameDuplicated() {
            // Given
            Long sellerId = 1L;
            UpdateSellerCommand command =
                    new UpdateSellerCommand(sellerId, null, "duplicate-name", null);
            Seller existingSeller = SellerFixture.anActiveSeller();
            Seller requestedSeller = SellerFixture.aNewActiveSeller("mustit", "duplicate-name");

            given(commandFactory.createForComparison(command)).willReturn(requestedSeller);
            given(sellerReadManager.findById(any(SellerId.class)))
                    .willReturn(Optional.of(existingSeller));
            given(
                            sellerReadManager.existsBySellerNameExcludingId(
                                    any(SellerName.class), any(SellerId.class)))
                    .willReturn(true);

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(DuplicateSellerNameException.class);

            then(sellerCommandFacade).should(never()).update(any(), any(), any(), any());
        }
    }
}
