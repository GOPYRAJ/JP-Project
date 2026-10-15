# Smart Campus Parcel & Delivery Management System

A Java + MySQL application that manages parcels arriving on a campus: from the moment a courier drops a parcel at the gate, to assigning a delivery agent, notifying the student/staff, and recording pickup.

**Tech stack**

| Layer | Technology |
|---|---|
| Language | Java 17 (JDK 17+) |
| Database | MySQL 8.x |
| DB access | JDBC (`mysql-connector-j`) |
| Build tool | Maven |
| UI | Console menu (Scanner). Can be upgraded to Swing/JavaFX later |
| Version control | Git + GitHub |

---

## 1. Project Modules (4 Modules)

Each teammate owns one module. Every module has the same layered structure: **Model → DAO → Service → UI**, and every module must implement **CRUD** (Create, Read, Update, Delete).

| # | Module | Owner | Purpose |
|---|---|---|---|
| 1 | **User Management** | Member 1 | Register and manage students, staff, admins and delivery agents; login |
| 2 | **Parcel Management** | Member 2 | Register incoming parcels, search, update and remove them |
| 3 | **Delivery & Tracking** | Member 3 | Assign parcels to delivery agents, update delivery status, keep status history |
| 4 | **Pickup Point & Notification** | Member 4 | Manage campus pickup points/lockers and send notifications to recipients |

### Module 1: User Management
- **Create:** register a new user (student / staff / admin / delivery agent)
- **Read:** view all users, search by ID, email or role
- **Update:** edit name, phone, hostel block, department, password
- **Delete:** remove a user (block if the user still has active parcels)
- **Extra:** `login(email, password)` with role check (store hashed password, e.g. SHA-256 or BCrypt)

### Module 2: Parcel Management
- **Create:** register a received parcel (tracking number, courier, recipient, weight, pickup point)
- **Read:** list all parcels, search by tracking number, recipient or status
- **Update:** edit parcel details, change pickup point
- **Delete:** delete a parcel record (only if status is `RECEIVED` or `CANCELLED`)
- **Extra:** auto-write the first entry into `parcel_status_log` when a parcel is created

### Module 3: Delivery & Tracking
- **Create:** assign a parcel to a delivery agent
- **Read:** view deliveries by agent, by parcel, by status; view full status history of a parcel
- **Update:** update delivery status (`ASSIGNED → OUT_FOR_DELIVERY → DELIVERED / FAILED`), add remarks
- **Delete:** cancel an assignment (parcel goes back to `RECEIVED`)
- **Extra:** every status change must also insert a row into `parcel_status_log`

### Module 4: Pickup Point & Notification
- **Pickup points**: Create / Read / Update / Delete campus pickup points (security gate, hostel office, library locker, etc.) with capacity
- **Notifications**: Create a notification, read notifications of a user (unread / all), mark as read, delete
- **Extra:** when a parcel is received or delivered, call `NotificationService.send(...)` to notify the recipient (Modules 2 and 3 call this)

---

## 2. Project Structure

```
smart-campus-parcel-system/
├── pom.xml
├── README.md
├── .gitignore
│
├── database/
│   ├── schema.sql                  # CREATE DATABASE + all tables
│   └── sample_data.sql             # test data for everyone
│
└── src/
    └── main/
        ├── java/com/smartcampus/parcel/
        │   │
        │   ├── Main.java                         # entry point, main menu
        │   │
        │   ├── config/
        │   │   └── DBConnection.java             # JDBC connection (shared)
        │   │
        │   ├── util/
        │   │   ├── InputUtil.java                # safe Scanner helpers
        │   │   ├── PasswordUtil.java             # password hashing
        │   │   └── Constants.java                # status/role constants
        │   │
        │   ├── user/                             # ---- MODULE 1 ----
        │   │   ├── model/User.java
        │   │   ├── dao/UserDAO.java
        │   │   ├── dao/UserDAOImpl.java
        │   │   ├── service/UserService.java
        │   │   └── ui/UserMenu.java
        │   │
        │   ├── parcel/                           # ---- MODULE 2 ----
        │   │   ├── model/Parcel.java
        │   │   ├── dao/ParcelDAO.java
        │   │   ├── dao/ParcelDAOImpl.java
        │   │   ├── service/ParcelService.java
        │   │   └── ui/ParcelMenu.java
        │   │
        │   ├── delivery/                         # ---- MODULE 3 ----
        │   │   ├── model/Delivery.java
        │   │   ├── model/StatusLog.java
        │   │   ├── dao/DeliveryDAO.java
        │   │   ├── dao/DeliveryDAOImpl.java
        │   │   ├── dao/StatusLogDAO.java
        │   │   ├── service/DeliveryService.java
        │   │   └── ui/DeliveryMenu.java
        │   │
        │   └── pickup/                           # ---- MODULE 4 ----
        │       ├── model/PickupPoint.java
        │       ├── model/Notification.java
        │       ├── dao/PickupPointDAO.java
        │       ├── dao/PickupPointDAOImpl.java
        │       ├── dao/NotificationDAO.java
        │       ├── dao/NotificationDAOImpl.java
        │       ├── service/PickupPointService.java
        │       ├── service/NotificationService.java
        │       └── ui/PickupMenu.java
        │
        └── resources/
            └── db.properties                     # url, username, password
```

