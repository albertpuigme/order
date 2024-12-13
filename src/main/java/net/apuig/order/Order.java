package net.apuig.order;

import java.time.ZonedDateTime;

import org.hibernate.annotations.Formula;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import net.apuig.order.pay.PaymentType;
import net.apuig.user.User;

@Entity
@Table(name = "orders")
public class Order
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    int version;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String seat;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatusType status;

    @Column
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column
    private String paymentToken;

    @Column
    private String paymentTransaction;

    @Column
    private ZonedDateTime paymentAt;

    @Formula("""
    (SELECT SUM(pio.amount * p.price) 
    FROM products_in_orders pio 
    JOIN products p ON pio.product_id = p.id 
    WHERE pio.order_id = id)
    """)
    public Float totalPrice;

    public Order(User user, String seat)
    {
        this.user = user;
        this.seat = seat;
        this.status = OrderStatusType.OPEN;
    }

    public Order()
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

    public int getVersion()
    {
        return version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public String getSeat()
    {
        return seat;
    }

    public void setSeat(String seat)
    {
        this.seat = seat;
    }

    public OrderStatusType getStatus()
    {
        return status;
    }

    public void setStatus(OrderStatusType status)
    {
        this.status = status;
    }

    public ZonedDateTime getPaymentAt()
    {
        return paymentAt;
    }

    public void setPaymentAt(ZonedDateTime paymentAt)
    {
        this.paymentAt = paymentAt;
    }

    public PaymentType getPaymentType()
    {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType)
    {
        this.paymentType = paymentType;
    }

    public String getPaymentToken()
    {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken)
    {
        this.paymentToken = paymentToken;
    }

    public String getPaymentTransaction()
    {
        return paymentTransaction;
    }

    public void setPaymentTransaction(String paymentTransaction)
    {
        this.paymentTransaction = paymentTransaction;
    }

    public Float getTotalPrice()
    {
        return totalPrice;
    }
}
