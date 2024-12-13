package net.apuig.product;

public interface CategoryProjection
{
    Long getId();

    String getName();

    Long getParentId();

    String getParentName();
}
