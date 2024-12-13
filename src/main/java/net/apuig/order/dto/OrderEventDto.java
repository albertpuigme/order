package net.apuig.order.dto;

import java.time.ZonedDateTime;

public record OrderEventDto(String action, String details, ZonedDateTime time)
{
}
