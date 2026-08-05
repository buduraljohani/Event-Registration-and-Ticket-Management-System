<div align="center">

# 🎟️ Event Registration and Ticket Management System

### A role-based Java Swing desktop application for managing events, registrations, and tickets

[![Java](https://img.shields.io/badge/Java-Swing-orange?logo=openjdk&logoColor=white)](https://docs.oracle.com/javase/8/docs/api/javax/swing/package-summary.html)
[![Database](https://img.shields.io/badge/Database-MySQL-blue?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Build](https://img.shields.io/badge/Build-Apache%20Ant-red?logo=apache&logoColor=white)]()
[![IDE](https://img.shields.io/badge/IDE-NetBeans-1B6AC6?logo=apache-netbeans-ide&logoColor=white)]()
[![Status](https://img.shields.io/badge/status-completed-brightgreen)]()

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [User Roles](#-user-roles)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [Database Setup](#-database-setup)
- [Screenshots](#-screenshots)
- [Future Improvements](#-future-improvements)
- [Author](#-author)

---

## 📖 Overview

**Event Registration and Ticket Management System** is an integrated **Java (Swing)** desktop application for managing event registration and ticket generation, backed by a **MySQL** database (designed in MySQL Workbench).

The system provides **three separate dashboards**, each tailored to a specific role in the event lifecycle — from attendees browsing and registering for events, to organizers managing their own events, to admins overseeing the entire system.

---

## ✨ Key Features

- 🔐 **Secure authentication** — login and signup with password hashing (`HashUtil`)
- 🎫 **Event registration & ticket generation** for attendees
- 📅 **Event management** (create, update, oversee) for organizers
- 📊 **System oversight & reporting** for admins (`admin_report.txt` output)
- 🖥️ **Role-based dashboards** built with Java Swing, each with its own frame/UI
- 🗄️ **Persistent storage** via a dedicated MySQL connection layer (`DBConnection`)
- 🧩 Clean separation between UI frames, business logic, and data access

---

## 👥 User Roles

| Role | Capabilities |
|---|---|
| 🧑‍💻 **Attendee** | Register for events, browse available events, view/manage their tickets |
| 🗂️ **Organizer** | Create and manage events, track registrations |
| 🛡️ **Admin** | Oversee the system, manage users/events, generate reports |

Each role has a dedicated dashboard frame:
- `AttendeeDashboardFrame`
- `OrganizerDashboardFrame`
- `AdminDashboardFrame`

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java |
| **UI Framework** | Java Swing |
| **Database** | MySQL (schema designed in MySQL Workbench) |
| **Build Tool** | Apache Ant (NetBeans project) |
| **IDE** | Apache NetBeans |

---

## 📁 Project Structure

```
Event-Registration-and-Ticket-Management-System/
│
├── src/
│   ├── LoginFrame.java                 -- Login screen & authentication
│   ├── SignupFrame.java                -- New user registration screen
│   ├── HashUtil.java                   -- Password hashing utility
│   ├── DBConnection.java               -- MySQL database connection handler
│   ├── Event_project.java              -- Main application entry point
│   ├── AttendeeDashboardFrame.java      -- Attendee dashboard UI
│   ├── OrganizerDashboardFrame.java     -- Organizer dashboard UI
│   └── AdminDashboardFrame.java         -- Admin dashboard UI
│
├── build.xml                            -- Ant build script
├── build-impl.xml                       -- NetBeans-generated build implementation
├── manifest.mf                          -- JAR manifest
├── project.xml / project.properties     -- NetBeans project configuration
├── private.xml / private.properties     -- Local NetBeans settings
├── genfiles.properties                  -- Ant/NetBeans generated files tracking
├── admin_report.txt                     -- Sample admin report output
└── README.md
```

> 💡 **Tip:** Consider adding a `.gitignore` for NetBeans/Ant build artifacts (`*.class`, `build/`, `dist/`, `nbproject/private/`) so only source files are tracked in version control — this keeps the repo clean and diff-friendly.

---

## 🏗️ Architecture

```
┌─────────────────┐        ┌──────────────────┐       ┌─────────────────┐
│   LoginFrame /   │──────▶│  Role Dashboards  │──────▶│   DBConnection   │
│   SignupFrame    │        │  (Attendee /      │       │  (MySQL Access)  │
│                  │        │   Organizer /     │       │                  │
│  HashUtil for    │        │   Admin)          │       │  Event_project   │
│  password hash   │        │                  │       │  (entry point)   │
└─────────────────┘        └──────────────────┘       └─────────────────┘
```

1. **Event_project** launches the application and opens `LoginFrame`.
2. Users authenticate or sign up (`SignupFrame`) — passwords are hashed via `HashUtil` before being stored/verified through `DBConnection`.
3. Based on the user's role, the appropriate dashboard (`Attendee` / `Organizer` / `Admin`) is loaded.
4. All dashboards communicate with the MySQL database through the shared `DBConnection` class.

---

## 🚀 Getting Started

### Prerequisites
- Java JDK 8+
- MySQL Server + MySQL Workbench
- Apache NetBeans IDE (recommended, since the project includes NetBeans project files)

### Steps
1. **Clone the repository**
   ```bash
   git clone https://github.com/buduraljohani/Event-Registration-and-Ticket-Management-System.git
   ```
2. **Open in NetBeans**
   - `File → Open Project` → select the cloned folder.
3. **Set up the database** (see [Database Setup](#-database-setup) below).
4. **Configure the connection** in `DBConnection.java` with your MySQL host, username, and password.
5. **Build & Run**
   - Right-click the project → `Clean and Build`, then `Run`.

---

## 🗄️ Database Setup

1. Open **MySQL Workbench** and create a new schema (e.g. `event_management_db`).
2. Create the required tables (Users/Roles, Events, Registrations/Tickets) matching the fields referenced in `DBConnection.java` and the dashboard classes.
3. Update the JDBC connection string, username, and password inside `DBConnection.java` to point to your local schema.
4. Run the application — new signups and events will populate the database automatically.

> 📌 If you have the original MySQL Workbench `.mwb` file or SQL export for this schema, add it to a `/database` folder in the repo and link it here for full reproducibility.

---

## 🖼️ Screenshots

> Add screenshots of the Login screen, Signup screen, and each dashboard here for a stronger presentation, e.g.:
>
> ```
> ![Login Screen](./screenshots/login.png)
> ![Attendee Dashboard](./screenshots/attendee-dashboard.png)
> ![Organizer Dashboard](./screenshots/organizer-dashboard.png)
> ![Admin Dashboard](./screenshots/admin-dashboard.png)
> ```

---

## 🔮 Future Improvements

- [ ] Add input validation and error handling across all forms
- [ ] Encrypt/secure database credentials (avoid hardcoding in `DBConnection`)
- [ ] Export tickets as PDF (QR-coded) instead of plain text reports
- [ ] Add unit tests for `HashUtil` and database operations
- [ ] Migrate build to Maven/Gradle for easier dependency management outside NetBeans

---

## 👤 Author

**Budur Aljohani** — [@buduraljohani](https://github.com/buduraljohani)

If you found this project useful, consider giving it a ⭐!
