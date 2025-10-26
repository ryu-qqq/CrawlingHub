package com.ryuqq.crawlinghub.adapter.in.rest.seller;

import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.RegisterMustitSellerCommand;
import com.ryuqq.crawlinghub.application.mustit.seller.port.in.RegisterMustitSellerUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 머스트잇 셀러 REST Controller
 * <p>
 * RESTful API를 제공하는 Inbound Adapter입니다.
 * 비즈니스 로직은 포함하지 않으며, UseCase 호출과 응답 변환만 담당합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/sellers")
public class SellerController {

    private final RegisterMustitSellerUseCase registerMustitSellerUseCase;
    private final SellerApiMapper sellerApiMapper;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * @param registerMustitSellerUseCase 셀러 등록 UseCase
     * @param sellerApiMapper             API Mapper
     */
    public SellerController(
            RegisterMustitSellerUseCase registerMustitSellerUseCase,
            SellerApiMapper sellerApiMapper
    ) {
        this.registerMustitSellerUseCase = registerMustitSellerUseCase;
        this.sellerApiMapper = sellerApiMapper;
    }

    /**
     * 셀러 등록 API
     * <p>
     * POST /api/v1/sellers
     * </p>
     *
     * @param request 셀러 등록 Request (Bean Validation 적용)
     * @return 등록된 셀러 정보
     */
    @PostMapping
    public ResponseEntity<RegisterSellerApiResponse> registerSeller(
            @Valid @RequestBody RegisterSellerApiRequest request
    ) {
        // 1. API Request → Application Command 변환
        RegisterMustitSellerCommand command = sellerApiMapper.toCommand(request);

        // 2. UseCase 실행 → Domain Aggregate 반환
        com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller seller =
                registerMustitSellerUseCase.execute(command);

        // 3. Domain Aggregate → API Response 변환
        RegisterSellerApiResponse response = sellerApiMapper.toResponse(seller);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
