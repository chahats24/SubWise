
CREATE DATABASE IF NOT EXISTS subwise;
USE subwise;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE subscriptions (
    subscription_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    plan_name VARCHAR(100),
    price DECIMAL(10, 2) NOT NULL,
    billing_cycle ENUM('MONTHLY', 'QUARTERLY', 'YEARLY') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('ACTIVE', 'EXPIRED', 'CANCELLED')
        NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_subscription_price CHECK (price >= 0),
    CONSTRAINT chk_subscription_dates CHECK (end_date >= start_date),

    CONSTRAINT fk_subscription_user
        FOREIGN KEY (user_id) REFERENCES users(user_id),

    CONSTRAINT fk_subscription_category
        FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

CREATE TABLE payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    subscription_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'SUCCESS', 'FAILED')
        NOT NULL DEFAULT 'PENDING',

    CONSTRAINT chk_payment_amount CHECK (amount >= 0),

    CONSTRAINT fk_payment_user
        FOREIGN KEY (user_id) REFERENCES users(user_id),

    CONSTRAINT fk_payment_subscription
        FOREIGN KEY (subscription_id)
        REFERENCES subscriptions(subscription_id)
);

INSERT INTO categories (category_name) VALUES
('OTT'),
('Music'),
('Software'),
('Cloud Storage'),
('Fitness'),
('Education'),
('Gaming');

-- Demo Administrator Account for College Presentation
-- Email: admin@subwise.com | Password: admin123 (BCrypt hash)
INSERT INTO users (name, email, password_hash, role)
VALUES ('Administrator', 'admin@subwise.com', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'ADMIN')
ON DUPLICATE KEY UPDATE role = 'ADMIN';

SHOW TABLES;
