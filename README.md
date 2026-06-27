# CampusBite 🍔

**A modern, full-stack campus food ordering platform designed to eliminate waiting lines and streamline cafeteria operations.**

CampusBite provides a seamless experience for students to browse menus, add items to their cart, and securely check out using Razorpay integration. A dedicated Admin Dashboard allows cafeteria staff to manage the menu, track real-time orders, and view revenue analytics.

---

## 📸 Screenshots

### Admin Dashboard
<div align="center">
  <img width="48%" alt="Admin Page" src="https://github.com/user-attachments/assets/251aab71-60da-4c6e-b9bd-a076bf8dcfd6" />
  <img width="48%" alt="Admin Page" src="https://github.com/user-attachments/assets/e66b8c14-8507-4fb1-b38b-38e4676ea663" />
</div>
<br>

<div align="center">
  <img width="48%" alt="Admin Orders" src="https://github.com/user-attachments/assets/0eb6cb9a-b4f1-474f-a24d-1c4b3871ad00" />
  <img width="48%" alt="Admin Menu Management" src="https://github.com/user-attachments/assets/7d90755b-48cf-4f3e-af1d-ff0b01a9d223" />
</div>
<br>

### Mobile Responsive Design
<div align="center">
  <img width="25%" alt="Mobile View 1" src="https://github.com/user-attachments/assets/a1ce0354-3ed2-441f-9cf3-a9bba05deff7" />
  &nbsp; &nbsp; &nbsp;
  <img width="25%" alt="Mobile View 2" src="https://github.com/user-attachments/assets/cd49acf2-368a-4dfb-9c5b-a1b14298f5ba" />
    &nbsp; &nbsp; &nbsp;
  <img width="25%" alt="Mobile View 3" src="https://github.com/user-attachments/assets/9a70d926-92d6-4293-9eb4-f6bae6eec75d" />
</div>

---

## ✨ Features

### For Students:
* **Secure Login:** Google OAuth2 authentication restricted to official college email domains (`@sairamtap.edu.in`).
* **Live Menu:** Browse dynamic food categories and up-to-date item availability.
* **Shopping Cart:** Add, remove, and adjust quantities of food items before checkout.
* **Online Payments:** Fully integrated Razorpay gateway for seamless transactions.
* **Order Tracking:** View order history, payment statuses, and real-time order states.

### For Admins:
* **Centralized Dashboard:** View total revenue, active orders, and pending tasks at a glance.
* **Menu Management:** Add new food items, upload high-quality images, and adjust pricing/availability.
* **Order Processing:** Update order statuses (Pending -> Preparing -> Ready -> Delivered) in real-time.
* **Role-Based Security:** Secure JWT-based authentication ensuring only authorized staff can access the portal.

---

## 🛠️ Technology Stack

* **Backend:** Java 17, Spring Boot, Spring Security, Spring Data JPA
* **Database:** MySQL
* **Frontend:** Vanilla HTML, CSS, JavaScript (Mobile-Responsive UI)
* **Authentication:** Google OAuth2 (Students), JWT (Admins)
* **Payments:** Razorpay API
* **Deployment:** Pre-configured for Railway with persistent Volume support

---

## 🚀 Getting Started

### Prerequisites
* Java 17+
* MySQL 8.0+
* Maven
* A Razorpay Test Account
* A Google Cloud Console Project (for OAuth2)

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/BibiAshik/Campus-Bite.git
   cd Campus-Bite
   ```

2. **Configure Environment Variables:**
   Create an `application-local.properties` file in `src/main/resources/` with your actual database and API credentials. (Refer to `application.properties` for required keys).

3. **Run the Application:**
   Using Maven Wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the platform:**
   * Student Portal: `http://localhost:8080/student/login.html`
   * Admin Portal: `http://localhost:8080/admin/login.html`
