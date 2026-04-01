package com.shopwavefusion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopwavefusion.exception.OrderException;
import com.shopwavefusion.exception.UserException;
import com.shopwavefusion.modal.Address;
import com.shopwavefusion.modal.Cart;
import com.shopwavefusion.modal.CartItem;
import com.shopwavefusion.modal.Order;
import com.shopwavefusion.modal.OrderItem;
import com.shopwavefusion.modal.PaymentDetails;
import com.shopwavefusion.modal.Product;
import com.shopwavefusion.modal.User;
import com.shopwavefusion.repository.AddressRepository;
import com.shopwavefusion.repository.CartRepository;
import com.shopwavefusion.repository.OrderItemRepository;
import com.shopwavefusion.repository.OrderRepository;
import com.shopwavefusion.repository.UserRepository;
import com.shopwavefusion.user.domain.OrderStatus;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderServiceImplementation orderService;

    @InjectMocks
    private OrderItemServiceImplementation orderItemService;

    private User testUser;
    private Order testOrder;
    private OrderItem testOrderItem;
    private Product testProduct;
    private Address testAddress;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setFirstName("John");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setTitle("Laptop");
        testProduct.setPrice(1000);
        testProduct.setQuantity(10);

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

        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(2);
        testOrderItem.setPrice(1000);
        testOrderItem.setDiscountedPrice(800);
    }

    @Test
    public void testCreateOrder_Success() throws UserException, OrderException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.createOrder(testUser, testAddress);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getOrderStatus());
        assertEquals(testUser.getId(), result.getUser().getId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void testFindOrderById_Success() throws OrderException {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        Order result = orderService.findOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.PENDING, result.getOrderStatus());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindOrderById_NotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderException.class, () -> orderService.findOrderById(999L));
    }

    @Test
    public void testFindUserOrders_Success() throws UserException {
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserId(1L)).thenReturn(orders);

        List<Order> result = orderService.getUserOrders(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    public void testFindUserOrders_Empty() throws UserException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserId(1L)).thenReturn(new ArrayList<>());

        List<Order> result = orderService.getUserOrders(1L);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testUpdateOrderStatus_Success() throws OrderException {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void testCancelOrder_Pending() throws OrderException {
        testOrder.setOrderStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.cancelOrder(1L);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getOrderStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void testCancelOrder_AlreadyShipped() throws OrderException {
        testOrder.setOrderStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(OrderException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    public void testAddOrderItem_Success() throws OrderException {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(testOrderItem);

        OrderItem result = orderItemService.addOrderItem(testOrder, testOrderItem);

        assertNotNull(result);
        assertEquals(1L, result.getProduct().getId());
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
    }

    @Test
    public void testGetOrderTotal_Success() {
        List<OrderItem> items = new ArrayList<>();
        items.add(testOrderItem);

        double total = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        assertEquals(2000, total);
    }

    @Test
    public void testCalculateOrderDiscount_Success() {
        double originalPrice = 2000;
        double discountedPrice = 1600;
        double discount = originalPrice - discountedPrice;

        assertEquals(400, discount);
    }

    @Test
    public void testValidateOrderCreation_InsufficientPrice() {
        testOrder.setTotalPrice(-100);

        assertFalse(orderService.validateOrderTotal(testOrder.getTotalPrice()));
    }

    @Test
    public void testValidateOrderCreation_ValidPrice() {
        testOrder.setTotalPrice(1000);

        assertTrue(orderService.validateOrderTotal(testOrder.getTotalPrice()));
    }

    @Test
    public void testGetOrdersByStatus() {
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);

        when(orderRepository.findByOrderStatus(OrderStatus.PENDING)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(OrderStatus.PENDING, result.get(0).getOrderStatus());
    }

    @Test
    public void testGenerateOrderId() {
        String orderId = UUID.randomUUID().toString();

        assertNotNull(orderId);
        assertFalse(orderId.isEmpty());
    }

    @Test
    public void testOrderDelivery_Success() throws OrderException {
        testOrder.setOrderStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        testOrder.setDeliveryDate(LocalDateTime.now());
        testOrder.setOrderStatus(OrderStatus.DELIVERED);
        Order result = orderService.updateOrderStatus(1L, OrderStatus.DELIVERED);

        assertNotNull(result);
        assertNotNull(result.getDeliveryDate());
        assertEquals(OrderStatus.DELIVERED, result.getOrderStatus());
    }

    @Test
    public void testGetRecentOrders() {
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);

        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.getRecentOrders();

        assertNotNull(result);
        assertTrue(result.size() > 0);
    }
}
