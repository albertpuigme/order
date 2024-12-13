package net.apuig.product.dto;

import net.apuig.product.CurrencyType;

public record ProductDto(Long id, String name, Float price, CurrencyType currency,
    CategoryDto category, String imageUrl)
{
}
