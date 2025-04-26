# Ecommerce Website - Backend

This is a backend system for a microservices-based ecommerce platform 
built using Spring Boot. The project is designed with a scalable and 
modular architecture, where each service is responsible for a specific functionality 
such as user management, product catalog, cart operations, order processing, 
payment handling, and notification delivery.



---

## Table of Contents
- [Services Overview](#services-overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Services Details](#services-details)
    - [User Service](#user-service)
    - [Product Service](#product-service)
    - [Cart Service](#cart-service)
    - [Order Service](#order-service)
    - [Payment Service](#payment-service)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Setup Instructions](#setup-instructions)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Postman Collection](#postman-collection)
- [Future Enhancements](#-future-enhancements)

---

## Services Overview

The platform consists of the following core services:

1. **User Service** - Manages user authentication and registration.
2. **Product Service** - Handles product catalog and category management.
3. **Cart Service** - Allows users to add/remove products in their shopping cart.
4. **Order Service** - Processes orders and communicates with the payment service.
5. **Payment Service** - Manages payment processing and order payment statuses.
6. **Notification Service** - Sends user notifications by consuming events from Kafka.

---

## Tech Stack

**Backend Framework:**
- Spring Boot (Java)
- Spring Security (JWT Authentication & Role-Based Access)
- Spring Data JPA (ORM)
- Spring WebFlux (Used WebClient for non-blocking service-to-service calls)

**Database:**
- MySQL (Separate DB for each microservice)
- Redis (Caching product data in Product Service)

**Inter-Service Communication:**
- REST APIs (Synchronous)
- WebClient (Non-blocking service-to-service calls)
- Kafka (Asynchronous messaging)

**Authentication & Authorization:**
- JWT (JSON Web Tokens)
- BCrypt (Secure password hashing)

**Containerization:**
- Docker
- Docker Compose
- `.env` support for environment configuration

**Dev & Build Tools:**
- IntelliJ IDEA
- Maven (Build automation)
- Postman / Swagger (API Testing and Documentation)

**Testing:**
- JUnit
- Mockito

---

## Architecture

The system follows a microservices architecture with each service responsible for specific functionality:
- Each service has its own database.
- Communication between services is enabled using REST APIs and asynchronous messaging via Kafka.

### Architecture Diagram
The following diagram illustrates the architecture of the ecommerce platform, including the tech stack for each component:

```plaintext
                               +-------------------------------------------------------------+
                               |                              Kafka                          |
                               |             (Central Event Bus for All Services)            |
                               +-----------------+---------+---------+-----------------------+
                                       ^          |           ^            ^         \ 
                                       |          |           |             \         \                    
            +-------------------+      |          |           |              \         \      +----------------------+
            |   User Service    |      |          |           |               \         \     | Notification Service |
            |-------------------|      |          |           |                \          --> |----------------------|
       +--->| - Spring Boot     |      |          |           |                 \             | - Spring Boot        |
       |    | - MySQL (user_db) |      |          |           |                  \            | - Kafka Consumer     |
       |    | - JWT, Roles      |      |          |           |                   \           +----------------------+
       |    +-------------------+      |          |           |                    \                                      
       |              ^           (Events)     (Events)     (Events)---------+    (Events)---------------+                                   
       |  (REST APIs) |             /             |                          |                           |               
       |              |            /              v                          v                           |            
       |    +-------------------+ /     +-------------------+      +-------------------+       +-------------------+  
       |    | Product Service   |v      |  Cart Service     |      | Order Service     |       | Payment Service   |  
       |    |-------------------|       |-------------------|      |-------------------|       |-------------------|  
       |    | - Spring Boot     |       | - Spring Boot     |      | - Spring Boot     |       | - Spring Boot     |  
       |    | - MySQL (prod_db) |<------| - MySQL (cart_db) |<-----| - MySQL (order_db)|<----->| - MySQL (pay_db)  |  
       |    | - Redis (Cache)   |       |                   |      | - Kafka Consumer  |       | - Kafka Producer  |  
       |    | - Kafka (Prod/    |       | - Kafka Consumer  |      |   (and Producer)  |       |                   | 
       |    |   Consumer)       |       |                   |      |                   |       |                   |
       |    +-------------------+       +-------------------+      +-------------------+       +-------------------+  
       |              ^                                                  |      |
       |              |_____________________(REST APIs)__________________|      |                                                       
       |                                                                        |
       |__(REST APIs)___________________________________________________________|                                               
                                                                                                                                                                                                        
```

---

## Services Details

### 1. User Service
- **Responsibilities**:
    - User registration and login.
    - JWT-based authentication.
    - Role-based access control (e.g., customer, admin).
- **Technologies**:
    - Spring Security for authentication and authorization.
    - BCrypt for secure password hashing.

### 2. Product Service
- **Responsibilities**:
    - Manage product catalog.
    - Handle product categories.
    - Provide product details, stock availability, and search functionalities.
- **Technologies**:
    - Spring Data JPA for database operations.
    - Redis for caching product data.

### 3. Cart Service
- **Responsibilities**:
    - Add/remove products to/from the cart.
    - Get the user's cart details.
    - Merge guest and user cart data.
- **Technologies**:
    - Spring WebFlux for reactive programming.
    - Integration with Product Service for stock validation.

### 4. Order Service
- **Responsibilities**:
    - Place and manage orders.
    - Reserve stock during order placement.
    - Handle order cancellations and status updates.
- **Technologies**:
    - Kafka for event-driven order processing.
    - Scheduled jobs to cancel expired orders.

### 5. Payment Service
- **Responsibilities**:
    - Generate payment links for orders.
    - Process payments and update order statuses.
    - Handle payment callbacks and retries.
- **Technologies**:
    - Kafka for payment events.
    - UUID for generating unique payment IDs.

### 6. Notification Service
- **Responsibilities**:
  - Simulate sending notifications (like email/SMS) on events such as payment success or failure.
  - Consume Kafka events from other services and log notification messages.
- **Technologies**:
  - Kafka Consumer for receiving events.
  - Simulated notification using Java logging (no live email/SMS).
  - Spring Boot for service orchestration.

---

## Getting Started

### Prerequisites

Ensure the following software is installed on your system:
- **Docker** and **Docker Compose**: To containerize and orchestrate services.
- **MySQL**: Each service uses its own MySQL database.

---

### Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/DV-00/ecommerce-website.git
   cd ecommerce-website
   ```

2. **Update `.env` File**:
    - The `.env` file is already included in the repository.
    - Open the `.env` file and update the following values with your MySQL credentials:
      ```plaintext
      MYSQL_DB_USER=your_mysql_username
      MYSQL_DB_PASSWORD=your_mysql_password
      ```

---

## Running the Application

1. **Create JAR Files**:
  - Build all services using Maven:
    ```bash
    mvn clean install
    ```
    
2. **Start the Application**:
    - Use Docker Compose to build and start all services:
      ```bash
      docker-compose up --build
      ```

2. **Access Services**:
    - Once the containers are running, access the services via their respective ports as defined in the `docker-compose.yml` file.

3. **Stop the Application**:
    - To stop all running containers, use:
      ```bash
      docker-compose down
      ```

---

## API Endpoints

### User Service
- `POST /users/register` - Register a new user.
- `POST /users/login` - Login a user and return a JWT.
- `GET /users/validate` - Validate a token and return user details.

### Product Service
- `GET /products/{id}` - Get product details by ID.
- `GET /products` - Get all products (supports pagination).
- `POST /products` - Create a new product (requires authorization).
- `PATCH /products/{id}/price` - Update product price (requires authorization).
- `PATCH /products/{id}/image` - Update product image (requires authorization).
- `PATCH /products/{id}/quantity` - Update product quantity (requires authorization).
- `GET /products/{id}/stock` - Get stock for a product ID.
- `DELETE /products/{id}` - Delete a product (requires authorization).

### Cart Service
- `POST /api/cart/add` - Add a product to the cart.
- `GET /api/cart` - Get the user's cart details (supports user ID or session ID).
- `POST /api/cart/merge` - Merge a guest cart into a user cart.
- `DELETE /api/cart/remove/{cartItemId}` - Remove an item from the cart.
- `PUT /api/cart/update/{cartItemId}` - Update an item in the cart.
- `DELETE /api/cart/clear` - Clear the cart for a user.
- `GET /api/cart/count` - Get the total number of items in the cart.
- `GET /api/cart/total` - Get the cart's total price.

### Order Service
- `POST /api/orders` - Place a new order (requires authorization).
- `GET /api/orders/{orderId}` - Get details of an order by ID.
- `GET /api/orders/user/{userId}` - Get all orders for a user.
- `PUT /api/orders/cancel/{orderId}` - Cancel an order.
- `GET /api/orders/{orderId}/status` - Get the status of an order.
- `GET /api/orders/event/{orderId}` - Get order event details.

### Payment Service
- `POST /payments/create-payment-link` - Generate a payment link for an order.
- `POST /payments/process/{paymentId}` - Process a payment (payment link generated when placing an order.).

---

## Postman Collection

- **Postman Collection**: [Download Postman Collection](https://app.getpostman.com/join-team?invite_code=62d5e6d8aa03bd1d480bb1a61c725ee1ef6550d0a61fa95568bfdab5c866ab0f&target_code=3a4bdd2af1a88bc43ced97dd691a9c68)

---

## Future Enhancements
- Add real payment gateway (e.g., Razorpay/Stripe).
- Integrate email/SMS notification system for order updates.
- Build a React or Angular frontend for better user experience.
- Add a rating and review system for products.
- Introduce Elasticsearch for advanced product searching.
- Add multi-currency support for international customers.
- Implement AI-based product recommendations.
- Build a mobile app using Flutter or React Native.
