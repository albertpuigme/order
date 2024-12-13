package net.apuig.order.dto;

import net.apuig.order.OrderStatusType;

public record OrderDto(Long orderId, Long passengerId, String seat, OrderStatusType status, Float price)
{
}
