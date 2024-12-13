package net.apuig.order.dto;

import java.time.Month;
import java.time.Year;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import net.apuig.order.pay.PaymentType;

public record PayOrderInVisaRequestDto(PaymentType type,
    @NotBlank @Pattern(regexp = "^4[0-9]{12}(?:[0-9]{3})?$", message = "Invalid card number format") String creditCard,
    Month expiryMonth, @Future Year expiryYear,
    @Pattern(regexp = "^[0-9]{4}$", message = "CVV must be 4 digits") String cvv)
    implements PayOrderInGatewayRequestDto
{
    // NOTE type in constructor for serialization inheritance
    @Override
    public PaymentType type()
    {
        return PaymentType.VISA;
    }
}
