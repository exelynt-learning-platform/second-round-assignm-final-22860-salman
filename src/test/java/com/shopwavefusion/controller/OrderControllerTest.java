package com.shopwavefusion.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopwavefusion.modal.Address;
import com.shopwavefusion.modal.Order;
import com.shopwavefusion.modal.PaymentDetails;
import com.shopwavefusion.modal.User;
import com.shopwavefusion.request.CreateOrderRequest;
import com.shopwavefusion.service.OrderService;
import com.shopwavefusion.user.domain.OrderStatus;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private Order testOrder;
    private User testUser;
    private Address testAddress;
    private CreateOrderRequest orderRequest;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setFirstName("John");

        testAddress = new Address();
        testAddress.setId(1L);
        testAddress.setStreetAddress("123 Main St");
        testAddress.setCity("NYC");
        testAddress.setState("NY");
        testAddress.setZipCode("10001");
        testAddress.setUser(testUser);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderId(UUID.randomUUID().toString());
        testOrder.setUser(testUser);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setTotalPrice(1000);
        testOrder.setTotalDiscountedPrice(800);
        testOrder.setOrderStatus(OrderStatus.PENDING);
        testOrder.setShippingAddress(testAddress);
        testOrder.setPaymentDetails(new PaymentDetails());
        testOrder.setOrderItems(new ArrayList<>());

        orderRequest = new CreateOrderRequest();
        orderRequest.setAddressId(1L);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testCreateOrder_Success() throws Exception {
        when(orderService.createOrder(any(User.class), any(Address.class))).thenReturn(testOrder);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.orderStatus", is("PENDING")));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testCreateOrder_InvalidAddress() throws Exception {
        orderRequest.setAddressId(null);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testGetOrderDetails_Success() throws Exception {
        when(orderService.findOrderById(1L)).thenReturn(testOrder);

        mockMvc.perform(get("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.orderStatus", is("PENDING")));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testGetOrderDetails_NotFound() throws Exception {
        when(orderService.findOrderById(999L)).thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(get("/orders/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testGetUserOrders_Success() throws Exception {
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);

        when(orderService.getUserOrders(1L)).thenReturn(orders);

        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testGetUserOrders_Empty() throws Exception {
        when(orderService.getUserOrders(1L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testCancelOrder_Success() throws Exception {
        testOrder.setOrderStatus(OrderStatus.CANCELLED);
        when(orderService.cancelOrder(1L)).thenReturn(testOrder);

        mockMvc.perform(delete("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus", is("CANCELLED")));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testCancelOrder_AlreadyShipped() throws Exception {
        when(orderService.cancelOrder(1L)).thenThrow(new RuntimeException("Cannot cancel shipped order"));

        mockMvc.perform(delete("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    public void testGetAllOrders_ByAdmin() throws Exception {
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);

        when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/admin/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testGetAllOrders_UnauthorizedUser() throws Exception {
        mockMvc.perform(get("/admin/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    public void testUpdateOrderStatus_ByAdmin() throws Exception {
        testOrder.setOrderStatus(OrderStatus.CONFIRMED);
        when(orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED)).thenReturn(testOrder);

        mockMvc.perform(put("/admin/orders/1/status")
                        .param("status", "CONFIRMED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus", is("CONFIRMED")));
    }

    @Test
    public void testCreateOrder_Unauthorized() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testGetOrderByOrderId_Success() throws Exception {
        when(orderService.findOrderById(1L)).thenReturn(testOrder);

        mockMvc.perform(get("/orders/1/details")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testGetOrderStatus_Success() throws Exception {
        when(orderService.findOrderById(1L)).thenReturn(testOrder);

        mockMvc.perform(get("/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    public void testGetOrdersByStatus_ByAdmin() throws Exception {
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);

        when(orderService.getOrdersByStatus(OrderStatus.PENDING)).thenReturn(orders);

        mockMvc.perform(get("/admin/orders/search")
                        .param("status", "PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testTrackOrder_Success() throws Exception {
        when(orderService.findOrderById(1L)).thenReturn(testOrder);

        mockMvc.perform(get("/orders/1/track")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }
}
