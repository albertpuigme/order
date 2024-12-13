package net.apuig.order.exception;

import org.springframework.http.HttpStatus;

import net.apuig.error.StoreWebException;

public class ProductInventoryException extends StoreWebException
{
    private static final long serialVersionUID = 1255616864274313365L;

    public ProductInventoryException()
    {
        super("Cannot satisfy required product amount", HttpStatus.CONFLICT);
    }
}
