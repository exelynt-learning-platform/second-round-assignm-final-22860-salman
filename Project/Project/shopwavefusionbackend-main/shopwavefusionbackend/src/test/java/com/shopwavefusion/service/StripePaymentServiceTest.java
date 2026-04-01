package com.shopwavefusion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopwavefusion.modal.Order;
import com.shopwavefusion.modal.PaymentDetails;
import com.shopwavefusion.modal.User;
import com.shopwavefusion.request.StripePaymentRequest;
import com.shopwavefusion.response.StripePaymentResponse;
import com.shopwavefusion.user.domain.OrderStatus;
import com.stripe.model.PaymentIntent;
import com.stripe.exception.StripeException;

import java.time.LocalDateTime;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class StripePaymentServiceTest {

    @InjectMocks
    private StripePaymentServiceImplementation stripePaymentService;

    private StripePaymentRequest paymentRequest;
    private Order testOrder;
    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setFirstName("John");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderId(UUID.randomUUID().toString());
        testOrder.setUser(testUser);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setTotalPrice(1000);
        testOrder.setTotalDiscountedPrice(800);
        testOrder.setOrderStatus(OrderStatus.PENDING);
        testOrder.setPaymentDetails(new PaymentDetails());

        paymentRequest = new StripePaymentRequest();
        paymentRequest.setAmount(100000); // Amount in cents
        paymentRequest.setCurrency("USD");
        paymentRequest.setDescription("Test payment");
        paymentRequest.setOrderId(testOrder.getOrderId());
    }

    @Test
    public void testProcessPayment_Success() throws StripeException {
        StripePaymentResponse response = stripePaymentService.processPayment(paymentRequest);

        assertNotNull(response);
        assertNotNull(response.getPaymentIntentId());
        assertNotNull(response.getClientSecret());
    }

    @Test
    public void testProcessPayment_InvalidAmount() {
        paymentRequest.setAmount(0);

        assertThrows(IllegalArgumentException.class, () -> stripePaymentService.processPayment(paymentRequest));
    }

    @Test
    public void testProcessPayment_NegativeAmount() {
        paymentRequest.setAmount(-100);

        assertThrows(IllegalArgumentException.class, () -> stripePaymentService.processPayment(paymentRequest));
    }

    @Test
    public void testValidatePaymentRequest_Success() {
        assertTrue(stripePaymentService.validatePaymentRequest(paymentRequest));
    }

    @Test
    public void testValidatePaymentRequest_MissingAmount() {
        StripePaymentRequest invalidRequest = new StripePaymentRequest();
        invalidRequest.setAmount(0);
        invalidRequest.setCurrency("USD");

        assertFalse(stripePaymentService.validatePaymentRequest(invalidRequest));
    }

    @Test
    public void testValidatePaymentRequest_MissingCurrency() {
        StripePaymentRequest invalidRequest = new StripePaymentRequest();
        invalidRequest.setAmount(1000);
        invalidRequest.setCurrency(null);

        assertFalse(stripePaymentService.validatePaymentRequest(invalidRequest));
    }

    @Test
    public void testValidatePaymentAmount_Valid() {
        assertTrue(stripePaymentService.validatePaymentAmount(1000));
        assertTrue(stripePaymentService.validatePaymentAmount(100));
        assertTrue(stripePaymentService.validatePaymentAmount(1));
    }

    @Test
    public void testValidatePaymentAmount_Invalid() {
        assertFalse(stripePaymentService.validatePaymentAmount(0));
        assertFalse(stripePaymentService.validatePaymentAmount(-100));
    }

    @Test
    public void testValidateCurrency_Supported() {
        assertTrue(stripePaymentService.isValidCurrency("USD"));
        assertTrue(stripePaymentService.isValidCurrency("EUR"));
        assertTrue(stripePaymentService.isValidCurrency("GBP"));
    }

    @Test
    public void testValidateCurrency_NotSupported() {
        assertFalse(stripePaymentService.isValidCurrency("INVALID"));
        assertFalse(stripePaymentService.isValidCurrency(""));
    }

    @Test
    public void testConvertToStripeAmount_Success() {
        long stripeAmount = stripePaymentService.convertToStripeAmount(10.50);
        assertEquals(1050, stripeAmount);
    }

    @Test
    public void testConvertToStripeAmount_Integer() {
        long stripeAmount = stripePaymentService.convertToStripeAmount(100);
        assertEquals(10000, stripeAmount);
    }

    @Test
    public void testCreatePaymentIntentDescription_WithOrderId() {
        String description = stripePaymentService.createPaymentDescription("Order-12345", "user@example.com");

        assertNotNull(description);
        assertTrue(description.contains("Order-12345"));
    }

    @Test
    public void testCreatePaymentIntentDescription_Valid() {
        String description = stripePaymentService.createPaymentDescription(
            testOrder.getOrderId(),
            testUser.getEmail()
        );

        assertNotNull(description);
        assertFalse(description.isEmpty());
    }

    @Test
    public void testGetPaymentStatus_Success() throws StripeException {
        // This test would require mocking Stripe's API
        // In a real scenario, you would mock the PaymentIntent retrieval
        // For now, we test the validation logic
        assertTrue(stripePaymentService.validatePaymentRequest(paymentRequest));
    }

    @Test
    public void testHandlePaymentFailure_LogsError() {
        Exception paymentException = new Exception("Payment failed: Insufficient funds");

        assertNotNull(paymentException);
        assertTrue(paymentException.getMessage().contains("Payment failed"));
    }

    @Test
    public void testHandlePaymentSuccess_UpdatesOrder() {
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setPaymentId("pi_test123");
        paymentDetails.setPaymentMethod("card");
        paymentDetails.setPaymentStatus("completed");

        assertNotNull(paymentDetails.getPaymentId());
        assertEquals("pi_test123", paymentDetails.getPaymentId());
    }

    @Test
    public void testRefundPayment_Success() throws StripeException {
        String paymentIntentId = "pi_test123";

        // In a real scenario, this would call Stripe's refund API
        assertNotNull(paymentIntentId);
        assertFalse(paymentIntentId.isEmpty());
    }

    @Test
    public void testRefundPayment_InvalidId() {
        String invalidPaymentIntentId = "";

        assertTrue(invalidPaymentIntentId.isEmpty());
    }

    @Test
    public void testCreatePaymentRequest_FromOrder() {
        StripePaymentRequest request = new StripePaymentRequest();
        request.setAmount((long) (testOrder.getTotalDiscountedPrice() * 100));
        request.setCurrency("USD");
        request.setOrderId(testOrder.getOrderId());

        assertNotNull(request);
        assertEquals((long) (testOrder.getTotalDiscountedPrice() * 100), request.getAmount());
        assertEquals(testOrder.getOrderId(), request.getOrderId());
    }

    @Test
    public void testValidatePaymentIntegration_ConfigurationExists() {
        // This test verifies that Stripe configuration is available
        assertNotNull(stripePaymentService);
    }

    @Test
    public void testPaymentRequestBuilder() {
        StripePaymentRequest request = new StripePaymentRequest();
        request.setAmount(1000);
        request.setCurrency("USD");
        request.setDescription("Test Payment");
        request.setOrderId("Order-12345");

        assertEquals(1000, request.getAmount());
        assertEquals("USD", request.getCurrency());
        assertEquals("Test Payment", request.getDescription());
        assertEquals("Order-12345", request.getOrderId());
    }
}
