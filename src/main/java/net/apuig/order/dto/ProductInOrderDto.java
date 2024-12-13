package net.apuig.order.dto;

public record ProductInOrderDto(Long productId, String productName, Integer amount,
    Float pricePerUnit)
{
}
