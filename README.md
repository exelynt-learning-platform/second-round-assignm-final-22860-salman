# 🛒 E-Commerce Backend System (Spring Boot)

## 📌 Overview

This project is a backend implementation of an **E-commerce platform** built using **Spring Boot**. It provides RESTful APIs for user authentication, product management, cart handling, order processing, and payment integration.

---

## 🚀 Features

### 🔐 User Authentication & Authorization

* User Registration & Login
* JWT-based Authentication
* Role-based Authorization (User/Admin)
* Secure APIs using Spring Security

---

### 📦 Product Management

* Create, Read, Update, Delete (CRUD) Products
* Product attributes:

  * Name
  * Description
  * Price
  * Stock Quantity
  * Image URL

---

### 🛒 Cart Management

* Add items to cart
* Update cart items
* Remove items from cart
* Each user can manage only their own cart

---

### 📑 Order Management

* Create orders from cart
* View order history
* Order includes:

  * User details
  * Product list
  * Total price
  * Shipping details
  * Payment status

---

### 💳 Payment Integration

* Integrated with **Stripe / PayPal**
* Handle:

  * Payment success
  * Payment failure
* Automatically update order status

---

### 🗄️ Database Integration

* Spring Data JPA for persistence
* Entity relationships:

  * One-to-Many (User → Orders)
  * Many-to-Many (Orders ↔ Products)
  * Cart linked with User & Products

---

### ⚠️ Error Handling & Validation

* Input validation (price, stock, etc.)
* Standard HTTP responses:

  * `200 OK`
  * `201 Created`
  * `400 Bad Request`
  * `401 Unauthorized`
  * `404 Not Found`

---

## 🛠️ Tech Stack

* Java
* Spring Boot
* Spring Security
* JWT (JSON Web Token)
* Spring Data JPA
* Hibernate
* MySQL / H2 Database
* Stripe / PayPal API

---

## 📂 Project Structure

```
src/main/java/com/shopwavefusion/
│
├── controller        # REST Controllers
├── service           # Business Logic
├── repository        # JPA Repositories
├── model             # Entity Classes
├── config            # Security & JWT Config
├── exception         # Custom Exceptions
├── request/response  # DTOs
```

---

## ⚙️ Setup Instructions

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/your-repo-url.git
cd project-folder
```

### 2️⃣ Configure Database

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce
spring.datasource.username=root
spring.datasource.password=yourpassword
```

### 3️⃣ Run the Application

```bash
mvn spring-boot:run
```

---

## 🔑 API Endpoints (Sample)

### Auth

* `POST /auth/register`
* `POST /auth/login`

### Products

* `GET /products`
* `POST /products`
* `PUT /products/{id}`
* `DELETE /products/{id}`

### Cart

* `POST /cart/add`
* `PUT /cart/update`
* `DELETE /cart/remove`

### Orders

* `POST /orders`
* `GET /orders/{id}`

---

## 🧪 Testing

* Unit and Integration tests included
* Run:

```bash
mvn test
```

---

## 📈 Future Enhancements

* Wishlist feature
* Product reviews & ratings
* Advanced search & filters
* Admin dashboard

---

## 👨‍💻 Author

* Developed as part of backend assignment project

---

## 📜 License

This project is for educational purposes.
