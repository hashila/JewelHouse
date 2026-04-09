# 🏷️ Jewellery E-Commerce SPA

This project is a **Single Page Application (SPA)** for a Jewellery E-Commerce platform, designed with a focus on **scalability, maintainability, and clean architecture**.

---

## 🚀 Tech Stack

### 🔹 Backend
- Java 21  
- Spring Boot 4.0.5  
- MySQL  

### 🔹 Frontend
- Angular 19.2.15  
- Tailwind CSS  

---

## 📦 Project Structure

- `backend/` → Spring Boot application  
- `frontend/` → Angular SPA  

---

## ⚙️ Prerequisites

Ensure the following are installed on your system:

- Java 21  
- Node.js & npm  
- Angular CLI  
- MySQL (default port: 3306)  

---

## 🗄️ Database Setup

1. Create the database:
```sql
CREATE DATABASE jwelryhousemst;
```

2. Import the provided `.sql` file into the database.

### Default Configuration
- Database Name: `jwelryhousemst`  
- Port: `3306`  

> These values can be modified in the backend configuration file if required.

---

## ▶️ Running the Backend (Spring Boot)

Navigate to the backend directory and run:

```bash
./mvnw spring-boot:run
```

Or (if Maven is installed globally):

```bash
mvn spring-boot:run
```

- Backend will start on:  
  **http://localhost:8080**

---

## ▶️ Running the Frontend (Angular)

Navigate to the frontend directory:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Run the application:

```bash
ng serve --port 4800
```

- Frontend will run on:  
  **http://localhost:4800**

> Note: CORS is configured in the backend to allow requests from port `4800`.

---

## 🔐 Configuration

Backend configurations (such as database settings and ports) can be updated in:

```
src/main/resources/application.yml
```

---

## ✨ Features

- Jewellery Item Management (CRUD operations)  
- Filtering (metal type, price range, category)  
- Sorting (price, name)
- Dynamic Price Calculation (metal price, making charges, taxes, shipping)  
- Responsive UI design  
- Input validation and error handling  

---

## 📄 Notes

- The project follows clean architecture principles for better scalability and maintainability.  
- RESTful APIs are implemented following best practices.  

---
