package com.priyansu.workflow.security;

import com.priyansu.workflow.controller.AuthController;
import com.priyansu.workflow.controller.DashboardController;
import com.priyansu.workflow.dto.DashboardResponse;
import com.priyansu.workflow.dto.LoginResponse;
import com.priyansu.workflow.service.DashboardService;
import com.priyansu.workflow.service.TokenBlacklistService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {DashboardController.class, AuthController.class})
@AutoConfigureMockMvc(addFilters = true)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class SecurityConfigMvcTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean JwtService jwtService;
    @MockitoBean TokenBlacklistService tokenBlacklistService;
    @MockitoBean DashboardService dashboardService;
    @MockitoBean AuthService authService;

    @Test
    @DisplayName("Should reject an unauthenticated request to a protected endpoint")
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @DisplayName("Should allow an authenticated user to access a protected endpoint")
    void shouldAllowAuthenticatedRequest() throws Exception {
        when(dashboardService.getDashboard()).thenReturn(new DashboardResponse(0, 0, 0, 0, 0, 0, 0, 0, 0.0));

        mockMvc.perform(get("/api/v1/dashboard").with(user("user").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should permit the public authentication endpoint")
    void shouldPermitAuthEndpoint() throws Exception {
        when(authService.login(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new LoginResponse(UUID.randomUUID(), "access-token", "refresh-token"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"user@example.com\",\"password\":\"Password1!\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return not found for an unmapped public endpoint")
    void shouldMapUnmappedEndpointToNotFound() throws Exception {
        mockMvc.perform(get("/auth/not-a-real-endpoint"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }
}
