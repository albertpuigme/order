package net.apuig.order.pay;

import java.util.UUID;

import org.springframework.stereotype.Component;

import net.apuig.order.dto.PayOrderInVisaRequestDto;
import net.apuig.product.CurrencyType;

// TODO implement visa gateway
@Component
public class PaymentGatewayVisa implements PaymentGateway<PayOrderInVisaRequestDto>
{
    @Override
    public GenerateTokenResult generateToken(PayOrderInVisaRequestDto payment)
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
