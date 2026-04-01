package com.shopwavefusion.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.shopwavefusion.request.StripePaymentRequest;
import com.shopwavefusion.response.StripePaymentResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

@Service
public class StripePaymentServiceImplementation implements StripePaymentService {

    @Override
    public StripePaymentResponse processPayment(StripePaymentRequest paymentRequest) throws Exception {
        try {
            // Create charge map
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", paymentRequest.getAmount()); // amount in cents
            chargeParams.put("currency", paymentRequest.getCurrency());
            chargeParams.put("source", paymentRequest.getToken()); // token from frontend

            // Create a charge
            Charge charge = Charge.create(chargeParams);

            // Create response
            StripePaymentResponse response = new StripePaymentResponse();
            response.setChargeId(charge.getId());
            response.setStatus(charge.getStatus());
            response.setAmount(charge.getAmount());
            response.setCurrency(charge.getCurrency());
            response.setSuccess(charge.getPaid());
            response.setMessage("Payment processed successfully");

            return response;

        } catch (StripeException e) {
            StripePaymentResponse response = new StripePaymentResponse();
            response.setSuccess(false);
            response.setMessage("Payment failed: " + e.getUserMessage());
            return response;
        }
    }

}
