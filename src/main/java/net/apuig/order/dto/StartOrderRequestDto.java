package net.apuig.order.dto;

import jakarta.validation.constraints.NotBlank;

public record StartOrderRequestDto(@NotBlank String seat)
{
}
