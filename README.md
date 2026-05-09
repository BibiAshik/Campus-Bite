# CampusBite - College Canteen Pre-Order System

## 1. Project Overview
CampusBite is a beginner-friendly full-stack web application designed to reduce long queues inside college canteens. It allows students to browse the canteen menu, add food items to their cart, and place pre-orders to receive a unique pickup token. Canteen staff can log into an admin dashboard to manage the menu, track pending orders, and mark them as ready for pickup.

## 2. Features
**For Students (No account required):**
* Browse the food menu with clean, categorized filters.
* Add items to the cart, automatically saved via `localStorage`.
* Place an order by simply providing a Student Name and Roll Number.
* Receive a unique Token Number (e.g., A-047) to show at the counter.
* Real-time order tracking using the generated token.

**For Admin / Canteen Staff (Login required):**
* Secure dashboard to view total daily orders, pending orders, and revenue.
* Accept and update order statuses (`PENDING` -> `READY` -> `PICKED_UP`).
* Full Menu Management: Add new food items, update prices, or mark items as `SOLD OUT`.

## 3. Technologies Used
* **Frontend:** Vanilla HTML5, CSS3, and JavaScript (No external frameworks like React or Tailwind used to keep it beginner-friendly).
* **Backend:** Java 21, Spring Boot, Spring MVC, Spring Security.
* **Database & ORM:** MySQL, Spring Data JPA (Hibernate).
* **Build Tool:** Maven.


## 4.How to Run the Project

1. Clone or download the repository.
2. Open the project in IntelliJ IDEA, Eclipse, or VS Code.
3. Ensure Maven dependencies are downloaded and synced properly.
4. Run the `CampusBiteApplication.java` main class.
5. Open your browser and navigate to: `http://localhost:8080`

> **Note on Frontend Preview:** If you want to preview the UI without starting the Java server (e.g., using VS Code Live Server), the frontend will automatically fallback to static hardcoded JavaScript data so you can still view the menu cards and cart functionality!

## Database Setup

1. Install MySQL Server.

2. Create the database:

   ```sql
   CREATE DATABASE campus_bite;
   ```

3. Update the MySQL username and password inside `application.properties`.

4. Ensure MySQL server is running before starting the application.

5. Run the Spring Boot application normally.


## 4. Admin Login Details
Upon the first run, the application automatically generates a default admin account.
* **Username:** `admin`
* **Password:** `admin123`

