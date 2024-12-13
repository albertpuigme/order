package net.apuig.order.exception;

import org.springframework.http.HttpStatus;

import net.apuig.error.StoreWebException;

public class OrderInvalidPaymentException extends StoreWebException
{
    private static final long serialVersionUID = -1069162499695752742L;

    public OrderInvalidPaymentException(String error)
    {
        super("Payment method is not valid: "+error, HttpStatus.BAD_REQUEST);
    }
}
