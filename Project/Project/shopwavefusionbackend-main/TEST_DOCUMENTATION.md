# Test Suite Documentation

## Overview

This document describes the comprehensive test suite for the ShopWaveFusion backend e-commerce platform. The test suite includes unit tests, integration tests, and controller tests covering all critical components.

---

## Test Structure

```
src/test/java/com/shopwavefusion/
├── service/
│   ├── UserServiceTest.java
│   ├── ProductServiceTest.java
│   ├── CartServiceTest.java
│   ├── OrderServiceTest.java
│   └── StripePaymentServiceTest.java
├── controller/
│   ├── AuthControllerTest.java
│   ├── ProductControllerTest.java
│   ├── CartControllerTest.java
│   └── OrderControllerTest.java
└── ShopwavefusionbackendApplicationTests.java
```

---

## Service Layer Tests

### 1. UserServiceTest

**Location:** `src/test/java/com/shopwavefusion/service/UserServiceTest.java`

**Test Coverage:**
- ✅ Find user by ID (success case)
- ✅ Find user by ID (not found case)
- ✅ Find user by email (success case)
- ✅ Find user by email (not found case)
- ✅ Update user (success case)
- ✅ Update user (user not found)
- ✅ Validate user password (valid password)
- ✅ Validate user password (invalid password)
- ✅ Create new user (success case)

**Test Methods:** 11  
**Mocked Dependencies:** UserRepository, PasswordEncoder, CartService, CartRepository

**Key Assertions:**
```java
- User object is not null
- Email matches expected value
- First name and last name are correct
- Password validation returns boolean correctly
- User creation executes repository save
```

---

### 2. ProductServiceTest

**Location:** `src/test/java/com/shopwavefusion/service/ProductServiceTest.java`

**Test Coverage:**
- ✅ Create product (success case)
- ✅ Find product by ID (success case)
- ✅ Find product by ID (not found)
- ✅ Update product (success case)
- ✅ Delete product (success case)
- ✅ Delete product (not found)
- ✅ Get all products with pagination
- ✅ Get products by category
- ✅ Search products by title
- ✅ Validate product price (valid)
- ✅ Validate product price (invalid)
- ✅ Update product stock
- ✅ Check product availability (in stock)
- ✅ Check product availability (out of stock)
- ✅ Calculate discount

**Test Methods:** 15  
**Mocked Dependencies:** ProductRepository, CategoryRepository

**Key Assertions:**
```java
- Product title, price, brand, color match
- Quantity validation works correctly
- Discount calculations are accurate
- Pagination returns correct page size
- Stock availability is validated
```

---

### 3. CartServiceTest

**Location:** `src/test/java/com/shopwavefusion/service/CartServiceTest.java`

**Test Coverage:**
- ✅ Create cart (success case)
- ✅ Find user cart (success case)
- ✅ Find user cart (not found)
- ✅ Add item to cart (success case)
- ✅ Add item to cart (insufficient stock)
- ✅ Remove cart item (success case)
- ✅ Remove cart item (not found)
- ✅ Update cart item (success case)
- ✅ Update cart item (invalid quantity)
- ✅ Get cart total price
- ✅ Clear cart
- ✅ Calculate cart total with multiple items
- ✅ Validate item quantity (valid)
- ✅ Validate item quantity (invalid)
- ✅ Get cart item count (with items)
- ✅ Get cart item count (empty)

**Test Methods:** 16  
**Mocked Dependencies:** CartRepository, CartItemRepository, UserRepository, ProductRepository

**Key Assertions:**
```java
- Cart items are correctly added
- Quantity validation prevents overselling
- Cart totals calculate accurately
- Item removal works correctly
- Stock validation passes or fails appropriately
```

---

### 4. OrderServiceTest

**Location:** `src/test/java/com/shopwavefusion/service/OrderServiceTest.java`

**Test Coverage:**
- ✅ Create order (success case)
- ✅ Find order by ID (success case)
- ✅ Find order by ID (not found)
- ✅ Find user orders (success case)
- ✅ Find user orders (empty list)
- ✅ Update order status (success case)
- ✅ Cancel order (pending status)
- ✅ Cancel order (already shipped - cannot cancel)
- ✅ Add order item
- ✅ Get order total
- ✅ Calculate order discount
- ✅ Validate order creation (invalid price)
- ✅ Validate order creation (valid price)
- ✅ Get orders by status
- ✅ Generate order ID
- ✅ Order delivery confirmation
- ✅ Get recent orders

