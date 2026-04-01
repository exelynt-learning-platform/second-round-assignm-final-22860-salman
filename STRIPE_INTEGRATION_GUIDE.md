# Stripe Payment Integration - Testing Guide

## Overview
This e-commerce project now has Stripe payment integration in TEST MODE. The implementation uses:
- **Stripe PaymentIntent API** (recommended approach)
- **REST endpoints** for payment operations
- **MySQL database** to track orders and payments

## Setup Complete ✓

### 1. Dependencies Added
- Stripe Java SDK (v20.127.0)
- Spring Boot Web Starter
- Spring Boot Configuration Processor

### 2. Database Schema Created
#### New Tables:
- **orders** - Stores order information
  - id, order_tracking_number, total_price, total_quantity
  - status (pending/processing/completed)
  - stripe_payment_intent_id
  - billing_address, shipping_address
  - customer_email, date_created

- **order_item** - Stores items in each order
  - id, product_id, quantity, unit_price
  - image_url, order_id (foreign key)

### 3. Configuration
**Stripe Keys (Test Mode):**
```
stripe.api.secret-key=sk_test_51THHhIAhGQ68O3ly65j8qClt4fN1efoqFCnxT0dy1rgup2BUGnko75XMnIItuZic4CXsGNNnZenNmadtOGzoK0Av00PyQagxkr
stripe.api.publishable-key=pk_test_51THHhIAhGQ68O3lygU8lZxhUtvMLV7kEUvkbuHEcqSQPEYKG8Vw71dWCVIqPChkqAPbLAkZWDhIbtjhrFoM0fqJ500pgs12eAU
```

---

## Available REST Endpoints

### 1. Create Payment Intent
**POST** `/api/payments/create-payment-intent`

**Request Body:**
```json
{
  "orderId": 1
}
```

**Response (Success):**
```json
{
  "clientSecret": "pi_..._secret_...",
  "paymentIntentId": "pi_..."
}
```

**Usage:** Call this after creating an order. The `clientSecret` is used by frontend to confirm payment.

---

### 2. Get Payment Intent Status
**GET** `/api/payments/payment-intent/{paymentIntentId}`

**Response:**
```json
{
  "id": "pi_...",
  "status": "succeeded",
  "amount": 5000,
  "currency": "usd"
}
```

**Possible Statuses:**
- `requires_payment_method` - Waiting for payment method
- `requires_confirmation` - Waiting for confirmation
- `processing` - Payment is processing
- `requires_action` - Requires additional action (3D Secure, etc.)
- `succeeded` - Payment successful
- `canceled` - Payment was canceled

---

### 3. Update Order Status
**POST** `/api/payments/update-order-status`

**Request Body:**
```json
{
  "paymentIntentId": "pi_..."
}
```

**Response:**
```json
{
  "success": true,
  "message": "Order updated successfully"
}
```

---

### 4. Cancel Payment
**POST** `/api/payments/cancel/{paymentIntentId}`

**Response:**
```json
{
  "id": "pi_...",
  "status": "canceled",
  "message": "Payment canceled successfully"
}
```

---

### 5. Health Check
**GET** `/api/payments/health`

**Response:**
```json
{
  "status": "OK"
}
```

---

## Step-by-Step Testing Instructions

### Prerequisites
1. Start MySQL database
2. Create database: `full-stack-ecommerce`
3. Create tables using the Order and OrderItem entities
4. Have some products in the database

### Test Scenario: Complete Payment Flow

#### Step 1: Create an Order (via custom endpoint - to be created)
First, you need to create an order. For testing, insert directly into database or create a dedicated endpoint.

**SQL Example:**
```sql
INSERT INTO orders (order_tracking_number, total_price, total_quantity, status, customer_email, date_created)
VALUES ('ORD-001', 50.00, 2, 'pending', 'customer@example.com', NOW());
```

#### Step 2: Create Payment Intent
```bash
curl -X POST http://localhost:8080/api/payments/create-payment-intent \
  -H "Content-Type: application/json" \
  -d '{"orderId": 1}'
```

**Expected Response:**
```json
{
  "clientSecret": "pi_3OHrVfAhGQ68O3ly1234_secret_vvvv",
  "paymentIntentId": "pi_3OHrVfAhGQ68O3ly1234"
}
```

#### Step 3: Complete Payment in Frontend (or Stripe Dashboard)
**Option A - Stripe Dashboard Testing:**
Go to: https://dashboard.stripe.com/test/payments

**Option B - Frontend Integration:**
```javascript
// Use the clientSecret from Step 2
const confirmPayment = await stripe.confirmCardPayment(clientSecret, {
  payment_method: {
    card: cardElement,
    billing_details: { name: "Test Customer" }
  }
});
```

#### Step 4: Get Payment Status
```bash
curl http://localhost:8080/api/payments/payment-intent/pi_3OHrVfAhGQ68O3ly1234
```

