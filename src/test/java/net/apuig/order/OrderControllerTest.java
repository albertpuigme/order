package net.apuig.order;

import static net.apuig.user.UserType.PASSENGER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import net.apuig.StoreApplication;
import net.apuig.order.dto.AddProductToOrderRequestDto;
import net.apuig.order.dto.ModifyOrderSeatRequestDto;
import net.apuig.order.dto.ModifyProductInOrderAmountRequestDto;
import net.apuig.order.dto.PayOrderInStripeRequestDto;
import net.apuig.order.dto.StartOrderRequestDto;
import net.apuig.order.exception.OrderEmptyException;
import net.apuig.order.exception.OrderInvalidActionException;
import net.apuig.order.exception.OrderInvalidPaymentException;
import net.apuig.order.exception.ProductAlreadyExistsException;
import net.apuig.order.exception.ProductInventoryException;
import net.apuig.order.pay.GenerateTokenResult;
import net.apuig.order.pay.PaymentGatewayStripe;
import net.apuig.order.pay.PaymentResult;
import net.apuig.order.pay.PaymentType;
import net.apuig.product.Product;
import net.apuig.product.ProductRepository;
import net.apuig.user.User;
import net.apuig.user.UserRepository;

@SpringBootTest(classes = StoreApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderControllerTest
{
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper json;

    @MockitoBean
    private PaymentGatewayStripe stripe;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductInOrderRepository productInOrderRepository;

    @Autowired
    private OrderEventRepository orderEventRepository;

    @Autowired
    private ProductRepository productRepository;

    private User user;

    private Order order;

    private Product product;

    @BeforeAll
    public void setup()
    {
        // already running EntitySetup. Categories, Products and Attendants
        user = userRepository.save(new User("apuig", passwordEncoder.encode("shhh!!"), PASSENGER));
        order = orderRepository.save(new Order(user, "A1"));
        product = productRepository.findByName("Espresso");

        userRepository.save(new User("bob", passwordEncoder.encode("shhh!!"), PASSENGER));
    }

    @AfterAll
    public void cleanup()
    {
        orderEventRepository.deleteAll();
        productInOrderRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.delete(user);
    }

    @Test
    public void listOrders() throws Exception
    {
        this.mockMvc.perform(get("/orders")//
            .with(httpBasic("apuig", "shhh!!"))//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.page.number").value("0"))
            .andExpect(jsonPath("$.page.size").value("10"))
            .andExpect(jsonPath("$.page.totalPages").value("1"))
            .andExpect(jsonPath("$.page.totalElements").value("1"))
            .andExpect(jsonPath("$.content[0].orderId").value(order.getId()))
            .andExpect(jsonPath("$.content[0].passengerId").value(user.getId()))
            .andExpect(jsonPath("$.content[0].seat").value(order.getSeat()))
            .andExpect(jsonPath("$.content[0].status").value("OPEN"))
            .andExpect(jsonPath("$.content[0].price").value("0.0"));
    }

    @Test
    public void getOrder() throws Exception
    {
        this.mockMvc.perform(get("/orders/%d".formatted(order.getId()))//
            .with(httpBasic("apuig", "shhh!!"))//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.orderId").value(order.getId()))
            .andExpect(jsonPath("$.passengerId").value(user.getId()))
            .andExpect(jsonPath("$.seat").value(order.getSeat()))
            .andExpect(jsonPath("$.status").value("OPEN"))
            .andExpect(jsonPath("$.price").value("0.0"));
    }

    @Test
    public void getOrderOtherPassenger() throws Exception
    {
        this.mockMvc.perform(get("/orders/%d".formatted(order.getId()))//
            .with(httpBasic("bob", "shhh!!"))//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void startOrder() throws Exception
    {
        String orderUrl = this.mockMvc.perform(post("/orders")//
            .content(json.writeValueAsBytes(new StartOrderRequestDto("A2")))
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isCreated())//
            .andExpect(jsonPath("$.orderId").exists())
            .andExpect(jsonPath("$.passengerId").value(user.getId()))
            .andExpect(jsonPath("$.seat").value("A2")).andExpect(jsonPath("$.status").value("OPEN"))
            .andExpect(jsonPath("$.price").value("0.0")).andReturn().getResponse()
            .getHeader(HttpHeaders.LOCATION);

        assertThat(orderUrl).startsWith("/orders/");
        Long orderId = Long.parseLong(orderUrl.substring(orderUrl.lastIndexOf('/') + 1));
        assertThat(orderEventRepository.findByOrderId(orderId)).map(oe -> oe.getAction())
            .containsExactly("start");
    }

    @Test
    @Transactional
    public void modifyOrderSeat() throws Exception
    {
        this.mockMvc.perform(put("/orders/%s".formatted(order.getId()))//
            .content(json.writeValueAsBytes(new ModifyOrderSeatRequestDto("A3")))
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk());

        assertThat(orderRepository.findById(order.getId()).orElseThrow())
            .matches(o -> Objects.equals(o.getSeat(), "A3"));
        assertThat(orderEventRepository.findByOrderId(order.getId())).map(oe -> oe.getAction())
            .containsExactly("modifySeat");
    }

    @Test
    @Transactional
    public void modifyOrderSeatAlreadyPaid() throws Exception
    {
        Order orderPaid = orderRepository.findById(order.getId()).orElseThrow();
        orderPaid.setStatus(OrderStatusType.PAID);

        this.mockMvc.perform(put("/orders/%s".formatted(order.getId()))//
            .content(json.writeValueAsBytes(new ModifyOrderSeatRequestDto("A3")))
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isConflict())//
            .andExpect(jsonPath("$.code").value(OrderInvalidActionException.class.getSimpleName()))
            .andExpect(
                jsonPath("$.message", allOf(containsString("modifySeat"), containsString("PAID"))));
    }

    @Test
    @Transactional
    public void addProductToOrder() throws Exception
    {
        this.mockMvc.perform(post("/orders/%d/products".formatted(order.getId()))//
            .content(json.writeValueAsBytes(new AddProductToOrderRequestDto(product.getId(), 2)))
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.price").exists());
        // NOTE cannot assert price because Test transaction doesn't commit, Formula not evaluated
        entityManager.flush();
        entityManager.clear();
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getTotalPrice())
            .isEqualTo(product.getPrice() * 2);

        assertThat(orderEventRepository.findByOrderId(order.getId())).map(oe -> oe.getAction())
            .containsExactly("addProduct");
    }

    @Test
    public void addProductToOrderAmount0() throws Exception
    {
        this.mockMvc.perform(post("/orders/%d/products".formatted(order.getId()))//
            .content(json.writeValueAsBytes(new AddProductToOrderRequestDto(product.getId(), 0)))
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void addProductToOrderAlready() throws Exception
    {
        productInOrderRepository.save(new ProductInOrder(product, order, 1));

        this.mockMvc.perform(post("/orders/%d/products".formatted(order.getId()))//
            .content(json.writeValueAsBytes(new AddProductToOrderRequestDto(product.getId(), 2)))
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isConflict())//
            .andExpect(
                jsonPath("$.code").value(ProductAlreadyExistsException.class.getSimpleName()));
    }

    @Test
    @Transactional
    public void addProductToOrderInventory() throws Exception
    {
        this.mockMvc.perform(post("/orders/%d/products".formatted(order.getId()))//
            .content(json.writeValueAsBytes(new AddProductToOrderRequestDto(product.getId(), 200)))
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isConflict())//
            .andExpect(jsonPath("$.code").value(ProductInventoryException.class.getSimpleName()));
    }

    @Test
    @Transactional
    public void getProductsInOrder() throws Exception
    {
        productInOrderRepository.save(new ProductInOrder(product, order, 1));

        this.mockMvc.perform(get("/orders/%d/products".formatted(order.getId()))//
            .with(httpBasic("apuig", "shhh!!"))//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.[0].productId").value(product.getId()))
            .andExpect(jsonPath("$.[0].productName").value(product.getName()))
            .andExpect(jsonPath("$.[0].amount").value("1"))
            .andExpect(jsonPath("$.[0].pricePerUnit").value(product.getPrice()));
    }

    @Test
    @Transactional
    public void modifyAmountOfProductInOrder() throws Exception
    {
        productInOrderRepository.save(new ProductInOrder(product, order, 1));

        this.mockMvc.perform(put("/orders/%d/products".formatted(order.getId()))//
            .content(json
                .writeValueAsBytes(new ModifyProductInOrderAmountRequestDto(product.getId(), 3)))
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.price").exists());
        // NOTE cannot assert price because Test transaction doesn't commit, Formula not evaluated
        entityManager.flush();
        entityManager.clear();
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getTotalPrice())
            .isEqualTo(product.getPrice() * 3);

        assertThat(orderEventRepository.findByOrderId(order.getId())).map(oe -> oe.getAction())
            .containsExactly("modifyProduct");
    }

    @Test
    @Transactional
    public void modifyAmountOfProductInInventory() throws Exception
    {
        productInOrderRepository.save(new ProductInOrder(product, order, 1));

        this.mockMvc.perform(put("/orders/%d/products".formatted(order.getId()))//
            .content(json
                .writeValueAsBytes(new ModifyProductInOrderAmountRequestDto(product.getId(), 200)))
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isConflict())//
            .andExpect(jsonPath("$.code").value(ProductInventoryException.class.getSimpleName()));
    }

    @Test
    @Transactional
    public void modifyAmountOfProductInOrderDelete() throws Exception
    {
        productInOrderRepository.save(new ProductInOrder(product, order, 1));

        this.mockMvc.perform(put("/orders/%d/products".formatted(order.getId()))//
            .content(json
                .writeValueAsBytes(new ModifyProductInOrderAmountRequestDto(product.getId(), 0)))
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.price").exists());
        // NOTE cannot assert price because Test transaction doesn't commit, Formula not evaluated
        entityManager.flush();
        entityManager.clear();
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getTotalPrice()).isNull();

        assertThat(orderEventRepository.findByOrderId(order.getId())).map(oe -> oe.getAction())
            .containsExactly("removeProduct");
    }

    @Test
    @Transactional
    public void cancelOrder() throws Exception
    {
        this.mockMvc.perform(post("/orders/%d/cancel".formatted(order.getId()))//
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk());

        assertThat(orderEventRepository.findByOrderId(order.getId())).map(oe -> oe.getAction())
            .containsExactly("cancelOrder");
    }

    @Test
    @Transactional
    public void cancelOrderAlreadyPaid() throws Exception
    {
        Order orderPaid = orderRepository.findById(order.getId()).orElseThrow();
        orderPaid.setStatus(OrderStatusType.PAID);

        this.mockMvc.perform(post("/orders/%d/cancel".formatted(order.getId()))//
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isConflict())//
            .andExpect(jsonPath("$.code").value(OrderInvalidActionException.class.getSimpleName()))
            .andExpect(jsonPath("$.message",
                allOf(containsString("cancelOrder"), containsString("PAID"))));
    }

    @Test
    @Transactional
    public void finishOrder() throws Exception
    {
        productInOrderRepository.save(new ProductInOrder(product, order, 1));

        this.mockMvc.perform(post("/orders/%d/finish".formatted(order.getId()))//
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk());

        assertThat(orderEventRepository.findByOrderId(order.getId())).map(oe -> oe.getAction())
            .containsExactly("finishOrder");
    }

    @Test
    public void finishOrderNoProducts() throws Exception
    {
        this.mockMvc.perform(post("/orders/%d/finish".formatted(order.getId()))//
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isConflict())//
            .andExpect(jsonPath("$.code").value(OrderEmptyException.class.getSimpleName()));
    }

    @Test
    @Transactional
    public void finishOrderAlreadyPaid() throws Exception
    {
        Order orderPaid = orderRepository.findById(order.getId()).orElseThrow();
        orderPaid.setStatus(OrderStatusType.PAID);

        this.mockMvc.perform(post("/orders/%d/finish".formatted(order.getId()))//
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isConflict())//
            .andExpect(jsonPath("$.code").value(OrderInvalidActionException.class.getSimpleName()))
            .andExpect(
                jsonPath("$.message", allOf(containsString("finish"), containsString("PAID"))));
    }

    @Test
    @Transactional
    public void payOrder() throws Exception
    {
        order.setStatus(OrderStatusType.PENDING_PAYMENT);
        orderRepository.save(order);
        productInOrderRepository.save(new ProductInOrder(product, order, 1));
        entityManager.flush();
        entityManager.clear();

        PayOrderInStripeRequestDto paymentRequest =
            new PayOrderInStripeRequestDto(null, "card_123456789ABCDEFGHIJKLMNO");

        when(stripe.generateToken(any())).thenReturn(GenerateTokenResult.ok("testToken"));
        when(stripe.pay(eq("testToken"), anyFloat(), any()))
            .thenReturn(PaymentResult.ok("testTransaction"));

        this.mockMvc.perform(post("/orders/%d/pay".formatted(order.getId()))//
            .content(json.writeValueAsBytes(paymentRequest))//
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk());

        verify(stripe).generateToken(eq(paymentRequest));
        verify(stripe).pay(eq("testToken"), eq(product.getPrice()), eq(product.getCurrency()));

        Order o = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals("testToken", o.getPaymentToken());
        assertEquals("testTransaction", o.getPaymentTransaction());
        assertEquals(PaymentType.STRIPE, o.getPaymentType());
        assertEquals(OrderStatusType.PAYMENT_IN_PROGRESS, o.getStatus());

        assertThat(orderEventRepository.findByOrderId(order.getId())).map(oe -> oe.getAction())
            .containsExactly("payOrder");
    }

    @Test
    @Transactional
    public void payOrderFailGenerateToken() throws Exception
    {
        order.setStatus(OrderStatusType.PENDING_PAYMENT);
        orderRepository.save(order);
        productInOrderRepository.save(new ProductInOrder(product, order, 1));
        entityManager.flush();
        entityManager.clear();

        when(stripe.generateToken(any())).thenReturn(GenerateTokenResult.error("errorInToken"));

        this.mockMvc.perform(post("/orders/%d/pay".formatted(order.getId()))//
            .content(json.writeValueAsBytes(
                new PayOrderInStripeRequestDto(null, "card_123456789ABCDEFGHIJKLMNO")))//
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isBadRequest())//
            .andExpect(
                jsonPath("$.code").value(OrderInvalidPaymentException.class.getSimpleName()));

        assertEquals(OrderStatusType.PENDING_PAYMENT,
            orderRepository.findById(order.getId()).orElseThrow().getStatus());
        assertThat(orderEventRepository.findByOrderId(order.getId())).map(oe -> oe.getAction())
            .containsExactly("payOrderInvalidToken");
    }

    @Test
    @Transactional
    public void payOrderFailPayment() throws Exception
    {
        order.setStatus(OrderStatusType.PENDING_PAYMENT);
        orderRepository.save(order);
        productInOrderRepository.save(new ProductInOrder(product, order, 1));
        entityManager.flush();
        entityManager.clear();

        PayOrderInStripeRequestDto paymentRequest =
            new PayOrderInStripeRequestDto(null, "card_123456789ABCDEFGHIJKLMNO");

        when(stripe.generateToken(any())).thenReturn(GenerateTokenResult.ok("testToken"));
        when(stripe.pay(eq("testToken"), anyFloat(), any()))
            .thenReturn(PaymentResult.error("errorInPay"));

        this.mockMvc.perform(post("/orders/%d/pay".formatted(order.getId()))//
            .content(json.writeValueAsBytes(paymentRequest))//
            .with(httpBasic("apuig", "shhh!!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isBadRequest())//
            .andExpect(
                jsonPath("$.code").value(OrderInvalidPaymentException.class.getSimpleName()));

        verify(stripe).generateToken(eq(paymentRequest));

        assertEquals(OrderStatusType.PENDING_PAYMENT,
            orderRepository.findById(order.getId()).orElseThrow().getStatus());
        assertThat(orderEventRepository.findByOrderId(order.getId())).map(oe -> oe.getAction())
            .containsExactly("payOrderInvalidPay");
    }

    @Test
    @Transactional
    public void attendantDeliverOrder() throws Exception
    {
        Order orderPaid = orderRepository.findById(order.getId()).orElseThrow();
        orderPaid.setStatus(OrderStatusType.PAID);

        this.mockMvc.perform(post("/attendant/orders/%d/delivered".formatted(order.getId()))//
            .with(httpBasic("att1", "Att1Passowrd!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk());

        assertThat(orderEventRepository.findByOrderId(order.getId())).map(oe -> oe.getAction())
            .containsExactly("delivered");
    }

    @Test
    public void attendantDeliverOrderNotPaid() throws Exception
    {
        this.mockMvc.perform(post("/attendant/orders/%d/delivered".formatted(order.getId()))//
            .with(httpBasic("att1", "Att1Passowrd!"))//
            .contentType(MediaType.APPLICATION_JSON)//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isConflict())//
            .andExpect(jsonPath("$.code").value(OrderInvalidActionException.class.getSimpleName()))
            .andExpect(jsonPath("$.message",
                allOf(containsString("deliverOrder"), containsString("OPEN"))));
    }

    @Test
    public void attendantListOrder() throws Exception
    {
        this.mockMvc.perform(get("/attendant/orders")//
            .with(httpBasic("att1", "Att1Passowrd!"))//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.page.number").value("0"))
            .andExpect(jsonPath("$.page.size").value("10"))
            .andExpect(jsonPath("$.page.totalPages").value("1"))
            .andExpect(jsonPath("$.page.totalElements").value("1"))
            .andExpect(jsonPath("$.content[0].orderId").value(order.getId()))
            .andExpect(jsonPath("$.content[0].passengerId").value(user.getId()))
            .andExpect(jsonPath("$.content[0].seat").value(order.getSeat()))
            .andExpect(jsonPath("$.content[0].status").value("OPEN"))
            .andExpect(jsonPath("$.content[0].price").value("0.0"));
    }

    @Test
    @Transactional
    public void attendantListOrderFilters() throws Exception
    {
        Order o = new Order(user, "C4");
        o.setStatus(OrderStatusType.PAID);
        o = orderRepository.save(o);

        // by seat
        this.mockMvc.perform(get("/attendant/orders")//
            .param("seat", "C4")//
            .with(httpBasic("att1", "Att1Passowrd!"))//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.page.size").value("10"))
            .andExpect(jsonPath("$.page.totalElements").value("1"))
            .andExpect(jsonPath("$.content[0].orderId").value(o.getId()))
            .andExpect(jsonPath("$.content[0].seat").value(o.getSeat()))
            .andExpect(jsonPath("$.content[0].status").value("PAID"));

        // by status
        this.mockMvc.perform(get("/attendant/orders")//
            .param("status", "PAID")//
            .with(httpBasic("att1", "Att1Passowrd!"))//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.page.size").value("10"))
            .andExpect(jsonPath("$.page.totalElements").value("1"))
            .andExpect(jsonPath("$.content[0].orderId").value(o.getId()))
            .andExpect(jsonPath("$.content[0].seat").value(o.getSeat()))
            .andExpect(jsonPath("$.content[0].status").value("PAID"));
    }

    @Test
    public void attendantListOrderNoPassenger() throws Exception
    {
        this.mockMvc.perform(get("/attendant/orders")//
            .with(httpBasic("apuig", "shhh!!"))//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void attendantGetProductsInOrder() throws Exception
    {
        productInOrderRepository.save(new ProductInOrder(product, order, 1));

        this.mockMvc.perform(get("/attendant/orders/%d/products".formatted(order.getId()))//
            .with(httpBasic("att1", "Att1Passowrd!"))//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.[0].productId").value(product.getId()))
            .andExpect(jsonPath("$.[0].productName").value(product.getName()))
            .andExpect(jsonPath("$.[0].amount").value("1"))
            .andExpect(jsonPath("$.[0].pricePerUnit").value(product.getPrice()));
    }

    @Test
    @Transactional
    public void attendantListOrderEvents() throws Exception
    {
        orderEventRepository.save(new OrderEvent(order, "foo", "bar"));

        this.mockMvc.perform(get("/attendant/orders/%d/events".formatted(order.getId()))//
            .with(httpBasic("att1", "Att1Passowrd!"))//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.[0].action").value("foo"))
            .andExpect(jsonPath("$.[0].details").value("bar"))
            .andExpect(jsonPath("$.[0].time").exists());
    }
}
