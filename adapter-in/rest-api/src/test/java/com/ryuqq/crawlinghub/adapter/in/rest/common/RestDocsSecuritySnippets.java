package com.ryuqq.crawlinghub.adapter.in.rest.common;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;

import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.snippet.Snippet;

/**
 * RestDocs Security Snippets
 *
 * <p>REST Docs 문서에 보안/권한 정보를 추가하기 위한 헬퍼 클래스
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>{@code
 * .andDo(document("api-name",
 *     RestDocsSecuritySnippets.authorization("seller:create"),
 *     requestFields(...),
 *     responseFields(...)
 * ));
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class RestDocsSecuritySnippets {

    private RestDocsSecuritySnippets() {
        // Utility class - prevent instantiation
    }

    /**
     * Authorization 헤더 문서화 스니펫 생성
     *
     * @param requiredPermission 필요한 권한 (예: "seller:create")
     * @return RequestHeadersSnippet
     */
    public static RequestHeadersSnippet authorization(String requiredPermission) {
        return requestHeaders(
                headerWithName("Authorization")
                        .description(
                                String.format(
                                        "Bearer JWT 토큰 (필수)%n"
                                                + "필요 권한: `%s`%n"
                                                + "ROLE_SUPER_ADMIN은 모든 권한 보유",
                                        requiredPermission))
                        .optional());
    }

    /**
     * Authorization 헤더 문서화 스니펫 생성 (다중 권한)
     *
     * @param requiredPermissions 필요한 권한들 (예: "seller:read", "seller:update")
     * @return RequestHeadersSnippet
     */
    public static RequestHeadersSnippet authorizationAny(String... requiredPermissions) {
        String permissions = String.join("` 또는 `", requiredPermissions);
        return requestHeaders(
                headerWithName("Authorization")
                        .description(
                                String.format(
                                        "Bearer JWT 토큰 (필수)%n"
                                                + "필요 권한: `%s` 중 하나%n"
                                                + "ROLE_SUPER_ADMIN은 모든 권한 보유",
                                        permissions))
                        .optional());
    }

    /**
     * Authorization 헤더 문서화 스니펫 생성 (모든 권한 필요)
     *
     * @param requiredPermissions 필요한 모든 권한들
     * @return RequestHeadersSnippet
     */
    public static RequestHeadersSnippet authorizationAll(String... requiredPermissions) {
        String permissions = String.join("`, `", requiredPermissions);
        return requestHeaders(
                headerWithName("Authorization")
                        .description(
                                String.format(
                                        "Bearer JWT 토큰 (필수)%n"
                                                + "필요 권한: `%s` 모두 필요%n"
                                                + "ROLE_SUPER_ADMIN은 모든 권한 보유",
                                        permissions))
                        .optional());
    }

    /**
     * 슈퍼 관리자 전용 API 문서화 스니펫
     *
     * @return RequestHeadersSnippet
     */
    public static RequestHeadersSnippet superAdminOnly() {
        return requestHeaders(
                headerWithName("Authorization")
                        .description("Bearer JWT 토큰 (필수)\n" + "필요 권한: ROLE_SUPER_ADMIN (슈퍼 관리자 전용)")
                        .optional());
    }

    /**
     * 인증만 필요한 API 문서화 스니펫
     *
     * @return RequestHeadersSnippet
     */
    public static RequestHeadersSnippet authenticated() {
        return requestHeaders(
                headerWithName("Authorization")
                        .description("Bearer JWT 토큰 (필수)\n" + "인증된 사용자만 접근 가능")
                        .optional());
    }

    /**
     * 권한 정보를 포함한 커스텀 스니펫 배열 생성
     *
     * @param requiredPermission 필요한 권한
     * @param additionalSnippets 추가 스니펫들
     * @return Snippet 배열
     */
    public static Snippet[] withAuthorization(
            String requiredPermission, Snippet... additionalSnippets) {
        Snippet[] result = new Snippet[additionalSnippets.length + 1];
        result[0] = authorization(requiredPermission);
        System.arraycopy(additionalSnippets, 0, result, 1, additionalSnippets.length);
        return result;
    }
}