**Response:**
```json
{
  "id": "pi_3OHrVfAhGQ68O3ly1234",
  "status": "succeeded",
  "amount": 5000,
  "currency": "usd"
}
```

#### Step 5: Update Order Status
```bash
curl -X POST http://localhost:8080/api/payments/update-order-status \
  -H "Content-Type: application/json" \
  -d '{"paymentIntentId": "pi_3OHrVfAhGQ68O3ly1234"}'
```

---

## Stripe Test Cards

Use these cards in test mode (no real charges):

| Card Type | Number | Exp Date | CVC |
|-----------|--------|----------|-----|
| Visa | 4242 4242 4242 4242 | 12/25 | 123 |
| Mastercard | 5555 5555 5555 4444 | 12/25 | 123 |
| Amex | 3782 822463 10005 | 12/25 | 1234 |
| Decline | 4000 0000 0000 0002 | 12/25 | 123 |

---

## Testing Failed Payments

**Test Declined Card:**
```bash
curl -X POST http://localhost:8080/api/payments/create-payment-intent \
  -H "Content-Type: application/json" \
  -d '{"orderId": 2}'
```

Then use card: `4000 0000 0000 0002`

---

## Frontend Integration (React.js Example)

```javascript
import { loadStripe } from "@stripe/js";
import { CardElement, Elements, useStripe, useElements } from "@stripe/react-stripe-js";

const stripePromise = loadStripe("pk_test_51THHhIAhGQ68O3lygU8lZxhUtvMLV7kEUvkbuHEcqSQPEYKG8Vw71dWCVIqPChkqAPbLAkZWDhIbtjhrFoM0fqJ500pgs12eAU");

function CheckoutForm({ orderId }) {
  const stripe = useStripe();
  const elements = useElements();

  const handlePayment = async () => {
    // Step 1: Create payment intent
    const response = await fetch("/api/payments/create-payment-intent", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ orderId })
    });
    const { clientSecret } = await response.json();

    // Step 2: Confirm payment
    const result = await stripe.confirmCardPayment(clientSecret, {
      payment_method: {
        card: elements.getElement(CardElement),
        billing_details: { name: "Test Customer" }
      }
    });

    if (result.paymentIntent.status === "succeeded") {
      console.log("Payment successful!");
      // Step 3: Update order status
      await fetch("/api/payments/update-order-status", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ paymentIntentId: result.paymentIntent.id })
      });
    }
  };

  return (
    <div>
      <CardElement />
      <button onClick={handlePayment}>Pay</button>
    </div>
  );
}

export default function App() {
  return (
    <Elements stripe={stripePromise}>
      <CheckoutForm orderId={1} />
    </Elements>
  );
}
```

---

## Troubleshooting

### Issue: "Stripe API Key not set"
**Solution:** Verify `stripe.api.secret-key` is set in `application.properties`

### Issue: "Order not found"
**Solution:** Make sure order exists in database before creating payment intent

### Issue: CORS errors
**Solution:** The `@CrossOrigin` annotation in PaymentController is set to `http://localhost:4200`. Update if needed.

### Issue: Payment processed but order not updated
**Solution:** Run the `/update-order-status` endpoint manually or check order table for `stripe_payment_intent_id` match

---

## Next Steps

1. **Create Order Endpoint** - Add REST endpoint to create orders
2. **Add Cart Management** - Implement shopping cart functionality
3. **Webhook Handling** - Add server-side webhook verification
4. **Email Notifications** - Send confirmation emails after payment
5. **Refund Functionality** - Handle refunds if needed
6. **Advanced Security** - Implement 3D Secure verification
7. **Database Adjustments** - Fine-tune schema based on requirements

---

## API Endpoints Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/payments/create-payment-intent` | Create PaymentIntent |
| GET | `/api/payments/payment-intent/{id}` | Get payment status |
| POST | `/api/payments/update-order-status` | Update order after payment |
| POST | `/api/payments/cancel/{id}` | Cancel payment |
| GET | `/api/payments/config` | Get config info |
| GET | `/api/payments/health` | Health check |

---

## Security Notes

⚠️ **Important for Production:**
- Never store secret keys in code or version control
- Use environment variables or secure configuration management
- Implement rate limiting on payment endpoints
- Add authentication/authorization to payment endpoints
- Use HTTPS only
- Validate amounts and quantities server-side
- Implement idempotency keys for duplicate requests
- Handle Stripe webhooks for payment confirmations

---

## Resources

- [Stripe API Documentation](https://stripe.com/docs/api)
- [Stripe Java SDK](https://github.com/stripe/stripe-java)
- [PaymentIntent Details](https://stripe.com/docs/payments/payment-intents)
- [Stripe Test Mode](https://stripe.com/docs/testing)
