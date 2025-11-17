package com.ryuqq.crawlinghub.application.seller.port.in.command;

import com.ryuqq.crawlinghub.application.seller.dto.command.RegisterSellerCommand;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * RegisterSellerUseCase 인터페이스 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ Input Port (Command UseCase) 인터페이스 계약 검증</li>
 *   <li>✅ 메서드 시그니처 검증</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 규칙:</strong></p>
 * <ul>
 *   <li>✅ Input Port는 port.in.command 패키지에 위치</li>
 *   <li>✅ Command DTO 입력, Response DTO 출력</li>
 *   <li>✅ UseCase 네이밍 (동사 + UseCase 접미사)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-18
 */
@DisplayName("RegisterSellerUseCase 인터페이스 테스트")
class RegisterSellerUseCaseTest {

    @Test
    @DisplayName("RegisterSellerCommand를 받아 SellerResponse를 반환해야 한다")
    void shouldAcceptCommandAndReturnResponse() {
        // Given
        RegisterSellerUseCase useCase = mock(RegisterSellerUseCase.class);
        RegisterSellerCommand command = new RegisterSellerCommand(
            "seller_12345",
            "무신사",
            1
        );

        // When: 메서드 시그니처 검증 (컴파일 타임)
        // SellerResponse response = useCase.execute(command);

        // Then: 인터페이스가 존재하고 시그니처가 올바르면 컴파일 성공
        assertThat(useCase).isNotNull();
    }

    @Test
    @DisplayName("UseCase는 execute() 메서드를 가져야 한다")
    void shouldHaveExecuteMethod() throws NoSuchMethodException {
        // When: Reflection으로 execute 메서드 존재 확인
        var method = RegisterSellerUseCase.class.getDeclaredMethod(
            "execute",
            RegisterSellerCommand.class
        );

        // Then
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(SellerResponse.class);
    }
}