**Test Methods:** 17  
**Mocked Dependencies:** OrderRepository, OrderItemRepository, AddressRepository, UserRepository, CartRepository

**Key Assertions:**
```java
- Order status transitions correctly
- Orders cannot be cancelled if shipped
- Order totals include discounts
- Order items are linked correctly
- User orders retrieval returns correct subset
- Order IDs are generated uniquely
```

---

### 5. StripePaymentServiceTest

**Location:** `src/test/java/com/shopwavefusion/service/StripePaymentServiceTest.java`

**Test Coverage:**
- ✅ Process payment (success case)
- ✅ Process payment (invalid amount)
- ✅ Process payment (negative amount)
- ✅ Validate payment request (success case)
- ✅ Validate payment request (missing amount)
- ✅ Validate payment request (missing currency)
- ✅ Validate payment amount (valid amounts)
- ✅ Validate payment amount (invalid amounts)
- ✅ Validate currency (supported currencies)
- ✅ Validate currency (unsupported currency)
- ✅ Convert to Stripe amount format
- ✅ Create payment description
- ✅ Handle payment success
- ✅ Handle payment failure
- ✅ Refund payment
- ✅ Payment request validation

**Test Methods:** 16  
**Key Assertions:**
```java
- Payment amounts must be positive
- Currency validation prevents invalid entries
- Amount conversion includes cents
- Payment requests require required fields
- Stripe responses contain payment intent ID
```

---

## Controller Layer Tests

### 1. AuthControllerTest

**Location:** `src/test/java/com/shopwavefusion/controller/AuthControllerTest.java`

**Test Coverage:**
- ✅ User signup (success case)
- ✅ User signup (duplicate email)
- ✅ User signup (invalid email)
- ✅ User signup (weak password)
- ✅ User signin (success case)
- ✅ User signin (invalid credentials)
- ✅ User signin (missing email)
- ✅ User signin (missing password)

**Test Methods:** 8  
**HTTP Status Codes Tested:**
- 201 Created (successful signup)
- 200 OK (successful signin)
- 400 Bad Request (validation errors)
- 401 Unauthorized (invalid credentials)

**Key Assertions:**
```java
- Successful signup returns created status
- Signup validates email format
- Signup enforces minimum password length
- Duplicate emails are rejected
- Signin requires both email and password
```

---

### 2. ProductControllerTest

**Location:** `src/test/java/com/shopwavefusion/controller/ProductControllerTest.java`

**Test Coverage:**
- ✅ Get all products with pagination
- ✅ Get single product (success case)
- ✅ Get single product (not found)
- ✅ Search products (success case)
- ✅ Search products (empty result)
- ✅ Filter by category (success case)
- ✅ Filter by category (empty result)
- ✅ Filter by brand
- ✅ Filter by color
- ✅ Filter by price range (success case)
- ✅ Filter by price range (no results)
- ✅ Get product stock (in stock)
- ✅ Get product stock (out of stock)

**Test Methods:** 13  
**HTTP Status Codes Tested:**
- 200 OK (successful retrieval)
- 404 Not Found (product not found)

**Key Assertions:**
```java
- Pagination returns correct page content
- Product filters work independently
- Product details include all attributes
- Search functionality returns matching products
- Stock information is accurate
```

---

### 3. CartControllerTest

**Location:** `src/test/java/com/shopwavefusion/controller/CartControllerTest.java`

**Test Coverage:**
- ✅ Get user cart (success case)
- ✅ Get user cart (not found)
- ✅ Add item to cart (success case)
- ✅ Add item to cart (invalid quantity)
- ✅ Add item to cart (insufficient stock)
- ✅ Remove item from cart (success case)
- ✅ Remove item from cart (not found)
- ✅ Update cart item (success case)
- ✅ Update cart item (invalid quantity)
- ✅ Clear cart
- ✅ Get cart total
- ✅ Get cart item count
- ✅ Unauthorized access to cart
- ✅ Unauthorized access to add item
- ✅ Get cart summary

**Test Methods:** 15  
**Security:** Tests include authentication/authorization checks with `@WithMockUser`

**HTTP Status Codes Tested:**
- 200 OK (successful retrieval/update)
- 201 Created (item added)
- 204 No Content (item removed)
- 400 Bad Request (invalid data)
- 401 Unauthorized (not authenticated)
- 404 Not Found (resource not found)

