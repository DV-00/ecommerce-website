-- Create Databases
CREATE DATABASE IF NOT EXISTS userservice_db;
CREATE DATABASE IF NOT EXISTS productservice_db;
CREATE DATABASE IF NOT EXISTS orderservice_db;
CREATE DATABASE IF NOT EXISTS cartservice_db;
CREATE DATABASE IF NOT EXISTS paymentservice_db;

-- Use `userservice_db` and create `users` table + sample data
USE userservice_db;

CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
    );

INSERT INTO users (username, email, password, role) VALUES
                                                        ('Dipak_Vanzara', 'dv@example.com', '$2a$12$3vHtMBd6aZwQZKFYSOKFmOoJMtS9f0e9gcaqJokVNDnJwgBp/1Ivu', 'ADMIN'),
                                                        ('Spl_Dv', 'spl@example.com', '$2a$12$jvJIOBNXNB2swNVWAq7A4eo0RBUHXwhrdOlLxgWHCVmFdBt0Hm8IC', 'ADMIN'),
                                                        ('Jane_Doe', 'jane@example.com', '$2a$12$qp8qmxbnQ6O4ZgCwv75HB.1ivFvIF8vLupvQ1pffx5eTLyv7G35aC', 'CUSTOMER'),
                                                        ('Daisy_Doe', 'daisy@example.com', '$2a$12$3TMo2hHvsVlQzZHWQd0L6.AvTLMhC8/7C9VHL2H1yX8zZHjFnJlY.', 'CUSTOMER');

-- Use `productservice_db` and create `category` & `product` tables + sample data
USE productservice_db;

CREATE TABLE IF NOT EXISTS category (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(255) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS product (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       title VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    image VARCHAR(255),
    category_id BIGINT,
    quantity INT NOT NULL DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES category(id)
    );

INSERT INTO category (name) VALUES
                                ('Electronics'),
                                ('Books'),
                                ('Clothing');

INSERT INTO product (title, price, description, image, category_id, quantity) VALUES
                                                                                  ('X2 Ultra', 175000.00, 'A high-end smartphone', 'smartphone.jpg', 1, 20),
                                                                                  ('Q2 Octa', 75000.00, 'A powerful gaming laptop', 'laptop.jpg', 1, 15),
                                                                                  ('Fiction Book', 19.99, 'A bestselling fiction book', 'book.jpg', 2, 100);

-- Use `orderservice_db` and create `orders` & `order_items` tables + sample data
USE orderservice_db;

CREATE TABLE IF NOT EXISTS orders (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      user_id BIGINT NOT NULL,
                                      status ENUM('PENDING', 'RESERVED', 'COMPLETED', 'FAILED', 'CANCELED', 'REFUNDED') NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES userservice_db.users(id)
    );

CREATE TABLE IF NOT EXISTS order_items (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           product_id BIGINT NOT NULL,
                                           quantity INT NOT NULL,
                                           price DECIMAL(10, 2) NOT NULL,
    order_id BIGINT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES productservice_db.product(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
    );

INSERT INTO orders (user_id, status, total_amount, expires_at) VALUES
                                                                   (3, 'PENDING', 175000.00, DATE_ADD(NOW(), INTERVAL 10 MINUTE)),
                                                                   (4, 'COMPLETED', 99.95, NULL);

INSERT INTO order_items (product_id, quantity, price, order_id) VALUES
                                                                    (1, 1, 175000.00, 1),
                                                                    (3, 5, 19.99, 2);

-- Use `cartservice_db` and create `cart_items` table + sample data
USE cartservice_db;

CREATE TABLE IF NOT EXISTS cart_items (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          product_id BIGINT NOT NULL,
                                          quantity INT NOT NULL,
                                          user_id BIGINT,
                                          session_id VARCHAR(255),
    FOREIGN KEY (product_id) REFERENCES productservice_db.product(id)
    );

INSERT INTO cart_items (product_id, quantity, user_id) VALUES
                                                           (1, 2, 3),
                                                           (3, 3, 4);

-- Use `paymentservice_db` and create `payments` table + sample data
USE paymentservice_db;

CREATE TABLE IF NOT EXISTS payments (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        payment_id VARCHAR(255) UNIQUE NOT NULL,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'CANCELED', 'REFUNDED') NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orderservice_db.orders(id),
    FOREIGN KEY (user_id) REFERENCES userservice_db.users(id)
    );

INSERT INTO payments (payment_id, order_id, user_id, amount, status) VALUES
                                                                         ('PAY12345', 1, 3, 175000.00, 'PENDING'),
                                                                         ('PAY67890', 2, 4, 99.95, 'SUCCESS');