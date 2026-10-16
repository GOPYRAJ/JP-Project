# Smart Campus Parcel & Delivery Management System

A Java + MySQL console application that manages parcels arriving on a campus: from the moment a courier drops a parcel at the gate, to assigning a delivery agent, notifying the student or staff member, and recording the pickup.

**Repository:** https://github.com/GOPYRAJ/JP-Project

| Layer | Technology |
|---|---|
| Language | Java 17 or higher |
| Database | MySQL 8.x |
| DB access | JDBC (`mysql-connector-j`) |
| Build tool | Maven |
| UI | Console menu (Scanner) |
| Version control | Git + GitHub |

---

## 1. Team and Modules

Each person owns **one module**. Every module has the same layers and must implement **CRUD** (Create, Read, Update, Delete).

| # | Module | Owner | Your branch name |
|---|---|---|---|
| 1 | User Management | *(name)* | `module1-user-management` |
| 2 | Parcel Management | *(name)* | `module2-parcel-management` |
| 3 | Delivery & Tracking | *(name)* | `module3-delivery-tracking` |
| 4 | Pickup Point & Notification | *(name)* | `module4-pickup-notification` |

Team leader: *(name)*

### Module 1: User Management (package `user`)
- **Create:** register a user (student / staff / admin / delivery agent)
- **Read:** list all users, search by ID, email or role
- **Update:** edit name, phone, hostel block, department, password
- **Delete:** remove a user (block it if the user still has active parcels)
- **Extra:** `login(email, password)`. Passwords are stored as hashes (see `util/PasswordUtil.java`). Use the same hash method that produced the sample data in `sample_data.sql`.

### Module 2: Parcel Management (package `parcel`)
- **Create:** register a received parcel (tracking number, courier, recipient, weight, pickup point)
- **Read:** list all parcels, search by tracking number, recipient or status
- **Update:** edit parcel details, change pickup point
- **Delete:** delete a parcel only if its status is `RECEIVED` or `CANCELLED`
- **Extra:** write the first row into `parcel_status_log` when a parcel is created. Provide `ParcelService.updateParcelStatus(int parcelId, String status)` for other modules to call.

### Module 3: Delivery & Tracking (package `delivery`)
- **Create:** assign a parcel to a delivery agent
- **Read:** deliveries by agent, by parcel, by status; full status history of a parcel
- **Update:** move the status `ASSIGNED → OUT_FOR_DELIVERY → DELIVERED / FAILED`, add remarks
- **Delete:** cancel an assignment (the parcel returns to `RECEIVED`)
- **Extra:** every status change also inserts a row into `parcel_status_log`. Only users with the `DELIVERY_AGENT` role can be assigned.

### Module 4: Pickup Point & Notification (package `pickup`)
- **Pickup points:** Create / Read / Update / Delete campus pickup points (gate, hostel office, library locker) with capacity
- **Notifications:** create, read (all / unread per user), mark as read, delete
- **Extra:** provide `NotificationService.send(int userId, int parcelId, String message)`. Modules 2 and 3 call it when a parcel is received or delivered.

---

## 2. Project Structure

```
JP-Project/
├── pom.xml
├── README.md
├── .gitignore
├── database/
│   ├── schema.sql                  # creates the database and all tables
│   └── sample_data.sql             # test data
└── src/main/
    ├── java/com/smartcampus/parcel/
    │   ├── Main.java               # main menu (leader edits)
    │   ├── config/DBConnection.java
    │   ├── util/                   # Constants, InputUtil, PasswordUtil
    │   ├── user/        {model, dao, service, ui}    # Module 1
    │   ├── parcel/      {model, dao, service, ui}    # Module 2
    │   ├── delivery/    {model, dao, service, ui}    # Module 3
    │   └── pickup/      {model, dao, service, ui}    # Module 4
    └── resources/
        ├── db.properties.example   # template (committed)
        └── db.properties           # YOUR local copy (never committed)
```

**Layer rules**

| Layer | Responsibility | Must NOT |
|---|---|---|
| `model` | Plain Java class: fields, constructors, getters/setters | contain SQL or logic |
| `dao` | All SQL / JDBC code, using `PreparedStatement` only | contain menus or business rules |
| `service` | Business rules and validation, calls the DAO | contain `Scanner` or SQL |
| `ui` | Console menu: reads input, prints output, calls the service | contain SQL |