**Layer rules**

| Layer | Responsibility | Must NOT |
|---|---|---|
| `model` | Plain Java class (fields, constructors, getters/setters) | contain SQL or logic |
| `dao` | All SQL / JDBC code (use `PreparedStatement` only) | contain menus or business rules |
| `service` | Business rules and validation, calls DAO | contain `Scanner` or SQL |
| `ui` | Console menu, reads input, prints output, calls service | contain SQL |

---

## 3. Database Schema (MySQL)

**Database name:** `smart_campus_parcel`

### ER Overview

```
users ──< parcels >── pickup_points
  │          │
  │          ├──< deliveries >── users (delivery agent)
  │          │
  │          └──< parcel_status_log
  │
  └──< notifications >── parcels
```

### Tables

| Table | Module | Description |
|---|---|---|
| `users` | 1 | All people in the system |
| `parcels` | 2 | Every parcel received on campus |
| `deliveries` | 3 | Parcel to agent assignments |
| `parcel_status_log` | 3 | History of every status change |
| `pickup_points` | 4 | Collection locations |
| `notifications` | 4 | Messages sent to users |

### `database/schema.sql`

```sql
CREATE DATABASE IF NOT EXISTS smart_campus_parcel;
USE smart_campus_parcel;

-- MODULE 1
CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    full_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    phone          VARCHAR(15)  NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    role           ENUM('STUDENT','STAFF','ADMIN','DELIVERY_AGENT') NOT NULL,
    department     VARCHAR(100),
    hostel_block   VARCHAR(50),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- MODULE 4
CREATE TABLE pickup_points (
    point_id    INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    location    VARCHAR(200) NOT NULL,
    capacity    INT NOT NULL CHECK (capacity > 0),
    is_active   BOOLEAN DEFAULT TRUE
);

-- MODULE 2
CREATE TABLE parcels (
    parcel_id        INT AUTO_INCREMENT PRIMARY KEY,
    tracking_no      VARCHAR(50)  NOT NULL UNIQUE,
    sender_name      VARCHAR(100) NOT NULL,
    courier_company  VARCHAR(100) NOT NULL,
    recipient_id     INT NOT NULL,
    pickup_point_id  INT NOT NULL,
    weight_kg        DECIMAL(6,2),
    description      VARCHAR(255),
    status           ENUM('RECEIVED','ASSIGNED','OUT_FOR_DELIVERY',
                          'DELIVERED','COLLECTED','FAILED','CANCELLED')
                     DEFAULT 'RECEIVED',
    received_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    collected_at     TIMESTAMP NULL,
    FOREIGN KEY (recipient_id)    REFERENCES users(user_id),
    FOREIGN KEY (pickup_point_id) REFERENCES pickup_points(point_id)
);

-- MODULE 3
CREATE TABLE deliveries (
    delivery_id   INT AUTO_INCREMENT PRIMARY KEY,
    parcel_id     INT NOT NULL,
    agent_id      INT NOT NULL,
    status        ENUM('ASSIGNED','OUT_FOR_DELIVERY','DELIVERED','FAILED')
                  DEFAULT 'ASSIGNED',
    assigned_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at  TIMESTAMP NULL,
    remarks       VARCHAR(255),
    FOREIGN KEY (parcel_id) REFERENCES parcels(parcel_id) ON DELETE CASCADE,
    FOREIGN KEY (agent_id)  REFERENCES users(user_id)
);

CREATE TABLE parcel_status_log (
    log_id       INT AUTO_INCREMENT PRIMARY KEY,
    parcel_id    INT NOT NULL,
    status       VARCHAR(30) NOT NULL,
    changed_by   INT,
    changed_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remarks      VARCHAR(255),
    FOREIGN KEY (parcel_id)  REFERENCES parcels(parcel_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- MODULE 4
CREATE TABLE notifications (
    notification_id  INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL,
    parcel_id        INT,
    message          VARCHAR(255) NOT NULL,
    is_read          BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)   REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (parcel_id) REFERENCES parcels(parcel_id) ON DELETE SET NULL
);
```

> **Important:** Create tables in the order above (`users` → `pickup_points` → `parcels` → rest) because of foreign keys. Do not change table or column names without telling the team.

---

## 4. CRUD Summary (what every module must have)

| Module | Create | Read | Update | Delete |
|---|---|---|---|---|
| User | `addUser()` | `getUserById()`, `getAllUsers()`, `getUsersByRole()`, `login()` | `updateUser()` | `deleteUser()` |
| Parcel | `addParcel()` | `getParcelById()`, `getAllParcels()`, `searchByTrackingNo()`, `getByRecipient()` | `updateParcel()`, `updateParcelStatus()` | `deleteParcel()` |
| Delivery | `assignDelivery()` | `getDeliveriesByAgent()`, `getByParcel()`, `getStatusHistory()` | `updateDeliveryStatus()` | `cancelDelivery()` |
| Pickup / Notification | `addPickupPoint()`, `sendNotification()` | `getAllPickupPoints()`, `getNotificationsByUser()` | `updatePickupPoint()`, `markAsRead()` | `deletePickupPoint()`, `deleteNotification()` |

