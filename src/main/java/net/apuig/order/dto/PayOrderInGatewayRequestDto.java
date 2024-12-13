package net.apuig.order.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import net.apuig.order.pay.PaymentType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({//
@JsonSubTypes.Type(value = PayOrderInVisaRequestDto.class, name = "VISA"),//
@JsonSubTypes.Type(value = PayOrderInStripeRequestDto.class, name = "STRIPE"),//
})
public sealed interface PayOrderInGatewayRequestDto
    permits PayOrderInVisaRequestDto, PayOrderInStripeRequestDto
{
    PaymentType type();
}
