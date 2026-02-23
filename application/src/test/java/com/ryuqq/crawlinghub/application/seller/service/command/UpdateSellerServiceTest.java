package com.ryuqq.crawlinghub.application.seller.service.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.common.dto.command.UpdateContext;
import com.ryuqq.crawlinghub.application.seller.component.SellerPersistenceValidator;
import com.ryuqq.crawlinghub.application.seller.dto.command.UpdateSellerCommand;
import com.ryuqq.crawlinghub.application.seller.factory.command.SellerCommandFactory;
import com.ryuqq.crawlinghub.application.seller.manager.SellerCommandManager;
import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateMustItSellerIdException;
import com.ryuqq.crawlinghub.domain.seller.exception.DuplicateSellerNameException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerHasActiveSchedulersException;
import com.ryuqq.crawlinghub.domain.seller.exception.SellerNotFoundException;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerUpdateData;
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
 * <p>Mockist 스타일 테스트: Validator/ReadManager/Factory/CommandManager 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSellerService 테스트")
class UpdateSellerServiceTest {

    @Mock private SellerPersistenceValidator validator;

    @Mock private SellerReadManager readManager;

    @Mock private SellerCommandFactory commandFactory;

    @Mock private SellerCommandManager commandManager;

    @InjectMocks private UpdateSellerService service;

    @Nested
    @DisplayName("execute() 셀러 수정 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 셀러 정보 수정 시 정상 실행")
        void shouldUpdateSellerSuccessfully() {
            // Given
            Long sellerId = 1L;
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            UpdateSellerCommand command =
                    new UpdateSellerCommand(sellerId, "new-mustit", "new-name", true);
            SellerId id = SellerId.of(sellerId);
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            MustItSellerName.of("new-mustit"),
                            SellerName.of("new-name"),
                            SellerStatus.ACTIVE);
            UpdateContext<SellerId, SellerUpdateData> context =
                    new UpdateContext<>(id, updateData, fixedInstant);
            Seller existingSeller = SellerFixture.anActiveSeller();

            given(commandFactory.createUpdateContext(command)).willReturn(context);
            given(readManager.findById(id)).willReturn(Optional.of(existingSeller));

            // When
            service.execute(command);

            // Then
            then(commandFactory).should().createUpdateContext(command);
            then(readManager).should().findById(id);
            then(validator).should().validateForUpdate(existingSeller, updateData);
            then(commandManager).should().persist(existingSeller);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 셀러 수정 시 SellerNotFoundException 발생")
        void shouldThrowExceptionWhenSellerNotFound() {
            // Given
            Long sellerId = 999L;
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            UpdateSellerCommand command =
                    new UpdateSellerCommand(sellerId, "new-mustit", "new-name", true);
            SellerId id = SellerId.of(sellerId);
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            MustItSellerName.of("new-mustit"),
                            SellerName.of("new-name"),
                            SellerStatus.ACTIVE);
            UpdateContext<SellerId, SellerUpdateData> context =
                    new UpdateContext<>(id, updateData, fixedInstant);

            given(commandFactory.createUpdateContext(command)).willReturn(context);
            given(readManager.findById(id)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(SellerNotFoundException.class);

            then(commandManager).should(never()).persist(any());
        }

        @Test
        @DisplayName("[실패] MustItSellerName 중복 시 DuplicateMustItSellerIdException 발생")
        void shouldThrowExceptionWhenMustItSellerNameDuplicated() {
            // Given
            Long sellerId = 1L;
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            UpdateSellerCommand command =
                    new UpdateSellerCommand(sellerId, "duplicate-mustit", "seller-name", true);
            SellerId id = SellerId.of(sellerId);
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            MustItSellerName.of("duplicate-mustit"),
                            SellerName.of("seller-name"),
                            SellerStatus.ACTIVE);
            UpdateContext<SellerId, SellerUpdateData> context =
                    new UpdateContext<>(id, updateData, fixedInstant);
            Seller existingSeller = SellerFixture.anActiveSeller();