---

## 5. How Modules Connect

```
Module 1 (users)  ──►  used by Modules 2, 3, 4 (recipient_id, agent_id, user_id)
Module 4 (pickup_points)  ──►  used by Module 2 (pickup_point_id)
Module 2 (parcels)  ──►  used by Modules 3 and 4
Module 3  ──►  calls ParcelService.updateParcelStatus() + NotificationService.send()
```

**Suggested build order:** Module 1 and Module 4 (pickup points) first, then Module 2, then Module 3, then notifications.

Until a teammate's module is ready, use the data in `database/sample_data.sql` to test your own module.

---

## 6. Setup Guide (do this once)

1. **Install:** JDK 17, MySQL 8, Maven, Git, and an IDE (IntelliJ / Eclipse / VS Code).
2. **Clone the repo:**
   ```bash
   git clone <repo-url>
   cd smart-campus-parcel-system
   ```
3. **Create the database:** open MySQL and run
   ```bash
   mysql -u root -p < database/schema.sql
   mysql -u root -p < database/sample_data.sql
   ```
4. **Configure `src/main/resources/db.properties`:**
   ```properties
   db.url=jdbc:mysql://localhost:3306/smart_campus_parcel
   db.username=root
   db.password=your_password
   ```
5. **Maven dependency** (in `pom.xml`):
   ```xml
   <dependency>
       <groupId>com.mysql</groupId>
       <artifactId>mysql-connector-j</artifactId>
       <version>8.3.0</version>
   </dependency>
   ```
6. **Run:** execute `Main.java` from your IDE, or
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.smartcampus.parcel.Main"
   ```

---

## 7. Team Rules and Coding Standards

- **Package names** lowercase; **classes** `PascalCase`; **methods/variables** `camelCase`; **tables/columns** `snake_case`.
- Always use `PreparedStatement` (never string-concatenated SQL) to prevent SQL injection.
- Use try-with-resources so connections are closed automatically.
- Never store plain-text passwords. Never commit `db.properties` with a real password (it is in `.gitignore`).
- Only edit files inside **your own module package**. If you need a change in another module or the schema, tell the owner / team.
- Use constants from `Constants.java` for roles and statuses; do not hard-code strings.

### Git workflow

```bash
git checkout -b module1-user-management     # one branch per module
git add .
git commit -m "Add UserDAO CRUD operations"
git push origin module1-user-management
```
Open a Pull Request into `main`; at least one teammate reviews before merging. Pull `main` regularly (`git pull origin main`).

---

## 8. Parcel Status Flow

```
RECEIVED ──► ASSIGNED ──► OUT_FOR_DELIVERY ──► DELIVERED ──► COLLECTED
    │                            │
    │                            └──► FAILED ──► (reassign) ASSIGNED
    └──► CANCELLED
```

---

## 9. Team Task Checklist

**Member 1: User Management**
- [ ] `User` model, `UserDAO`, `UserDAOImpl`
- [ ] `UserService` with validation (unique email, valid phone)
- [ ] `PasswordUtil` and `login()`
- [ ] `UserMenu` console UI

**Member 2: Parcel Management**
- [ ] `Parcel` model, `ParcelDAO`, `ParcelDAOImpl`
- [ ] `ParcelService` (validate recipient and pickup point exist)
- [ ] Search by tracking number / recipient / status
- [ ] `ParcelMenu` console UI

**Member 3: Delivery & Tracking**
- [ ] `Delivery` and `StatusLog` models, DAOs
- [ ] `DeliveryService` (only `DELIVERY_AGENT` role can be assigned; follow status flow)
- [ ] Status history view
- [ ] `DeliveryMenu` console UI

**Member 4: Pickup Point & Notification**
- [ ] `PickupPoint` and `Notification` models, DAOs
- [ ] `PickupPointService` (capacity check, active/inactive)
- [ ] `NotificationService.send()` used by other modules
- [ ] `PickupMenu` console UI

**Everyone**
- [ ] Wire your menu into `Main.java`
- [ ] Test every CRUD operation with sample data
- [ ] Add 5+ rows of your own test data to `sample_data.sql`

---

## 10. Optional Enhancements (after CRUD is complete)

- Role-based menus (student sees only own parcels; admin sees everything)
- Email/SMS notification integration
- OTP verification when collecting a parcel
- Reports: parcels per day, agent performance, pending parcels
- Swing/JavaFX GUI or a Spring Boot REST API
- Unit tests with JUnit 5

---

## 11. Troubleshooting

| Problem | Fix |
|---|---|
| `Communications link failure` | MySQL not running, or wrong host/port in `db.properties` |
| `Access denied for user` | Wrong username/password in `db.properties` |
| `Unknown database` | Run `schema.sql` first |
| `Cannot add or update a child row` | The referenced user / pickup point / parcel ID does not exist |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Maven dependency not loaded; reload Maven project |
