package net.apuig.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterPassengerRequestDto(@NotBlank @Size(min = 3, max = 128) String name,
    @NotBlank @Size(min = 8, max = 36) String password)
{
}
