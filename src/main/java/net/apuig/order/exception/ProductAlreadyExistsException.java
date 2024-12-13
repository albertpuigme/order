package net.apuig.order.exception;

import org.springframework.http.HttpStatus;

import net.apuig.error.StoreWebException;

public class ProductAlreadyExistsException extends StoreWebException
{
    private static final long serialVersionUID = 7122305604343889602L;

    public ProductAlreadyExistsException()
    {
        super("Product already exists in order, use modify amount", HttpStatus.CONFLICT);
    }
}
