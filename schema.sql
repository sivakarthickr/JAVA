-- =======================================================
-- KEC R2024 Evaluation System Database Schema
-- Kongu Engineering College (Autonomous)
-- =======================================================

CREATE DATABASE IF NOT EXISTS kec_evaluation;
USE kec_evaluation;

-- 1. Students Table
CREATE TABLE IF NOT EXISTS students (
    roll_no VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    dept VARCHAR(20) NOT NULL,
    sem INT NOT NULL
);

-- 2. Course Enrollments & Marks Table
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(20) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    credits INT NOT NULL,
    cia1 DOUBLE DEFAULT 0.0,
    cia2 DOUBLE DEFAULT 0.0,
    cia_other DOUBLE DEFAULT 0.0,
    ese DOUBLE DEFAULT 0.0,
    FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE
);

-- Sample Data (Optional Test Inserts)
INSERT INTO students (roll_no, name, dept, sem) 
VALUES ('24CSR001', 'Siva Karthick R', 'CSE', 1)
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO enrollments (roll_no, course_code, course_name, credits, cia1, cia2, cia_other, ese)
VALUES 
('24CSR001', '24CST11', 'Programming in C and Java', 4, 14.0, 13.5, 9.0, 52.0),
('24CSR001', '24MAT11', 'Matrices and Calculus', 4, 12.0, 14.0, 8.5, 48.0),
('24CSR001', '24PHT11', 'Engineering Physics', 3, 15.0, 15.0, 10.0, 55.0),
('24CSR001', '24EET11', 'Basics of Electrical & Electronics', 3, 13.0, 12.0, 8.0, 45.0);
