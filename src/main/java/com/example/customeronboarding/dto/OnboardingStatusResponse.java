package com.example.customeronboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OnboardingStatusResponse {
    private Long jobId;
    private String status;
    private String exitStatus;
    private String details;
}
