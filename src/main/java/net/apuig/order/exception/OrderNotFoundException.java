package net.apuig.order.exception;

import org.springframework.http.HttpStatus;

import net.apuig.error.StoreWebException;

public class OrderNotFoundException extends StoreWebException
{
    private static final long serialVersionUID = -556619893533111432L;

    public OrderNotFoundException()
    {
        super("Order not found", HttpStatus.NOT_FOUND);
    }
}
