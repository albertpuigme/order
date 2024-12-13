package net.apuig.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddProductToOrderRequestDto(@NotNull Long productId, @Positive Integer amount)
{
}