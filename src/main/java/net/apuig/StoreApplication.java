package net.apuig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // PaymentCheck
@SpringBootApplication
public class StoreApplication
{
    public static void main(final String[] args)
    {
        SpringApplication.run(StoreApplication.class, args);
    }
}
