package net.apuig.user;

import org.springframework.http.HttpStatus;

import net.apuig.error.StoreWebException;

public class PassengerNotFoundException extends StoreWebException
{
    private static final long serialVersionUID = 2026519831574609274L;

    public PassengerNotFoundException()
    {
        super("Passenger not found", HttpStatus.NOT_FOUND);
    }
}
