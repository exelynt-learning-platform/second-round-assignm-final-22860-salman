# Stripe Integration - Quick Start

## What's Been Implemented

✅ **Stripe Payment Integration in Test Mode**
✅ **PaymentIntent API** for secure transactions
✅ **Order & OrderItem entities** for tracking purchases
✅ **REST API endpoints** for payment operations
✅ **Configuration management** for Stripe keys

## Quick Setup

### 1. Prerequisites
- MySQL database running
- Database: `full-stack-ecommerce`
- Table structure generated from Order.java and OrderItem.java entities

### 2. Build & Run

```bash
# From project root
mvn clean install
mvn spring-boot:run
```

### 3. Create a Test Order

Insert into database:
```sql
INSERT INTO orders (order_tracking_number, total_price, total_quantity, status, customer_email, date_created)
VALUES ('TEST-001', 50.00, 1, 'pending', 'test@example.com', NOW());
```

### 4. Test Payment Flow

**A. Create Payment Intent:**
```bash
curl -X POST http://localhost:8080/api/payments/create-payment-intent \
  -H "Content-Type: application/json" \
  -d '{"orderId": 1}'
```

Response will include `clientSecret` for frontend.

**B. Use Test Card:** `4242 4242 4242 4242` (Exp: 12/25, CVC: 123)

**C. Check Payment Status:**
```bash
curl http://localhost:8080/api/payments/payment-intent/{paymentIntentId}
```

**D. Update Order:**
```bash
curl -X POST http://localhost:8080/api/payments/update-order-status \
  -H "Content-Type: application/json" \
  -d '{"paymentIntentId": "{paymentIntentId}"}'
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/payments/create-payment-intent` | POST | Create a Stripe payment intent |
| `/api/payments/payment-intent/{id}` | GET | Get payment status |
| `/api/payments/update-order-status` | POST | Update order after payment |
| `/api/payments/cancel/{id}` | POST | Cancel payment intent |
| `/api/payments/health` | GET | Health check |

## Stripe Test Mode Keys

```
Publishable: pk_test_51THHhIAhGQ68O3lygU8lZxhUtvMLV7kEUvkbuHEcqSQPEYKG8Vw71dWCVIqPChkqAPbLAkZWDhIbtjhrFoM0fqJ500pgs12eAU
Secret: sk_test_51THHhIAhGQ68O3ly65j8qClt4fN1efoqFCnxT0dy1rgup2BUGnko75XMnIItuZic4CXsGNNnZenNmadtOGzoK0Av00PyQagxkr
```

## Files Created/Modified

**New Files:**
- `src/main/java/com/luv2code/ecommerce/entity/Order.java`
- `src/main/java/com/luv2code/ecommerce/entity/OrderItem.java`
- `src/main/java/com/luv2code/ecommerce/dao/OrderRepository.java`
- `src/main/java/com/luv2code/ecommerce/dao/OrderItemRepository.java`
- `src/main/java/com/luv2code/ecommerce/config/StripeProperties.java`
- `src/main/java/com/luv2code/ecommerce/config/StripeConfiguration.java`
- `src/main/java/com/luv2code/ecommerce/service/PaymentService.java`
- `src/main/java/com/luv2code/ecommerce/rest/PaymentController.java`

**Modified Files:**
- `pom.xml` (added Stripe dependency)
- `src/main/resources/application.properties` (added Stripe keys)

## Next Steps

1. Create an order creation endpoint (POST `/api/orders`)
2. Add shopping cart functionality
3. Implement frontend using Stripe.js
4. Add webhook handling for payment confirmations
5. Add email notifications
6. Implement refund functionality

See `STRIPE_INTEGRATION_GUIDE.md` for detailed documentation.
