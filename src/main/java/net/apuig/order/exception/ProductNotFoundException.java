package net.apuig.order.exception;

import org.springframework.http.HttpStatus;

import net.apuig.error.StoreWebException;

public class ProductNotFoundException extends StoreWebException
{
    private static final long serialVersionUID = 5548937955540486089L;

    public ProductNotFoundException()
    {
        super("Product not found", HttpStatus.NOT_FOUND);
    }
}
