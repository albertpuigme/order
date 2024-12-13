package net.apuig.order;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nullable;
import net.apuig.order.dto.OrderDto;
import net.apuig.order.dto.OrderEventDto;
import net.apuig.order.dto.PayOrderInGatewayRequestDto;
import net.apuig.order.dto.ProductInOrderDto;
import net.apuig.order.exception.OrderEmptyException;
import net.apuig.order.exception.OrderInvalidActionException;
import net.apuig.order.exception.OrderInvalidPaymentException;
import net.apuig.order.exception.OrderNotFoundException;
import net.apuig.order.exception.ProductAlreadyExistsException;
import net.apuig.order.exception.ProductInventoryException;
import net.apuig.order.exception.ProductNotFoundException;
import net.apuig.order.pay.GenerateTokenResult;
import net.apuig.order.pay.PaymentGatewayService;
import net.apuig.order.pay.PaymentResult;
import net.apuig.order.pay.PaymentStatus;
import net.apuig.product.CurrencyType;
import net.apuig.product.Product;
import net.apuig.product.ProductRepository;
import net.apuig.user.User;
import net.apuig.user.UserService;

@Service
@Transactional(readOnly = true)
public class OrderService
{
    private final UserService userService;

    private final PaymentGatewayService paymentGatewayService;

    private final ProductRepository productRepository;

    private final OrderRepository orderRepository;

    private final ProductInOrderRepository productInOrderRepository;

    private final OrderEventRepository orderEventRepository;

    public OrderService(UserService userService, PaymentGatewayService paymentGatewayService,
        ProductRepository productRepository, OrderRepository orderRepository,
        ProductInOrderRepository productInOrderRepository,
        OrderEventRepository orderEventRepository)
    {
        this.userService = userService;
        this.paymentGatewayService = paymentGatewayService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.productInOrderRepository = productInOrderRepository;
        this.orderEventRepository = orderEventRepository;
    }

    @Transactional
    public OrderDto startOrder(String userName, String seat)
    {
        User user = userService.getPassenger(userName);

        // TODO seat validation, it should depend on the kind of trip
        Order order = orderRepository.save(new Order(user, seat));
        orderEventRepository.save(new OrderEvent(order, "start", "seat: " + seat));

        return toDto(order);
    }

    @Transactional
    public void modifySeat(Long orderId, String userName, String seat)
    {
        Order order = getOrder(orderId, userName);
        if (!order.getStatus().allowModifySeat)
        {
            throw new OrderInvalidActionException("modifySeat", order.getStatus());
        }

        order.setSeat(seat);
        orderEventRepository.save(new OrderEvent(order, "modifySeat", "seat: " + seat));
    }

    @Transactional
    public void addProductToOrder(Long orderId, String userName, Long productId, Integer amount)
    {
        // TODO consider a max amount
        // TODO consider a max number of products by order
        Order order = getOrder(orderId, userName);
        if (!order.getStatus().allowModifyProducts)
        {
            throw new OrderInvalidActionException("addProduct", order.getStatus());
        }

        Product product =
            productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);

        if (productInOrderRepository.existsByOrderIdAndProductId(orderId, productId))
        {
            throw new ProductAlreadyExistsException();
        }

        // Product#version will handle prevent concurrent modifications of the stock
        product.setAvailable(product.getAvailable() - amount);
        if (product.getAvailable() < 0)
        {
            throw new ProductInventoryException();
        }

