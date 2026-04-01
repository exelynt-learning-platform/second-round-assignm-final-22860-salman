package com.shopwavefusion.service;

import com.shopwavefusion.request.StripePaymentRequest;
import com.shopwavefusion.response.StripePaymentResponse;

public interface StripePaymentService {
    
    StripePaymentResponse processPayment(StripePaymentRequest paymentRequest) throws Exception;
    
}
