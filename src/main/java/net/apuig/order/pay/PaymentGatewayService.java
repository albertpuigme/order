package net.apuig.order.pay;

import java.util.EnumMap;

import org.springframework.stereotype.Service;

import net.apuig.order.dto.PayOrderInGatewayRequestDto;
import net.apuig.product.CurrencyType;

// TODO implement circuit breaker
// TODO control rate-limit
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class PaymentGatewayService
{
    private final EnumMap<PaymentType, PaymentGateway> gateways = new EnumMap<>(PaymentType.class);

    public PaymentGatewayService(PaymentGatewayStripe paymentGatewayStripe,
        PaymentGatewayVisa paymentGatewayVisa)
    {
        gateways.put(PaymentType.STRIPE, paymentGatewayStripe);
        gateways.put(PaymentType.VISA, paymentGatewayVisa);
    }

    public GenerateTokenResult generateToken(PayOrderInGatewayRequestDto request)
    {
        return gateways.get(request.type()).generateToken(request);
    }

    public PaymentResult pay(PaymentType type, String token, Float amount, CurrencyType currency)
    {
        return gateways.get(type).pay(token, amount, currency);
    }

    public PaymentStatus status(PaymentType type, String transactionId)
    {
        return gateways.get(type).status(transactionId);
    }
}