        productInOrderRepository.save(new ProductInOrder(product, order, amount));
        orderEventRepository.save(new OrderEvent(order, "addProduct",
            "product: %s (%d) amount: %d".formatted(product.getName(), productId, amount)));
    }

    @Transactional
    public void modifyProductInOrderAmount(Long orderId, String userName, Long productId,
        Integer amount)
    {
        // TODO consider a max amount
        Order order = getOrder(orderId, userName);
        ProductInOrder productInOrder = productInOrderRepository
            .findByOrderIdAndOrderUserNameAndProductId(orderId, userName, productId)
            .orElseThrow(ProductNotFoundException::new);

        if (Objects.equals(productInOrder.getAmount(), amount))
        {
            return;
        }

        if (!order.getStatus().allowModifyProducts)
        {
            throw new OrderInvalidActionException("modifyProduct", order.getStatus());
        }

        Integer diff = amount - productInOrder.getAmount();
        Product product = productInOrder.getProduct();
        // Product#version will handle prevent concurrent modifications of the stock
        product.setAvailable(product.getAvailable() - diff);
        if (product.getAvailable() < 0)
        {
            throw new ProductInventoryException();
        }

        if (amount.intValue() == 0)
        {
            productInOrderRepository.delete(productInOrder);

            orderEventRepository.save(new OrderEvent(order, "removeProduct",
                "product: %s (%d)".formatted(product.getName(), productId)));
        }
        else
        {
            productInOrder.setAmount(amount);
            
            orderEventRepository.save(new OrderEvent(order, "modifyProduct",
                "product: %s (%d) amount: %d".formatted(product.getName(), productId, amount)));
        }
    }

    @Transactional
    public void cancelOrder(Long orderId, String userName)
    {
        Order order = getOrder(orderId, userName);
        if (!order.getStatus().allowCancel)
        {
            throw new OrderInvalidActionException("cancelOrder", order.getStatus());
        }
        order.setStatus(OrderStatusType.CANCELLED);
        orderEventRepository.save(new OrderEvent(order, "cancelOrder", null));
    }

    @Transactional
    public void finishOrder(Long orderId, String userName)
    {
        Order order = getOrder(orderId, userName);
        if (!order.getStatus().allowFinish)
        {
            throw new OrderInvalidActionException("finishOrder", order.getStatus());
        }
        
        if(productInOrderRepository.findByOrderId(orderId).isEmpty())
        {
            throw new OrderEmptyException();
        }
        
        order.setStatus(OrderStatusType.PENDING_PAYMENT);
        orderEventRepository.save(new OrderEvent(order, "finishOrder", null));
    }

    // TODO consider TransactionTemplate
    @Transactional(noRollbackFor = OrderInvalidPaymentException.class)
    public void payOrder(Long orderId, String userName, PayOrderInGatewayRequestDto payment)
    {
        Order order = getOrder(orderId, userName);
        if (!order.getStatus().allowPay)
        {
            throw new OrderInvalidActionException("payOrder", order.getStatus());
        }

        GenerateTokenResult token = paymentGatewayService.generateToken(payment);
        if (token.isError())
        {
            orderEventRepository.save(new OrderEvent(order, "payOrderInvalidToken", token.error()));

            // NOTE if its not possible to generate the token (eg invalid credit card) the order
            // status is not changed
            throw new OrderInvalidPaymentException(token.error());
        }
        
        // TODO currency should be part or trip information or based on a User property (ala Locale for translation)
        CurrencyType currency = productInOrderRepository.findByOrderId(orderId).getFirst().getProduct().getCurrency();

        PaymentResult pay = paymentGatewayService.pay(payment.type(), token.token(), order.getTotalPrice(), currency);
        if (pay.isError())
        {
            orderEventRepository.save(new OrderEvent(order, "payOrderInvalidPay", pay.error()));
            // NOTE do not change order status or save token
            throw new OrderInvalidPaymentException(pay.error());
        }

        order.setPaymentType(payment.type());
        order.setPaymentToken(token.token());
        order.setPaymentTransaction(pay.transactionId());

        order.setStatus(OrderStatusType.PAYMENT_IN_PROGRESS);

        orderEventRepository.save(
            new OrderEvent(order, "payOrder", "In progress. type: %s token: %s, transactionId: %s"
                .formatted(payment.type(), token.token(), pay.transactionId())));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkPayment(Long orderId)
    {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty())
        {
            return;
        }
        Order order = orderOpt.get();

        PaymentStatus status =
            paymentGatewayService.status(order.getPaymentType(), order.getPaymentTransaction());

        switch (status)
        {
            case PENDING:
                // TODO some timeout ?
                return;

            case APPROVED:
                order.setStatus(OrderStatusType.PAID);
                order.setPaymentAt(ZonedDateTime.now(ZoneOffset.UTC));
                orderEventRepository.save(new OrderEvent(order, "paid", null));
                break;

            default:
                order.setStatus(OrderStatusType.PAYMENT_FAILED);
                orderEventRepository
                    .save(new OrderEvent(order, "paymentFailed", "status: " + status.name()));
        }
    }

    @Transactional
    public void deliverOrder(Long orderId, String attendantName)
    {
        Order order = orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
        if (!order.getStatus().allowDelivery)
        {
            throw new OrderInvalidActionException("deliverOrder", order.getStatus());
        }

        order.setStatus(OrderStatusType.DELIVERED);
        orderEventRepository
            .save(new OrderEvent(order, "delivered", "By attendant: " + attendantName));
    }

    public OrderDto getOrderDto(Long orderId, String userName)
    {
        return toDto(getOrder(orderId, userName));
    }

    public Page<OrderDto> listOrders(String userName, Pageable page)
    {
        return orderRepository.findByUserName(userName, page).map(OrderService::toDto);
    }

    public Page<OrderDto> listOrders(@Nullable String seat, Set<OrderStatusType> status,
        Pageable page)
    {
        return orderRepository.findBySeatAndStatusIn(seat, status, page).map(OrderService::toDto);
    }

    public List<ProductInOrderDto> listProductsInOrder(Long orderId, String userName)
    {
        return productInOrderRepository.findByOrderIdAndOrderUserName(orderId, userName).stream()
            .map(OrderService::toDto).toList();
    }

    public List<ProductInOrderDto> listProductsInOrder(Long orderId)
    {
        return productInOrderRepository.findByOrderId(orderId).stream().map(OrderService::toDto)
            .toList();
    }

    public List<OrderEventDto> listProductEvents(Long orderId)
    {
        return orderEventRepository.findByOrderId(orderId).stream().map(OrderService::toDto)
            .toList();
    }

    private Order getOrder(Long orderId, String userName)
    {
        Order order = orderRepository.findByIdAndUserName(orderId, userName);
        if (order == null)
        {
            throw new OrderNotFoundException();
        }
        return order;
    }

    private static OrderDto toDto(Order o)
    {
        return new OrderDto(o.getId(), o.getUser().getId(), o.getSeat(), o.getStatus(),
            o.getTotalPrice() == null ? 0.0f : o.getTotalPrice());
    }

    private static ProductInOrderDto toDto(ProductInOrder pio)
    {
        Product p = pio.getProduct();
        return new ProductInOrderDto(p.getId(), p.getName(), pio.getAmount(), p.getPrice());
    }

    private static OrderEventDto toDto(OrderEvent e)
    {
        return new OrderEventDto(e.getAction(), e.getDetails(), e.getTime());
    }
}