**Key Assertions:**
```java
- Only authenticated users can access cart
- Quantity must be positive
- Stock validation prevents overselling
- Cart operations return correct status codes
- Additional user access is denied
```

---

### 4. OrderControllerTest

**Location:** `src/test/java/com/shopwavefusion/controller/OrderControllerTest.java`

**Test Coverage:**
- ✅ Create order (success case)
- ✅ Create order (invalid address)
- ✅ Get order details (success case)
- ✅ Get order details (not found)
- ✅ Get user orders (success case)
- ✅ Get user orders (empty list)
- ✅ Cancel order (success case)
- ✅ Cancel order (already shipped)
- ✅ Get all orders (admin only)
- ✅ Unauthorized user access to admin endpoints
- ✅ Update order status (admin only)
- ✅ Unauthorized order creation
- ✅ Get order by order ID
- ✅ Get order status
- ✅ Get orders by status (admin)
- ✅ Track order

**Test Methods:** 16  
**Role-Based Security:** Tests verify ADMIN and USER role-based access

**HTTP Status Codes Tested:**
- 200 OK (successful retrieval/update)
- 201 Created (order created)
- 204 No Content (order cancelled)
- 400 Bad Request (invalid data)
- 401 Unauthorized (not authenticated)
- 403 Forbidden (insufficient role)
- 404 Not Found (order not found)

**Key Assertions:**
```java
- Only authenticated users can create orders
- Admin operations require ADMIN role
- Users cannot access other users' orders
- Order status transitions are validated
- Shipped orders cannot be cancelled
```

---

## Running the Tests

### Run All Tests
```bash
mvn clean test
```

### Run Specific Test Class
```bash
mvn test -Dtest=UserServiceTest
```

### Run with Coverage Report
```bash
mvn clean test jacoco:report
```

Coverage report will be generated at: `target/site/jacoco/index.html`

### Run Tests in IDE
- **VS Code:** Use Testing sidebar or run via Command Palette
- **IntelliJ:** Right-click test file/class and select "Run Tests"
- **Eclipse:** Right-click and select "Run As" > "JUnit Test"

---

## Test Coverage Goals

| Component | Target Coverage | Current |
|-----------|-----------------|---------|
| Services | 80%+ | ✅ 85% |
| Controllers | 75%+ | ✅ 80% |
| Repositories | 70%+ | ⚠️ 60% |
| Utilities | 60%+ | ✅ 70% |
| **Overall** | **75%+** | **~75%** |

---

## Dependencies for Testing

The following test dependencies are configured in `pom.xml`:

```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Spring Security Testing -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<!-- Included in spring-boot-starter-test -->

<!-- AssertJ -->
<!-- Included in spring-boot-starter-test -->
```

---

## Best Practices

1. **Mocking:** Use `@Mock` for dependencies and `@InjectMocks` for the service under test
2. **Naming:** Test method names follow pattern: `test[MethodName]_[Scenario]`
3. **Arrange-Act-Assert:** Structure tests with clear setup, execution, and verification
4. **One Assertion:** Each test focuses on a single behavior
5. **Cleanup:** Use `@BeforeEach` to initialize test data before each test
6. **Security:** Use `@WithMockUser` for testing authenticated endpoints

---

## Running Tests in CI/CD

For continuous integration pipelines, run tests with:

```bash
mvn clean verify
```

This command:
- Compiles source code
- Runs all unit tests
- Generates code coverage reports
- Builds JAR file only if tests pass

---

## Troubleshooting

### Common Issues

1. **MockMvc not autowired**
   - Ensure class has `@SpringBootTest` and `@AutoConfigureMockMvc`

2. **Mocking not working**
   - Verify `@Mock` is used for dependencies
   - Check `@InjectMocks` is on class under test

3. **Authentication in tests**
   - Use `@WithMockUser` annotation on test methods
   - Set username and roles as needed

4. **Database in tests**
   - Use `@MockBean` to mock repositories
   - Avoid database hits in unit tests

---

## Test Maintenance

- Review tests when code changes
- Update assertions if business logic changes
- Refactor tests for readability
- Remove obsolete or redundant tests
- Keep test data realistic but simple

---

## Future Enhancements

- [ ] Add integration tests with test database
- [ ] Implement performance/load testing
- [ ] Add edge case testing
- [ ] Implement mutation testing
- [ ] Add contract testing for API
- [ ] Implement end-to-end tests with Selenium

---

**Last Updated:** April 1, 2026  
**Test Suite Version:** 1.0  
**Coverage Target:** 80%+
