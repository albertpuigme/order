package net.apuig.user;

import org.springframework.http.HttpStatus;

import net.apuig.error.StoreWebException;

public class PassengerAlreadyExistsException extends StoreWebException
{
    private static final long serialVersionUID = 2026519831574609274L;

    public PassengerAlreadyExistsException()
    {
        super("Name already taken, use another one", HttpStatus.CONFLICT);
    }
}
