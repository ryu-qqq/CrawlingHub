package com.ryuqq.crawlinghub.adapter.in.rest.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * REST Docs HTML 문서 제공 컨트롤러
 *
 * <p>빌드된 REST Docs HTML 문서를 제공합니다.
 *
 * <p>/docs 경로로 접근 시 REST API 문서 페이지로 리다이렉트됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Controller
public class DocsController {

    /**
     * REST Docs HTML 문서로 리다이렉트
     *
     * @return index.html로 리다이렉트
     */
    @GetMapping("/docs")
    public String docs() {
        return "redirect:/docs/index.html";
    }
}
