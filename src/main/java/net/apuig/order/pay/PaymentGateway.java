package net.apuig.order.pay;

import net.apuig.order.dto.PayOrderInGatewayRequestDto;
import net.apuig.product.CurrencyType;

// TODO refund
public interface PaymentGateway<T extends PayOrderInGatewayRequestDto>
{
    GenerateTokenResult generateToken(T payment);

    PaymentResult pay(String token, Float amount, CurrencyType currency);

    PaymentStatus status(String transactionId);
}
