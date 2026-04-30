-- Appointment Scheduler Database Schema
-- This file is for reference only. Spring Boot will auto-create tables.

CREATE DATABASE IF NOT EXISTS appointment_scheduler;
USE appointment_scheduler;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('USER', 'STAFF', 'ADMIN') NOT NULL DEFAULT 'USER',
    status ENUM('ACTIVE', 'INACTIVE', 'DEACTIVATED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- Appointments Table
CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'RESCHEDULED', 'NO_SHOW') NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_staff (staff_id),
    INDEX idx_date (date),
    INDEX idx_status (status)
);

-- Reviews Table
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    appointment_id BIGINT,
    organization_rating INT NOT NULL CHECK (organization_rating BETWEEN 1 AND 5),
    staff_rating INT NOT NULL CHECK (staff_rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_appointment (appointment_id)
);

-- Broadcasts Table (Phase 2)
CREATE TABLE IF NOT EXISTS broadcasts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    target_audience ENUM('ALL', 'USERS', 'STAFF') NOT NULL DEFAULT 'ALL',
    status ENUM('ACTIVE', 'ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- Insert Default Admin (generate your own BCrypt hash using PasswordHashGenerator)
-- Run: java -cp target/appointment-scheduler-1.0.0.jar com.scheduler.util.PasswordHashGenerator
INSERT INTO users (name, username, email, password_hash, role, status, created_at, updated_at)
VALUES ('System Admin', 'admin', 'admin@scheduler.com', 
        '$2a$10$mxTaHBzHOqqzXykJ.yQfMeGR8qRAg1zN2YUS9EQV.dBHqgXnnY6VS', 
        'ADMIN', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- Insert Sample Staff
INSERT INTO users (name, username, email, password_hash, role, status, created_at, updated_at)
VALUES ('John Staff', 'staff1', 'staff1@scheduler.com', 
        '$2a$10$Q9xp81NyuyUuNtWOdbJo2uqtRp.M2AbwNBhf4/mco78o7U0mt65.a', 
        'STAFF', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- Insert Sample User
INSERT INTO users (name, username, email, password_hash, role, status, created_at, updated_at)
VALUES ('Jane User', 'user1', 'user1@scheduler.com', 
        '$2a$10$.OAo8EzGcLfhFIhcuGMS3Oma9QNcv4W2.ItBKEAC/w.rQ5WeaDDUW', 
        'USER', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;
