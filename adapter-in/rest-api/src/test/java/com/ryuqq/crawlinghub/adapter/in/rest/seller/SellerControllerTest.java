package com.ryuqq.crawlinghub.adapter.in.rest.seller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ryuqq.crawlinghub.adapter.in.rest.seller.controller.SellerController;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.RegisterSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.RegisterSellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.UpdateSellerApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.UpdateSellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper.SellerApiMapper;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.RegisterMustitSellerCommand;
import com.ryuqq.crawlinghub.application.mustit.seller.dto.command.UpdateMustitSellerCommand;
import com.ryuqq.crawlinghub.application.mustit.seller.port.in.RegisterMustitSellerUseCase;
import com.ryuqq.crawlinghub.application.mustit.seller.port.in.UpdateMustitSellerUseCase;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    private UpdateMustitSellerUseCase updateMustitSellerUseCase;

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
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellerId").value("SELLER001"))
                .andExpect(jsonPath("$.data.name").value("Test Seller"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.intervalType").value("DAILY"))
                .andExpect(jsonPath("$.data.intervalValue").value(1))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
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

    @Test
    @DisplayName("POST /api/v1/sellers - Validation 실패 (intervalType이 잘못된 값)")
    void registerSellerValidationFailInvalidIntervalType() throws Exception {
        // Given
        RegisterSellerApiRequest request = new RegisterSellerApiRequest(
                "SELLER001",
                "Test Seller",
                "INVALID_TYPE",
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
    @DisplayName("PUT /api/v1/sellers/{sellerId} - 셀러 수정 성공 (활성화 상태 변경)")
    void updateSellerSuccessActiveStatusOnly() throws Exception {
        // Given
        String sellerId = "SELLER001";
        UpdateSellerApiRequest request = new UpdateSellerApiRequest(false, null, null);
        String requestBody = objectMapper.writeValueAsString(request);

        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                sellerId, false, null, null
        );

        com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller mockSeller = createMockSeller();
        UpdateSellerApiResponse apiResponse = new UpdateSellerApiResponse(
                "SELLER001", "Test Seller", false, "DAILY", 1, java.time.LocalDateTime.now()
        );

        given(sellerApiMapper.toUpdateCommand(eq(sellerId), any(UpdateSellerApiRequest.class)))
                .willReturn(command);
        given(updateMustitSellerUseCase.execute(any(UpdateMustitSellerCommand.class)))
                .willReturn(mockSeller);
        given(sellerApiMapper.toUpdateResponse(any(com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller.class)))
                .willReturn(apiResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/sellers/" + sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellerId").value("SELLER001"))
                .andExpect(jsonPath("$.data.isActive").value(false))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/sellers/{sellerId} - 셀러 수정 성공 (크롤링 주기 변경)")
    void updateSellerSuccessCrawlIntervalOnly() throws Exception {
        // Given
        String sellerId = "SELLER001";
        UpdateSellerApiRequest request = new UpdateSellerApiRequest(null, "HOURLY", 6);
        String requestBody = objectMapper.writeValueAsString(request);

        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                sellerId, null,
                com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType.HOURLY, 6
        );

        com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller mockSeller = createMockSeller();
        UpdateSellerApiResponse apiResponse = new UpdateSellerApiResponse(
                "SELLER001", "Test Seller", true, "HOURLY", 6, java.time.LocalDateTime.now()
        );

        given(sellerApiMapper.toUpdateCommand(eq(sellerId), any(UpdateSellerApiRequest.class)))
                .willReturn(command);
        given(updateMustitSellerUseCase.execute(any(UpdateMustitSellerCommand.class)))
                .willReturn(mockSeller);
        given(sellerApiMapper.toUpdateResponse(any(com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller.class)))
                .willReturn(apiResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/sellers/" + sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellerId").value("SELLER001"))
                .andExpect(jsonPath("$.data.intervalType").value("HOURLY"))
                .andExpect(jsonPath("$.data.intervalValue").value(6))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/sellers/{sellerId} - 셀러 수정 성공 (모든 필드 변경)")
    void updateSellerSuccessAllFields() throws Exception {
        // Given
        String sellerId = "SELLER001";
        UpdateSellerApiRequest request = new UpdateSellerApiRequest(false, "WEEKLY", 2);
        String requestBody = objectMapper.writeValueAsString(request);

        UpdateMustitSellerCommand command = new UpdateMustitSellerCommand(
                sellerId, false,
                com.ryuqq.crawlinghub.domain.mustit.seller.CrawlIntervalType.WEEKLY, 2
        );

        com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller mockSeller = createMockSeller();
        UpdateSellerApiResponse apiResponse = new UpdateSellerApiResponse(
                "SELLER001", "Test Seller", false, "WEEKLY", 2, java.time.LocalDateTime.now()
        );

        given(sellerApiMapper.toUpdateCommand(eq(sellerId), any(UpdateSellerApiRequest.class)))
                .willReturn(command);
        given(updateMustitSellerUseCase.execute(any(UpdateMustitSellerCommand.class)))
                .willReturn(mockSeller);
        given(sellerApiMapper.toUpdateResponse(any(com.ryuqq.crawlinghub.domain.mustit.seller.MustitSeller.class)))
                .willReturn(apiResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/sellers/" + sellerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellerId").value("SELLER001"))
                .andExpect(jsonPath("$.data.isActive").value(false))
                .andExpect(jsonPath("$.data.intervalType").value("WEEKLY"))
                .andExpect(jsonPath("$.data.intervalValue").value(2))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

}
