package net.apuig.order;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import net.apuig.order.dto.AddProductToOrderRequestDto;
import net.apuig.order.dto.ModifyOrderSeatRequestDto;
import net.apuig.order.dto.ModifyProductInOrderAmountRequestDto;
import net.apuig.order.dto.OrderDto;
import net.apuig.order.dto.OrderEventDto;
import net.apuig.order.dto.PayOrderInGatewayRequestDto;
import net.apuig.order.dto.ProductInOrderDto;
import net.apuig.order.dto.StartOrderRequestDto;

@RestController
public class OrderController
{
    private final OrderService orderService;

    public OrderController(OrderService orderService)
    {
        this.orderService = orderService;
    }

    /** list orders */
    @GetMapping("/orders")
    @PreAuthorize("hasAuthority('PASSENGER')")
    public ResponseEntity<PagedModel<OrderDto>> listOrders(
        @RequestParam(defaultValue = "0") final int pageNo,
        @RequestParam(defaultValue = "10") @Valid @Max(50) final int pageSize,
        @AuthenticationPrincipal final User loginInfo)
    {
        return ResponseEntity.ok(new PagedModel<>(
            orderService.listOrders(loginInfo.getUsername(), PageRequest.of(pageNo, pageSize))));
    }

    /** get order */
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasAuthority('PASSENGER')")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId,
        @AuthenticationPrincipal final User loginInfo)
    {
        return ResponseEntity.ok(orderService.getOrderDto(orderId, loginInfo.getUsername()));
    }

    /** start an order */
    @PostMapping("/orders")
    @PreAuthorize("hasAuthority('PASSENGER')")
    public ResponseEntity<OrderDto> startOrder(@RequestBody @Valid StartOrderRequestDto request,
        @AuthenticationPrincipal final User loginInfo)
    {
        OrderDto order = orderService.startOrder(loginInfo.getUsername(), request.seat());
        return ResponseEntity.created(URI.create("/orders/" + order.orderId())).body(order);
    }

    /** modify the seat of an order */
    @PutMapping("/orders/{orderId}")
    @PreAuthorize("hasAuthority('PASSENGER')")
    public ResponseEntity<Void> modifyOrderSeat(@PathVariable Long orderId,
        @Valid @RequestBody ModifyOrderSeatRequestDto request,
        @AuthenticationPrincipal final User loginInfo)
    {
        orderService.modifySeat(orderId, loginInfo.getUsername(), request.seatLocation());
        return ResponseEntity.ok().build();
    }

    /** add a product to an order */
    @PostMapping("/orders/{orderId}/products")
    @PreAuthorize("hasAuthority('PASSENGER')")
    public ResponseEntity<OrderDto> addProductToOrder(@PathVariable Long orderId,
        @RequestBody @Valid AddProductToOrderRequestDto request,
        @AuthenticationPrincipal final User loginInfo)
    {
        orderService.addProductToOrder(orderId, loginInfo.getUsername(), request.productId(),
            request.amount());
        // return the new order price
        return ResponseEntity.ok(orderService.getOrderDto(orderId, loginInfo.getUsername()));
    }

    /** list the products in an order */
    @GetMapping("/orders/{orderId}/products")
    @PreAuthorize("hasAuthority('PASSENGER')")
    public ResponseEntity<List<ProductInOrderDto>> listProductsInOrder(@PathVariable Long orderId,
        @AuthenticationPrincipal final User loginInfo)
    {
        return ResponseEntity
            .ok(orderService.listProductsInOrder(orderId, loginInfo.getUsername()));
    }

    /** modify the product amount in an order. Set to 0 to remove it */
    @PutMapping("/orders/{orderId}/products")
    @PreAuthorize("hasAuthority('PASSENGER')")
    public ResponseEntity<OrderDto> modifyAmountOfProductInOrder(@PathVariable Long orderId,
        @RequestBody @Valid ModifyProductInOrderAmountRequestDto request,
        @AuthenticationPrincipal final User loginInfo)
    {
        orderService.modifyProductInOrderAmount(orderId, loginInfo.getUsername(),
            request.productId(), request.newAmount());
        // return the new order price
        return ResponseEntity.ok(orderService.getOrderDto(orderId, loginInfo.getUsername()));
    }

    /** cancel an order */
    @PostMapping("/orders/{orderId}/cancel")
    @PreAuthorize("hasAuthority('PASSENGER')")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId,
        @AuthenticationPrincipal final User loginInfo)
    {
        orderService.cancelOrder(orderId, loginInfo.getUsername());
        return ResponseEntity.ok().build();
    }

    /** finish an order, the user doesn't want more products */
    @PostMapping("/orders/{orderId}/finish")
    @PreAuthorize("hasAuthority('PASSENGER')")
    public ResponseEntity<OrderDto> finishOrder(@PathVariable Long orderId,
        @AuthenticationPrincipal final User loginInfo)
    {
        orderService.finishOrder(orderId, loginInfo.getUsername());
        return ResponseEntity.ok(orderService.getOrderDto(orderId, loginInfo.getUsername()));
    }

    /** pay an order */
    @PostMapping("/orders/{orderId}/pay")
    @PreAuthorize("hasAuthority('PASSENGER')")
    public ResponseEntity<Void> payOrder(@PathVariable Long orderId,
        @Valid @RequestBody PayOrderInGatewayRequestDto request,
        @AuthenticationPrincipal final User loginInfo)
    {
        try
        {
            orderService.payOrder(orderId, loginInfo.getUsername(), request);
        }
        catch (Exception e)
        {
            // TODO not really if really possible, but if we could include a storeId and orderId in
            // the request to the payment gateway, then we could try to refund at this point -- so
            // we will need a way to retrieve the transactionId based on storeId and orderId

            // additionally: payOrder method could also use TransactionTemplate to more fine grained
            // control so we can try to commit with the context of payToken and payTransaction, this
            // will cover any unexpected DB error to request the refund for a specific transaction

            // NOTE spring.jpa.open-in-view=false will allow both of those mechanism:
            // there is no transaction on the controller
            throw e;
        }

        return ResponseEntity.ok().build();
    }

    // ATTENDANT methods

    /** attendant delivered an order */
    @PostMapping("/attendant/orders/{orderId}/delivered")
    @PreAuthorize("hasAuthority('ATTENDANT')")
    public ResponseEntity<Void> attendantDeliverOrder(@PathVariable Long orderId,
        @AuthenticationPrincipal final User loginInfo)
    {
        orderService.deliverOrder(orderId, loginInfo.getUsername());
        return ResponseEntity.ok().build();
    }

    // TODO attendantRefundOrder

    /** attendant list orders */
    @GetMapping("/attendant/orders")
    @PreAuthorize("hasAuthority('ATTENDANT')")
    public ResponseEntity<PagedModel<OrderDto>> attendantListOrder(
        @RequestParam(required = false) final String seat,
        @RequestParam(required = false) final Set<OrderStatusType> status,
        @RequestParam(defaultValue = "0") final int pageNo,
        @RequestParam(defaultValue = "10") @Valid @Max(50) final int pageSize)
    {
        return ResponseEntity.ok(new PagedModel<>(orderService.listOrders(seat,
            status == null || status.isEmpty() ? OrderStatusType.ALL : status,
            PageRequest.of(pageNo, pageSize))));
    }

    /** attendant list the products in an order */
    @GetMapping("/attendant/orders/{orderId}/products")
    @PreAuthorize("hasAuthority('ATTENDANT')")
    public ResponseEntity<List<ProductInOrderDto>> attendantGetProductsInOrder(
        @PathVariable Long orderId)
    {
        return ResponseEntity.ok(orderService.listProductsInOrder(orderId));
    }

    /** attendant list the events of an order */
    @GetMapping("/attendant/orders/{orderId}/events")
    @PreAuthorize("hasAuthority('ATTENDANT')")
    public ResponseEntity<List<OrderEventDto>> attendantListOrderEvents(@PathVariable Long orderId)
    {
        return ResponseEntity.ok(orderService.listProductEvents(orderId));
    }
}
