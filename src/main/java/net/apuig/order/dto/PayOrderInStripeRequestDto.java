package net.apuig.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import net.apuig.order.pay.PaymentType;

public record PayOrderInStripeRequestDto(PaymentType type,
    @NotBlank @Pattern(regexp = "^(tok|card)_[a-zA-Z0-9]{24}$") String token)
    implements PayOrderInGatewayRequestDto
{
    // NOTE type in constructor for serialization inheritance
    @Override
    public PaymentType type()
    {
        return PaymentType.STRIPE;
    }
}
