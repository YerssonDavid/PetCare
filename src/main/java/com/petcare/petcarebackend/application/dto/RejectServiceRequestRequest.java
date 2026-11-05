package com.petcare.petcarebackend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record RejectServiceRequestRequest(
        @NotBlank
        @Size(max = 500)
        String reason
) {
}
