package ru.sanmedexpress.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 40) String phone,
        @Size(max = 4000) String comment
) {
}
