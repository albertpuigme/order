package net.apuig.order.exception;

import org.springframework.http.HttpStatus;

import net.apuig.error.StoreWebException;
import net.apuig.order.OrderStatusType;

public class OrderInvalidActionException extends StoreWebException
{
    private static final long serialVersionUID = 8533128335914717910L;

    public OrderInvalidActionException(String action, OrderStatusType status)
    {
        super("Cannot perform %s when the order is %s".formatted(action, status),
            HttpStatus.CONFLICT);
    }
}
