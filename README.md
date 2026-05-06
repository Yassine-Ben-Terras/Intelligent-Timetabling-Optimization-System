# 🚀 Planify — Intelligent Timetabling Optimization System

![Vue.js](https://img.shields.io/badge/Vue.js-3.x-4FC08D?style=for-the-badge\&logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge\&logo=spring-boot)
![Google OR-Tools](https://img.shields.io/badge/Google_OR--Tools-Optimization-4285F4?style=for-the-badge\&logo=google)
![TypeScript](https://img.shields.io/badge/TypeScript-Ready-3178C6?style=for-the-badge\&logo=typescript)

**Planify** is a full-stack **Intelligent Timetabling Optimization System** designed for higher education institutions.
It automates complex scheduling processes using **constraint programming** and **SAT (Boolean satisfiability)** techniques to generate **conflict-free, optimized timetables**.

---

## ✨ Features

* 🧠 **Advanced Optimization Engine**
  Powered by **Google OR-Tools (MaxSAT)** to handle complex scheduling constraints:

  * Room capacities
  * Professor availability
  * Session types
  * Working hours

* 🔐 **Secure Authentication & Authorization**
  Built with **Spring Security** and **JWT**

* 📊 **Bulk Data Import**
  Excel (`.xlsx`) ingestion using **Apache POI**

* 📄 **PDF Export & Archiving**
  Generate and store timetable reports automatically

* 🏫 **Academic Structure Management**
  Manage:

  * Academic years & semesters
  * Departments & fields (Filières)
  * Modules, sections, and groups

* 👨‍🏫 **Resource Management**
  Configure:

  * Professor availability (`DispoProf`)
  * Room types (`TypeLocal`)
  * Custom constraints

* ⚡ **Modern Frontend (SPA)**
  Built with **Vue 3 + Vite + Pinia + TypeScript**

---

## 🏗️ Architecture

This project follows a **monorepo structure** with a clear separation between frontend and backend.

### 🖥️ Frontend

* Vue 3 (Composition API)
* Vite
* TypeScript
* Pinia (State Management)
* Vue Router
* Axios (API communication)

### ⚙️ Backend

* Spring Boot (Java)
* Spring Security + JWT
* Spring Data JPA / Hibernate
* Google OR-Tools (MaxSAT Solver)
* Apache POI (Excel processing)

---

## 🚀 Getting Started

### ✅ Prerequisites

Make sure you have installed:

* Node.js (v16+)
* npm or yarn
* JDK (v17+)
* Maven (or use `mvnw`)
* PostgreSQL / MySQL

---

### 1️⃣ Database Configuration

Create a database and update:

```
Back-end/src/main/resources/application.properties
```

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/planify_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

---

### 2️⃣ Run Backend

```bash
cd Back-end
./mvnw clean install
./mvnw spring-boot:run
```

➡️ API available at: `http://localhost:8080`

---

### 3️⃣ Run Frontend

```bash
cd Front-end
npm install
npm run dev
```

➡️ App available at: `http://localhost:5173`

---

## 📂 Project Structure

```
Intelligent-Timetabling-Optimization-System/
├── Back-end/
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── entities/
│   ├── orTools/
│   ├── repository/
│   ├── security/
│   └── service/
│
└── Front-end/
    ├── assets/
    ├── components/
    ├── layouts/
    ├── router/
    ├── services/
    ├── stores/
    ├── types/
    └── views/
```

---

## ⚙️ Core Algorithm (OR-Tools)

The scheduling engine converts academic constraints into **Boolean variables**.

Examples:

* “Professor unavailable on Monday morning”
* “Group requires a lab room”

The **MaxSAT solver**:

* ✅ Enforces **hard constraints** (must be satisfied)
* 🎯 Optimizes **soft constraints** (preferences)

Result: an efficient, conflict-free timetable.

---

## 📝 License

This project is **proprietary** and intended for academic and institutional use.

---
