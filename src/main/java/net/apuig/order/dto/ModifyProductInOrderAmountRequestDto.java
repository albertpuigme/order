package net.apuig.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Use neAmount = 0 to delete the product from the order */
public record ModifyProductInOrderAmountRequestDto(@NotNull Long productId,
    @NotNull @Min(0) Integer newAmount)
{
}
