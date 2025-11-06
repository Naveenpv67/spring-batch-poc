package com.example.customeronboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OnboardingResponse {
    private Long jobId;
    private String message;
}
