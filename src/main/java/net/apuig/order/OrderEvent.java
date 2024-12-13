package net.apuig.order;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_events")
public class OrderEvent
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private ZonedDateTime time;

    @Column(nullable = false)
    private String action;

    @Column
    private String details;

    public OrderEvent(Order order, String action, String details)
    {
        this.order = order;
        this.action = action;
        this.details = details;
        this.time = ZonedDateTime.now(ZoneOffset.UTC);
    }

    public OrderEvent()
    {
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Order getOrder()
    {
        return order;
    }

    public void setOrder(Order order)
    {
        this.order = order;
    }

    public ZonedDateTime getTime()
    {
        return time;
    }

    public void setTime(ZonedDateTime time)
    {
        this.time = time;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getDetails()
    {
        return details;
    }

    public void setDetails(String details)
    {
        this.details = details;
    }
}
