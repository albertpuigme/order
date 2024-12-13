package net.apuig.error;

import org.springframework.http.HttpStatus;

public abstract class StoreWebException extends RuntimeException
{
    private static final long serialVersionUID = -5154932377641754992L;

    private final HttpStatus status;

    public StoreWebException(String msg, HttpStatus status)
    {
        super(msg);
        this.status = status;
    }

    public HttpStatus getStatus()
    {
        return status;
    }
}
