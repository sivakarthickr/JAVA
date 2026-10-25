# KEC R2024 Evaluation System

Java-based Academic Evaluation and Grade Sheet Generation System following the Autonomous Regulations 2024 (R2024) of **Kongu Engineering College (KEC)**.

## Features
- **Student Registration**: Register students with Roll Number, Name, Department, and Semester.
- **Incremental Mark Entry**: Record and update continuous assessment marks across all evaluation phases:
  - `CIA1`: Continuous Internal Assessment 1
  - `CIA2`: Continuous Internal Assessment 2
  - `CIA_OTHER`: Assignments, Seminars, Quizzes
  - `ESE`: End Semester Examination
- **Grade Sheet Generation**:
  - Automatically calculates CIA Total (scaled to max 40) and ESE Total (scaled to max 60).
  - Calculates Total Course Marks (out of 100).
  - Assigns Letter Grades (`O`, `A+`, `A`, `B+`, `B`, `U`) and Grade Points (10 down to 6, 0 for Re-appearance).
  - Computes Semester Grade Point Average (**SGPA**).
- **Dual Persistence Architecture**:
  - Connects to **MySQL** via JDBC when database is available.
  - Automatically falls back to **In-Memory** storage if MySQL is not configured, allowing zero-setup immediate execution.

## Project Structure
- `MainApp.java`: Interactive console application menu for registration, mark entry, and report generation.
- `AcademicDAO.java`: Data Access Object implementing student records, mark calculations, and report formatting.
- `schema.sql`: MySQL database schema for `students` and `enrollments` tables.

## How to Run

### 1. Compile the code:
```bash
javac AcademicDAO.java MainApp.java
```

### 2. Run the application:
```bash
java MainApp
```
