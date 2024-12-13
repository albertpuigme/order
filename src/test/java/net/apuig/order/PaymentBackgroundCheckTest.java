package net.apuig.order;

import static net.apuig.user.UserType.PASSENGER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import net.apuig.StoreApplication;
import net.apuig.order.pay.PaymentBackgroundCheck;
import net.apuig.order.pay.PaymentGatewayStripe;
import net.apuig.order.pay.PaymentStatus;
import net.apuig.order.pay.PaymentType;
import net.apuig.user.User;
import net.apuig.user.UserRepository;

@SpringBootTest(classes = StoreApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestInstance(Lifecycle.PER_CLASS)
public class PaymentBackgroundCheckTest
{
    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private PaymentGatewayStripe stripe;

    @Autowired
    @InjectMocks
    private PaymentBackgroundCheck paymentBackgroundCheck;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderEventRepository orderEventRepository;

    private Order order;

    @AfterEach
    public void cleanup()
    {
        orderEventRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    public void periodicCheckPayments()
    {
        User user = userRepository.save(new User("apuig", "encoded-password", PASSENGER));
        order = new Order(user, "A1");
        order.setStatus(OrderStatusType.PAYMENT_IN_PROGRESS);
        order.setPaymentType(PaymentType.STRIPE);
        order.setPaymentToken("testToken");
        order.setPaymentTransaction("testTransaction");
        order = orderRepository.save(order);

        when(stripe.status(eq("testTransaction"))).thenReturn(PaymentStatus.APPROVED);

        // NOTE need new transaction in test because OrderService#checkPayment uses a REQUIRES_NEW
        entityManager.flush();
        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();
        paymentBackgroundCheck.periodicCheckPayments();
        entityManager.flush();
        TestTransaction.flagForCommit();
        TestTransaction.end();

        Order o = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatusType.PAID, o.getStatus());
        assertNotNull(o.getPaymentAt());

        assertThat(orderEventRepository.findByOrderId(order.getId())).map(oe -> oe.getAction())
            .containsExactly("paid");
    }

    @Test
    @Transactional
    public void periodicCheckPaymentsFailed()
    {
        User user = userRepository.save(new User("apuig", "encoded-password", PASSENGER));
        order = new Order(user, "A1");
        order.setStatus(OrderStatusType.PAYMENT_IN_PROGRESS);
        order.setPaymentType(PaymentType.STRIPE);
        order.setPaymentToken("testToken");
        order.setPaymentTransaction("testTransaction");
        order = orderRepository.save(order);

        when(stripe.status(eq("testTransaction"))).thenReturn(PaymentStatus.DECLINED);

        // NOTE need new transaction in test because OrderService#checkPayment uses a REQUIRES_NEW
        entityManager.flush();
        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();
        paymentBackgroundCheck.periodicCheckPayments();
        entityManager.flush();
        TestTransaction.flagForCommit();
        TestTransaction.end();

        Order o = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatusType.PAYMENT_FAILED, o.getStatus());
        assertNull(o.getPaymentAt());

        assertThat(orderEventRepository.findByOrderId(order.getId())).map(oe -> oe.getAction())
            .containsExactly("paymentFailed");
    }
}