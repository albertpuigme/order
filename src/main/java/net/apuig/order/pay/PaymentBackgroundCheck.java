package net.apuig.order.pay;

import static net.apuig.order.OrderStatusType.PAYMENT_IN_PROGRESS;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.apuig.order.OrderRepository;
import net.apuig.order.OrderService;

@Component
public class PaymentBackgroundCheck
{
    private static final Logger LOG = LoggerFactory.getLogger(PaymentBackgroundCheck.class);

    private final OrderRepository orderRepository;

    private final OrderService orderService;

    public PaymentBackgroundCheck(OrderService orderService, OrderRepository orderRepository)
    {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @Scheduled(fixedDelayString = "${net.apuig.payment.checkseconds:5}", //
        initialDelayString = "${net.apuig.payment.delayseconds:60}", //
        timeUnit = TimeUnit.SECONDS)
    public void periodicCheckPayments()
    {
        for (Long pendingPayOrderId : orderRepository.findIdsByStatus(PAYMENT_IN_PROGRESS))
        {
            try
            {
                orderService.checkPayment(pendingPayOrderId);
            }
            catch (Exception e)
            {
                LOG.error("Cannot check payment of order " + pendingPayOrderId, e);
            }
        }
    }
}
