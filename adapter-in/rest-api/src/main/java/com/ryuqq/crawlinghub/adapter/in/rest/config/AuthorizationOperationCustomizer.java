package com.ryuqq.crawlinghub.adapter.in.rest.config;

import io.swagger.v3.oas.models.Operation;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * Swagger Operation에 권한 정보를 자동 추가하는 커스터마이저.
 *
 * <p>{@code @PreAuthorize} 어노테이션을 파싱하여 각 엔드포인트의 description 앞에 권한 블록을 삽입합니다.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Component
public class AuthorizationOperationCustomizer implements OperationCustomizer {

    private static final Pattern SUPER_ADMIN_PATTERN =
            Pattern.compile("@access\\.superAdmin\\(\\)");
    private static final Pattern AUTHENTICATED_PATTERN =
            Pattern.compile("@access\\.authenticated\\(\\)");
    private static final Pattern HAS_PERMISSION_PATTERN =
            Pattern.compile("@access\\.hasPermission\\('([^']+)'\\)");
    private static final Pattern HAS_ANY_PERMISSION_PATTERN =
            Pattern.compile("@access\\.hasAnyPermission\\(([^)]+)\\)");

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        PreAuthorize preAuthorize = handlerMethod.getMethodAnnotation(PreAuthorize.class);

        String authBlock = resolveAuthBlock(preAuthorize);

        if (authBlock != null) {
            prependDescription(operation, authBlock);
        } else {
            markAsPublic(operation);
        }

        return operation;
    }

    private String resolveAuthBlock(PreAuthorize preAuthorize) {
        if (preAuthorize == null) {
            return null;
        }
        return resolveFromPreAuthorize(preAuthorize.value());
    }

    private String resolveFromPreAuthorize(String expression) {
        if (SUPER_ADMIN_PATTERN.matcher(expression).find()) {
            return formatBlock("SUPER_ADMIN 전용", null);
        }

        Matcher hasPermissionMatcher = HAS_PERMISSION_PATTERN.matcher(expression);
        if (hasPermissionMatcher.find()) {
            return formatBlock("인증 필요", hasPermissionMatcher.group(1));
        }

        Matcher hasAnyPermissionMatcher = HAS_ANY_PERMISSION_PATTERN.matcher(expression);
        if (hasAnyPermissionMatcher.find()) {
            String permissions = hasAnyPermissionMatcher.group(1).replaceAll("'", "").trim();
            return formatBlock("인증 필요 (다중 권한)", permissions);
        }

        if (AUTHENTICATED_PATTERN.matcher(expression).find()) {
            return formatBlock("인증 필요", null);
        }

        return formatBlock("인증 필요", null);
    }

    private String formatBlock(String accessLevel, String permission) {
        StringBuilder sb = new StringBuilder();
        sb.append("**권한**: ").append(accessLevel);
        if (permission != null) {
            sb.append(" | 필요 권한: `").append(permission).append("`");
        }
        sb.append("\n\n---\n\n");
        return sb.toString();
    }

    private void prependDescription(Operation operation, String authBlock) {
        String existing = operation.getDescription();
        if (existing != null && !existing.isBlank()) {
            operation.setDescription(authBlock + existing);
        } else {
            operation.setDescription(authBlock.trim());
        }
    }

    private void markAsPublic(Operation operation) {
        String publicBlock = "**권한**: Public (인증 불필요)\n\n---\n\n";
        String existing = operation.getDescription();
        if (existing != null && !existing.isBlank()) {
            operation.setDescription(publicBlock + existing);
        } else {
            operation.setDescription(publicBlock.trim());
        }
        operation.setSecurity(Collections.emptyList());
    }
}
