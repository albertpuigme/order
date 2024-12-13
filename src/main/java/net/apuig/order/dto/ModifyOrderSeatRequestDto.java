package net.apuig.order.dto;

import jakarta.validation.constraints.NotBlank;

public record ModifyOrderSeatRequestDto(@NotBlank String seatLocation)
{
}