            given(commandFactory.createUpdateContext(command)).willReturn(context);
            given(readManager.findById(id)).willReturn(Optional.of(existingSeller));
            doThrow(new DuplicateMustItSellerIdException("duplicate-mustit"))
                    .when(validator)
                    .validateForUpdate(existingSeller, updateData);

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(DuplicateMustItSellerIdException.class);

            then(commandManager).should(never()).persist(any());
        }

        @Test
        @DisplayName("[실패] SellerName 중복 시 DuplicateSellerNameException 발생")
        void shouldThrowExceptionWhenSellerNameDuplicated() {
            // Given
            Long sellerId = 1L;
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            UpdateSellerCommand command =
                    new UpdateSellerCommand(sellerId, "mustit-name", "duplicate-name", true);
            SellerId id = SellerId.of(sellerId);
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            MustItSellerName.of("mustit-name"),
                            SellerName.of("duplicate-name"),
                            SellerStatus.ACTIVE);
            UpdateContext<SellerId, SellerUpdateData> context =
                    new UpdateContext<>(id, updateData, fixedInstant);
            Seller existingSeller = SellerFixture.anActiveSeller();

            given(commandFactory.createUpdateContext(command)).willReturn(context);
            given(readManager.findById(id)).willReturn(Optional.of(existingSeller));
            doThrow(new DuplicateSellerNameException("duplicate-name"))
                    .when(validator)
                    .validateForUpdate(existingSeller, updateData);

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(DuplicateSellerNameException.class);

            then(commandManager).should(never()).persist(any());
        }

        @Test
        @DisplayName("[실패] 활성 스케줄러 존재 시 비활성화하면 SellerHasActiveSchedulersException 발생")
        void shouldThrowExceptionWhenDeactivatingSellerWithActiveSchedulers() {
            // Given
            Long sellerId = 1L;
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            UpdateSellerCommand command =
                    new UpdateSellerCommand(sellerId, "mustit-name", "seller-name", false);
            SellerId id = SellerId.of(sellerId);
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            MustItSellerName.of("mustit-name"),
                            SellerName.of("seller-name"),
                            SellerStatus.INACTIVE);
            UpdateContext<SellerId, SellerUpdateData> context =
                    new UpdateContext<>(id, updateData, fixedInstant);
            Seller existingSeller = SellerFixture.anActiveSeller();

            given(commandFactory.createUpdateContext(command)).willReturn(context);
            given(readManager.findById(id)).willReturn(Optional.of(existingSeller));
            doThrow(new SellerHasActiveSchedulersException(sellerId, 3))
                    .when(validator)
                    .validateForUpdate(existingSeller, updateData);

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(SellerHasActiveSchedulersException.class);

            then(commandManager).should(never()).persist(any());
        }

        @Test
        @DisplayName("[성공] 활성 스케줄러 없으면 비활성화 성공")
        void shouldDeactivateSellerWhenNoActiveSchedulers() {
            // Given
            Long sellerId = 1L;
            Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
            UpdateSellerCommand command =
                    new UpdateSellerCommand(sellerId, "mustit-name", "seller-name", false);
            SellerId id = SellerId.of(sellerId);
            SellerUpdateData updateData =
                    SellerUpdateData.of(
                            MustItSellerName.of("mustit-name"),
                            SellerName.of("seller-name"),
                            SellerStatus.INACTIVE);
            UpdateContext<SellerId, SellerUpdateData> context =
                    new UpdateContext<>(id, updateData, fixedInstant);
            Seller existingSeller = SellerFixture.anActiveSeller();

            given(commandFactory.createUpdateContext(command)).willReturn(context);
            given(readManager.findById(id)).willReturn(Optional.of(existingSeller));

            // When
            service.execute(command);

            // Then
            then(validator).should().validateForUpdate(existingSeller, updateData);
            then(commandManager).should().persist(existingSeller);
        }
    }
}
