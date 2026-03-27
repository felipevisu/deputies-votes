package com.deolhoneles.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AccountRequest(
        @NotBlank String name,
        @NotBlank String lastName,
        @NotBlank @Email String email
) {
}
