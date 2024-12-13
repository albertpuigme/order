package net.apuig.order.pay;

import java.util.UUID;

import org.springframework.stereotype.Component;

import net.apuig.order.dto.PayOrderInStripeRequestDto;
import net.apuig.product.CurrencyType;

// TODO implement stripe gateway
@Component
public class PaymentGatewayStripe implements PaymentGateway<PayOrderInStripeRequestDto>
{
    @Override
    public GenerateTokenResult generateToken(PayOrderInStripeRequestDto payment)
    {
        return GenerateTokenResult.ok(UUID.randomUUID().toString());
    }

    @Override
    public PaymentResult pay(String token, Float amount, CurrencyType currency)
    {
        return PaymentResult.ok(UUID.randomUUID().toString());
    }

    @Override
    public PaymentStatus status(String transactionId)
    {
        return PaymentStatus.APPROVED;
    }
}
