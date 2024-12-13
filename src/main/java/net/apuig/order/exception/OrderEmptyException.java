package net.apuig.order.exception;

import org.springframework.http.HttpStatus;

import net.apuig.error.StoreWebException;

public class OrderEmptyException extends StoreWebException
{
    private static final long serialVersionUID = -6493416793489858482L;

    public OrderEmptyException()
    {
        super("Add products to finish the order", HttpStatus.CONFLICT);
    }
}
