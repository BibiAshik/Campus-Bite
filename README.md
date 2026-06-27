# CampusBite 🍔

**A modern, full-stack campus food ordering platform designed to eliminate waiting lines and streamline cafeteria operations.**

CampusBite provides a seamless experience for students to browse menus, add items to their cart, and securely check out using Razorpay integration. A dedicated Admin Dashboard allows cafeteria staff to manage the menu, track real-time orders, and view revenue analytics.

---

## 📸 Screenshots

<br><br>
*(Manually insert your Student Dashboard / Login screenshot here)*
<br><br>

<br><br>
*(Manually insert your Admin Dashboard screenshot here)*
<br><br>

<br><br>
*(Manually insert your Checkout / Payment screenshot here)*
<br><br>

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