Look at `ParcelService.java` and `NotificationService.java` as examples of the style.

---

## 3. Database

**Database name:** `smart_campus_parcel`. The exact tables and columns are in `database/schema.sql`. Read that file before writing your DAO, and use the column names exactly as written there.

| Table | Module | Description |
|---|---|---|
| `users` | 1 | Students, staff, admins, delivery agents |
| `parcels` | 2 | Every parcel received on campus |
| `deliveries` | 3 | Parcel-to-agent assignments |
| `parcel_status_log` | 3 | History of every status change |
| `pickup_points` | 4 | Collection locations |
| `notifications` | 4 | Messages sent to users |

```
users ──< parcels >── pickup_points
  │          │
  │          ├──< deliveries >── users (delivery agent)
  │          └──< parcel_status_log
  └──< notifications >── parcels
```

**Parcel status flow**
```
RECEIVED ──► ASSIGNED ──► OUT_FOR_DELIVERY ──► DELIVERED ──► COLLECTED
    │                            │
    │                            └──► FAILED ──► (reassign) ASSIGNED
    └──► CANCELLED
```

> Do not change table or column names yourself. Tell the team leader, who will update `schema.sql`.

---

## 4. CRUD Checklist per Module

| Module | Create | Read | Update | Delete |
|---|---|---|---|---|
| User | `addUser()` | `getUserById()`, `getAllUsers()`, `getUsersByRole()`, `login()` | `updateUser()` | `deleteUser()` |
| Parcel | `addParcel()` | `getParcelById()`, `getAllParcels()`, `searchByTrackingNo()`, `getByRecipient()` | `updateParcel()`, `updateParcelStatus()` | `deleteParcel()` |
| Delivery | `assignDelivery()` | `getDeliveriesByAgent()`, `getByParcel()`, `getStatusHistory()` | `updateDeliveryStatus()` | `cancelDelivery()` |
| Pickup / Notification | `addPickupPoint()`, `sendNotification()` | `getAllPickupPoints()`, `getNotificationsByUser()` | `updatePickupPoint()`, `markAsRead()` | `deletePickupPoint()`, `deleteNotification()` |

---

## 5. How Modules Connect

- Everyone uses `DBConnection.getConnection()`. Never write your own connection code.
- Modules talk to each other **only through service classes**, never by writing SQL on another module's table.
  ```java
  parcelService.updateParcelStatus(parcelId, "OUT_FOR_DELIVERY");
  notificationService.send(userId, parcelId, "Your parcel is out for delivery");
  ```
- `Main.java` shows the main menu. Each module exposes one menu class (`UserMenu`, `ParcelMenu`, `DeliveryMenu`, `PickupMenu`) with a `show()` method. The leader wires them in.

**Suggested build order:** Module 1 and the pickup-points part of Module 4 first, then Module 2, then Module 3, then notifications.

---

## 6. First-Time Setup (everyone, once)

1. **Install:** JDK 17 or higher, MySQL 8, Maven, Git, and an IDE (IntelliJ / VS Code / Eclipse). Check:
   ```bash
   java -version
   mvn -version
   git --version
   mysql --version
   ```
2. **Clone the repo** into a simple folder (not OneDrive, no spaces in the path if possible):
   ```bash
   git clone https://github.com/GOPYRAJ/JP-Project.git
   cd JP-Project
   ```
3. **Create the database.** Log in to MySQL from the project folder:
   ```bash
   mysql -u root -p
   ```
   At the `mysql>` prompt:
   ```sql
   SOURCE database/schema.sql;
   SOURCE database/sample_data.sql;
   exit;
   ```
4. **Create your `db.properties`:** copy `src/main/resources/db.properties.example` to `src/main/resources/db.properties` and enter **your own** MySQL password:
   ```properties
   db.url=jdbc:mysql://localhost:3306/smart_campus_parcel
   db.username=root
   db.password=YOUR_MYSQL_PASSWORD
   ```
5. **Build and run:**
   ```bash
   mvn clean compile
   mvn exec:java -Dexec.mainClass="com.smartcampus.parcel.Main"
   ```
   You should see `Database connected successfully.` and the main menu. Send the leader a screenshot to confirm.

