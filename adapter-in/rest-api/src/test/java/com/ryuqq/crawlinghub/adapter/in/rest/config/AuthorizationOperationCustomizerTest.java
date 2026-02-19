package com.ryuqq.crawlinghub.adapter.in.rest.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.method.HandlerMethod;

/**
 * AuthorizationOperationCustomizer 단위 테스트.
 *
 * <p>검증 범위:
 *
 * <ul>
 *   <li>@PreAuthorize 없는 Public 엔드포인트 처리
 *   <li>superAdmin 권한 블록 생성
 *   <li>hasPermission 단일 권한 블록 생성
 *   <li>hasAnyPermission 다중 권한 블록 생성
 *   <li>authenticated 인증 전용 블록 생성
 *   <li>기존 description 보존 및 앞에 블록 삽입
 * </ul>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("config")
@DisplayName("AuthorizationOperationCustomizer 단위 테스트")
class AuthorizationOperationCustomizerTest {

    private AuthorizationOperationCustomizer customizer;

    @BeforeEach
    void setUp() {
        customizer = new AuthorizationOperationCustomizer();
    }

    @Nested
    @DisplayName("PreAuthorize 없는 엔드포인트")
    class WithoutPreAuthorize {

        @Test
        @DisplayName("@PreAuthorize 없으면 Public으로 표시된다")
        void customize_WithoutPreAuthorize_MarksAsPublic() {
            // Given
            HandlerMethod handlerMethod = Mockito.mock(HandlerMethod.class);
            given(handlerMethod.getMethodAnnotation(PreAuthorize.class)).willReturn(null);
            Operation operation = new Operation();

            // When
            customizer.customize(operation, handlerMethod);

            // Then
            assertThat(operation.getDescription()).contains("Public (인증 불필요)");
            assertThat(operation.getSecurity()).isNotNull();
            assertThat(operation.getSecurity()).isEmpty();
        }

        @Test
        @DisplayName("기존 description이 있으면 앞에 Public 블록이 추가된다")
        void customize_WithoutPreAuthorize_PrependPublicBlockToExistingDescription() {
            // Given
            HandlerMethod handlerMethod = Mockito.mock(HandlerMethod.class);
            given(handlerMethod.getMethodAnnotation(PreAuthorize.class)).willReturn(null);
            Operation operation = new Operation();
            operation.setDescription("기존 설명");

            // When
            customizer.customize(operation, handlerMethod);

            // Then
            String description = operation.getDescription();
            assertThat(description).startsWith("**권한**: Public (인증 불필요)");
            assertThat(description).endsWith("기존 설명");
            assertThat(operation.getSecurity()).isEmpty();
        }
    }

    @Nested
    @DisplayName("superAdmin 권한")
    class SuperAdminAuthorization {

        @Test
        @DisplayName("@access.superAdmin()이면 SUPER_ADMIN 전용 블록이 설정된다")
        void customize_WithSuperAdminExpression_SetsSuperAdminBlock() {
            // Given
            HandlerMethod handlerMethod = Mockito.mock(HandlerMethod.class);
            PreAuthorize preAuthorize = Mockito.mock(PreAuthorize.class);
            given(preAuthorize.value()).willReturn("@access.superAdmin()");
            given(handlerMethod.getMethodAnnotation(PreAuthorize.class)).willReturn(preAuthorize);
            Operation operation = new Operation();

            // When
            customizer.customize(operation, handlerMethod);

            // Then
            assertThat(operation.getDescription()).contains("SUPER_ADMIN 전용");
            assertThat(operation.getSecurity()).isNull();
        }
    }

    @Nested
    @DisplayName("hasPermission 권한")
    class HasPermissionAuthorization {

        @Test
        @DisplayName("@access.hasPermission('SELLER_READ')이면 해당 권한 블록이 설정된다")
        void customize_WithHasPermissionExpression_SetsPermissionBlock() {
            // Given
            HandlerMethod handlerMethod = Mockito.mock(HandlerMethod.class);
            PreAuthorize preAuthorize = Mockito.mock(PreAuthorize.class);
            given(preAuthorize.value()).willReturn("@access.hasPermission('SELLER_READ')");
            given(handlerMethod.getMethodAnnotation(PreAuthorize.class)).willReturn(preAuthorize);
            Operation operation = new Operation();

            // When
            customizer.customize(operation, handlerMethod);

            // Then
            String description = operation.getDescription();
            assertThat(description).contains("인증 필요");
            assertThat(description).contains("SELLER_READ");
        }
    }

    @Nested
    @DisplayName("hasAnyPermission 권한")
    class HasAnyPermissionAuthorization {

        @Test
        @DisplayName("@access.hasAnyPermission('SELLER_READ', 'SELLER_WRITE')이면 다중 권한 블록이 설정된다")
        void customize_WithHasAnyPermissionExpression_SetsAnyPermissionBlock() {
            // Given
            HandlerMethod handlerMethod = Mockito.mock(HandlerMethod.class);
            PreAuthorize preAuthorize = Mockito.mock(PreAuthorize.class);
            given(preAuthorize.value())
                    .willReturn("@access.hasAnyPermission('SELLER_READ', 'SELLER_WRITE')");
            given(handlerMethod.getMethodAnnotation(PreAuthorize.class)).willReturn(preAuthorize);
            Operation operation = new Operation();

            // When
            customizer.customize(operation, handlerMethod);

            // Then
            String description = operation.getDescription();
            assertThat(description).contains("다중 권한");
            assertThat(description).contains("SELLER_READ");
            assertThat(description).contains("SELLER_WRITE");
        }
    }

    @Nested
    @DisplayName("authenticated 권한")
    class AuthenticatedAuthorization {

        @Test
        @DisplayName("@access.authenticated()이면 인증 필요 블록이 설정되고 권한 상세는 없다")
        void customize_WithAuthenticatedExpression_SetsAuthenticatedBlock() {
            // Given
            HandlerMethod handlerMethod = Mockito.mock(HandlerMethod.class);
            PreAuthorize preAuthorize = Mockito.mock(PreAuthorize.class);
            given(preAuthorize.value()).willReturn("@access.authenticated()");
            given(handlerMethod.getMethodAnnotation(PreAuthorize.class)).willReturn(preAuthorize);
            Operation operation = new Operation();

            // When
            customizer.customize(operation, handlerMethod);

            // Then
            String description = operation.getDescription();
            assertThat(description).contains("인증 필요");
            assertThat(description).doesNotContain("필요 권한:");
            assertThat(operation.getSecurity()).isNull();
        }
    }

    @Nested
    @DisplayName("기존 description 유지")
    class ExistingDescriptionPreservation {

        @Test
        @DisplayName("기존 description이 있으면 권한 블록이 앞에 삽입되고 기존 내용이 보존된다")
        void customize_WithExistingDescription_PrependsAuthBlockAndPreservesExisting() {
            // Given
            HandlerMethod handlerMethod = Mockito.mock(HandlerMethod.class);
            PreAuthorize preAuthorize = Mockito.mock(PreAuthorize.class);
            given(preAuthorize.value()).willReturn("@access.hasPermission('SELLER_READ')");
            given(handlerMethod.getMethodAnnotation(PreAuthorize.class)).willReturn(preAuthorize);
            Operation operation = new Operation();
            operation.setDescription("셀러 목록을 조회합니다.");

            // When
            customizer.customize(operation, handlerMethod);

            // Then
            String description = operation.getDescription();
            assertThat(description).startsWith("**권한**:");
            assertThat(description).contains("인증 필요");
            assertThat(description).contains("SELLER_READ");
            assertThat(description).endsWith("셀러 목록을 조회합니다.");
        }

        @Test
        @DisplayName("description이 없으면 권한 블록만 설정되고 구분선은 trim된다")
        void customize_WithoutExistingDescription_SetsOnlyAuthBlock() {
            // Given
            HandlerMethod handlerMethod = Mockito.mock(HandlerMethod.class);
            PreAuthorize preAuthorize = Mockito.mock(PreAuthorize.class);
            given(preAuthorize.value()).willReturn("@access.authenticated()");
            given(handlerMethod.getMethodAnnotation(PreAuthorize.class)).willReturn(preAuthorize);
            Operation operation = new Operation();

            // When
            customizer.customize(operation, handlerMethod);

            // Then
            String description = operation.getDescription();
            assertThat(description).isNotBlank();
            assertThat(description).contains("인증 필요");
        }
    }
}
