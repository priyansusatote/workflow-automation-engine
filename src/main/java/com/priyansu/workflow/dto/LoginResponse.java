package com.priyansu.workflow.dto;

import java.util.UUID;

public record LoginResponse(
        UUID id,
        String accessToken,
        String refreshToken
) {
}