> The `WARNING: ...` lines Maven prints on a new JDK are harmless.
> Two prompts exist: the Windows terminal (`mvn`, `git`, `mysql -u root -p`) and the MySQL prompt `mysql>` (`SOURCE`, `SELECT`, `exit;`). Do not mix them.

---

## 7. Git Workflow (very important)

**Never push directly to `main`.** `main` always holds working code, and nothing enters it without a Pull Request and a review.

### Create your branch (once)
```bash
git checkout main
git pull origin main
git checkout -b module1-user-management      # use YOUR branch name
```

### Daily routine
```bash
git checkout module1-user-management
git pull origin main                  # bring in the latest merged work
# ... write code in YOUR module package only ...
git add .
git status                            # check: db.properties must NOT be listed
git commit -m "Add UserDAO insert and select methods"
git push origin module1-user-management
```
The first push of a new branch may ask you to run the command Git prints (`git push --set-upstream origin ...`). Copy and run it.

### Pull Request
1. On GitHub, open the repo. A "Compare & pull request" button appears for your branch.
2. Base branch: `main`. Describe what you built and how you tested it.
3. The leader reviews and merges. If changes are requested, commit them to the same branch and push again; the PR updates itself.

### After a merge
```bash
git checkout your-branch
git pull origin main
```

### Rules to avoid conflicts
- Edit **only your own module's package**.
- Only the leader edits `schema.sql`, `sample_data.sql`, `pom.xml`, `Main.java`, `Constants.java` and `DBConnection.java`. If you need a change, message the leader.
- Commit small and often, with clear messages.
- Never commit `db.properties` or the `target/` folder. Both are in `.gitignore`.

---

## 8. Coding Standards

- Packages lowercase, classes `PascalCase`, methods and variables `camelCase`, tables and columns `snake_case`.
- Always use `PreparedStatement` with `?`. Never build SQL by joining strings (SQL injection).
- Use try-with-resources so connections close automatically.
- Never store plain-text passwords, and never hard-code passwords in code.
- Use constants from `Constants.java` for roles and statuses, not raw strings.
- Add short comments explaining what each method does.

---

## 9. Task Checklist

**Module 1: User Management**
- [ ] `User` model, `UserDAO`
- [ ] `UserService` with validation (unique email, valid phone)
- [ ] `login()` using `PasswordUtil`
- [ ] `UserMenu`

**Module 2: Parcel Management**
- [ ] `Parcel` model, `ParcelDAO`
- [ ] `ParcelService` (check the recipient and pickup point exist; real `updateParcelStatus`)
- [ ] Search by tracking number / recipient / status
- [ ] `ParcelMenu`

**Module 3: Delivery & Tracking**
- [ ] `Delivery` and `StatusLog` models, DAOs
- [ ] `DeliveryService` (only `DELIVERY_AGENT` can be assigned; follow the status flow)
- [ ] Status history view
- [ ] `DeliveryMenu`

**Module 4: Pickup Point & Notification**
- [ ] `PickupPoint` and `Notification` models, DAOs
- [ ] `PickupPointService` (capacity check, active/inactive)
- [ ] Real `NotificationService.send()`
- [ ] `PickupMenu`

**Everyone**
- [ ] Test every CRUD operation against the sample data
- [ ] Open a Pull Request when a feature is ready
- [ ] Tell the leader when your menu is ready to be wired into `Main.java`

---

## 10. Optional Enhancements (after CRUD is complete)

- Role-based menus (a student sees only their own parcels)
- Email/SMS notification integration
- OTP verification when collecting a parcel
- Reports: parcels per day, agent performance, pending parcels
- Swing/JavaFX GUI or a Spring Boot REST API
- JUnit 5 tests

---

## 11. Troubleshooting

| Problem | Fix |
|---|---|
| `mvn` / `mysql` not recognized | Not installed, or its `bin` folder is not in PATH. Add it and reopen the terminal |
| `Communications link failure` | MySQL service is not running (Windows Services → MySQL80), or wrong host/port |
| `Access denied for user 'root'` | Wrong password in `db.properties` |
| `Unknown database` | Run `database/schema.sql` first |
| `db.properties not found` | You skipped step 4 of the setup |
| `Cannot add or update a child row` | The referenced user / pickup point / parcel ID does not exist |
| `'SOURCE' is not recognized` | You typed a MySQL command in the Windows terminal. Log in with `mysql -u root -p` first |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Reload the Maven project so the dependency downloads |
