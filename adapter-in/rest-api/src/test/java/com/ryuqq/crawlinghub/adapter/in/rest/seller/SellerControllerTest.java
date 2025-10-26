package com.ryuqq.crawlinghub.adapter.in.rest.seller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.RegisterMustitSellerCommand;
import com.ryuqq.crawlinghub.application.mustit.seller.port.in.RegisterMustitSellerUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SellerController 단위 테스트
 * <p>
 * Mockito를 사용하여 Controller Layer만 격리 테스트합니다.
 * UseCase는 Mock으로 대체하여 Controller 로직만 검증합니다.
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerController 단위 테스트")
class SellerControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private RegisterMustitSellerUseCase registerMustitSellerUseCase;

    @Mock
    private SellerApiMapper sellerApiMapper;

    @InjectMocks
    private SellerController sellerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sellerController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/v1/sellers - 셀러 등록 성공")
    void registerSellerSuccess() throws Exception {
        // Given
        RegisterSellerApiRequest request = new RegisterSellerApiRequest(
                "SELLER001", "Test Seller", "DAILY", 1
        );
        String requestBody = objectMapper.writeValueAsString(request);

        RegisterMustitSellerCommand command = new RegisterMustitSellerCommand(
                "SELLER001", "Test Seller",
                com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType.DAILY, 1
        );

        com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller mockSeller = createMockSeller();
        RegisterSellerApiResponse apiResponse = createApiResponse();

        given(sellerApiMapper.toCommand(any(RegisterSellerApiRequest.class))).willReturn(command);
        given(registerMustitSellerUseCase.execute(any(RegisterMustitSellerCommand.class)))
                .willReturn(mockSeller);
        given(sellerApiMapper.toResponse(any(com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller.class)))
                .willReturn(apiResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sellerId").value("SELLER001"))
                .andExpect(jsonPath("$.name").value("Test Seller"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.intervalType").value("DAILY"))
                .andExpect(jsonPath("$.intervalValue").value(1))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    private com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller createMockSeller() {
        com.ryuqq.crawlinghub.domain.mustit.seller.CrawlInterval crawlInterval =
                new com.ryuqq.crawlinghub.domain.mustit.seller.CrawlInterval(
                        com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType.DAILY, 1
                );
        return new com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller(
                "SELLER001", "Test Seller", crawlInterval
        );
    }

    private RegisterSellerApiResponse createApiResponse() {
        return new RegisterSellerApiResponse(
                "SELLER001", "Test Seller", true, "DAILY", 1, java.time.LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /api/v1/sellers - Validation 실패 (sellerId가 null)")
    void registerSellerValidationFailSellerIdNull() throws Exception {
        // Given
        RegisterSellerApiRequest request = new RegisterSellerApiRequest(
                null,
                "Test Seller",
                "DAILY",
                1
        );

        String requestBody = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/sellers - Validation 실패 (name이 빈 문자열)")
    void registerSellerValidationFailNameBlank() throws Exception {
        // Given
        RegisterSellerApiRequest request = new RegisterSellerApiRequest(
                "SELLER001",
                "   ",
                "DAILY",
                1
        );

        String requestBody = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/sellers - Validation 실패 (intervalValue가 0)")
    void registerSellerValidationFailIntervalValueZero() throws Exception {
        // Given
        RegisterSellerApiRequest request = new RegisterSellerApiRequest(
                "SELLER001",
                "Test Seller",
                "DAILY",
                0
        );

        String requestBody = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/sellers - Validation 실패 (intervalValue가 음수)")
    void registerSellerValidationFailIntervalValueNegative() throws Exception {
        // Given
        RegisterSellerApiRequest request = new RegisterSellerApiRequest(
                "SELLER001",
                "Test Seller",
                "DAILY",
                -1
        );

        String requestBody = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
