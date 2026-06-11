CREATE DATABASE IF NOT EXISTS `order_management`;

USE `order_management`;

CREATE TABLE `orders` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `total_price` DECIMAL(10, 2) NOT NULL,
  `total_quantity` INT NOT NULL,
  `order_date` DATETIME NOT NULL
);

CREATE TABLE `order_items` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL,
  `product_id` INT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `price` DECIMAL(10, 2) NOT NULL,
  `quantity` INT NOT NULL,
  FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`)
);

CREATE TABLE `scenic` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `province` VARCHAR(255) DEFAULT NULL,
  `city` VARCHAR(255) DEFAULT NULL,
  `photo` VARCHAR(255) DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `score` DOUBLE DEFAULT NULL,
  `ticket_price` DECIMAL(10,2) DEFAULT NULL,
  `audit_status` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `tour_route` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(255) DEFAULT NULL,
  `intro` TEXT DEFAULT NULL,
  `days` INT DEFAULT NULL,
  `price` DOUBLE DEFAULT NULL,
  `distance` DOUBLE DEFAULT NULL,
  `duration` VARCHAR(50) DEFAULT NULL,
  `service_include` TEXT DEFAULT NULL,
  `service_exclude` TEXT DEFAULT NULL,
  `images` VARCHAR(255) DEFAULT NULL,
  `notice` TEXT DEFAULT NULL,
  `merchant_id` BIGINT DEFAULT NULL,
  `audit_status` INT DEFAULT NULL,
  `create_time` DATETIME DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `route_scenic` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `route_id` BIGINT NOT NULL,
  `scenic_id` BIGINT NOT NULL,
  `day_number` INT DEFAULT NULL,
  `sort_order` INT DEFAULT NULL,
  FOREIGN KEY (`route_id`) REFERENCES `tour_route`(`id`),
  FOREIGN KEY (`scenic_id`) REFERENCES `scenic`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cart` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(255) NOT NULL,
  `created_time` DATETIME DEFAULT NULL,
  `updated_time` DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `cart_item` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `cart_id` BIGINT NOT NULL,
  `scenic_id` BIGINT NOT NULL,
  `scenic_name` VARCHAR(255) DEFAULT NULL,
  `ticket_price` DECIMAL(10,2) DEFAULT NULL,
  `quantity` INT DEFAULT 1,
  `route_id` BIGINT DEFAULT NULL,
  FOREIGN KEY (`cart_id`) REFERENCES `cart`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;