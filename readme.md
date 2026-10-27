# SubWise — Subscription Management System

SubWise is a college project for managing recurring subscriptions. It contains two separate applications that use the same MySQL database:

1. **Member 1 — JavaFX desktop application** for customers and administrators.
2. **Member 2 — Java web application** built with Java, JSP, Servlets, Maven and Tomcat.
3. **Database** — MySQL schema/setup script in `database/subwise.sql`.

> Keep the two Maven projects separate. Each has its own `pom.xml`.

## 1. Project structure

```text
Subwise-complete/
├── database/
│   └── subwise.sql
├── member1/                       # JavaFX desktop application
│   ├── pom.xml
│   └── src/main/java/com/subwise/
│       ├── App.java
│       ├── customer/               # Customer UI controllers
│       ├── dao/                    # Database access
│       ├── database/               # JavaFX DB connection
│       ├── member3/                # Admin dashboard and monitoring
│       ├── model/
│       └── utils/
└── member2/
    └── subwise/                    # Java web application
        ├── pom.xml
        └── src/main/
            ├── java/com/subwise/
            │   ├── dao/
            │   ├── listener/
            │   ├── model/
            │   ├── servlet/
            │   └── util/
            └── webapp/
                ├── WEB-INF/
                ├── css/
                ├── admin-login.jsp
                ├── admin-dashboard.jsp
                ├── login.jsp
                ├── register.jsp
                ├── dashboard.jsp
                ├── subscriptions.jsp
                ├── payments.jsp
                └── profile.jsp
```

`target/` folders contain generated build output. Maven can recreate them. Don't include `member1_backup` or `subwise_backup` in the ZIP if you have verified a separate backup.

## 2. Requirements

- JDK 21
- Maven (tested with Maven 3.10.0)
- MySQL Server 8
- MySQL Workbench (optional)
- Apache Tomcat 10.1 for the web application

Check Java and Maven in PowerShell:

```powershell
java -version
mvn -version
```

If Maven isn't on PATH, use the full path to `mvn.cmd`, for example:

```powershell
& "C:\Users\YOUR_USERNAME\Downloads\apache-maven-3.10.0\bin\mvn.cmd" -version
```

Replace `YOUR_USERNAME` and paths with your own computer's locations.

## 3. Database setup

### First-time setup

1. Start MySQL Server.
2. Open MySQL Workbench and connect to your local MySQL server.
3. Open `database/subwise.sql`.
4. Review the SQL script, then execute it to create the schema/tables/categories as defined in the script.
5. Verify the `subwise` schema and tables `users`, `categories`, `subscriptions`, and `payments` exist.

**Warning:** The SQL script may create or alter data depending on its contents. Do not run it over an existing database without checking it first. If the database already contains demo data, don't re-import just to launch the application.

### Database password configuration

The web app's Tomcat process uses the environment variable `SUBWISE_DB_PASSWORD` for its database password. Set it to the password for the MySQL account configured in the web project's `DBConnection.java`.

For the current PowerShell session only:

```powershell
$env:SUBWISE_DB_PASSWORD = "YOUR_MYSQL_PASSWORD"
```

This setting applies only to processes launched from that PowerShell session. If Tomcat runs as a Windows service, configure the variable in the service's environment/startup configuration and restart Tomcat. Do not put real passwords in source code or commit them to the ZIP.

The JavaFX app has a separate connection configuration in `member1/src/main/java/com/subwise/database/DBConnection.java`. Check it on the teammate's computer and update it for their MySQL credentials if needed.

## 4. Run the JavaFX desktop application (Member 1)

Open PowerShell or the VS Code terminal.

1. Change to the JavaFX project folder:

```powershell
cd "PATH\TO\Subwise-complete\member1"
```

Example:

```powershell
cd "C:\Users\YOUR_USERNAME\Desktop\Subwise-complete\member1"
```

2. Build:

```powershell
mvn clean package
```

3. Launch:

```powershell
mvn javafx:run
```

If Maven isn't on PATH, use the full `mvn.cmd` path:

```powershell
& "C:\Users\YOUR_USERNAME\Downloads\apache-maven-3.10.0\bin\mvn.cmd" clean package
& "C:\Users\YOUR_USERNAME\Downloads\apache-maven-3.10.0\bin\mvn.cmd" javafx:run
```

### JavaFX features

- Welcome screen with customer/admin entry points
- Customer login and registration
- Customer dashboard and subscription management
- Payment history and simulated demo payments
- Customer profile
- Admin dashboard with subscription monitoring, search/filtering, category spending chart and expiry-related controls

**Data note:** Some JavaFX dashboard/state features use the JavaFX project's `SubscriptionStore`/`UserStore`. Don't assume every JavaFX view automatically refreshes from web-created records unless verified in the running application.

## 5. Run the web application (Member 2)

### Build the WAR file

1. Open a terminal.
2. Change to the web project folder:

```powershell
cd "PATH\TO\Subwise-complete\member2\subwise"
```

Example:

```powershell
cd "C:\Users\YOUR_USERNAME\Desktop\Subwise-complete\member2\subwise"
```

3. Build:

```powershell
mvn clean package
```

The WAR should be created at:

```text
member2/subwise/target/subwise.war
```

### Deploy to Tomcat 10.1

1. Stop Tomcat before replacing an existing deployment.
2. Copy `target/subwise.war` to Tomcat's `webapps` folder and name it `subwise.war`.
3. If replacing an old deployment, remove the old exploded `webapps/subwise` folder only while Tomcat is stopped and only if you intend to replace it.
4. Start Tomcat and wait for deployment.

A common Windows Tomcat path is:

```text
C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps\
```

Your installation path may differ. Do not delete the entire `webapps` folder.

### Open the web application

Customer login:

```text
http://localhost:8080/subwise/login.jsp
```

Registration:

```text
http://localhost:8080/subwise/register.jsp
```

Admin login:

```text
http://localhost:8080/subwise/admin-login.jsp
```

Adjust the port or context path if your Tomcat configuration differs.

### Web application features

- Customer registration and login
- Dashboard and subscription management
- Add/edit/cancel subscriptions
- Payment history with simulated demo payments
- Customer profile updates
- Upcoming-expiry reminders and background expiry monitoring
- Admin login and dashboard
- Cross-customer subscription overview, search/filtering, category analytics and administrative controls

Payments are simulated for this college demo; no real payment gateway is connected.

## 6. Demo accounts and sample data

The demo database was reported to contain:

| Account | User ID | Email | Role |
|---|---:|---|---|
| Riya | 5 | `riya@gmail.com` | CUSTOMER |
| Raj | 6 | `raj@gmail.com` | CUSTOMER |
| Administrator | Check the `users` table | `admin@subwise.com` | ADMIN |

These IDs reflect the database at the time this README was written. Auto-increment IDs may change if the database is reset or recreated. Verify them using:

```sql
USE subwise;
SELECT user_id, name, email, role FROM users ORDER BY user_id;
```

### Admin demo credentials

- Web admin: `admin@subwise.com` / `admin123` if the existing database account/password is still configured that way.
- JavaFX admin: `admin@subwise.com` / `admin123` (the current JavaFX version uses predefined demo credentials).

Customer passwords are those set during registration. Do not assume suggested passwords work unless you registered the accounts with them.

Suggested demo customer accounts:
- Riya Sharma — `riya@gmail.com`
- Raj Mehta — `raj@gmail.com`

### Sample subscriptions visible in the demo

| User ID | Service | Category | Price | Billing cycle | End date |
|---:|---|---|---:|---|---|
| 5 | Netflix | Entertainment | ₹649 | Monthly | 2026-11-25 |
| 5 | Spotify | Music | ₹119 | Monthly | 2026-10-30 |
| 5 | YouTube Premium | Entertainment | ₹149 | Monthly | 2026-10-29 |
| 5 | Google One | Utilities | ₹130 | Monthly | 2026-10-31 |
| 6 | Disney+ Hotstar | Entertainment | ₹2,990 | Yearly | 2026-10-30 |
| 6 | Coursera Plus | Education | ₹399 | Monthly | 2026-11-01 |
| 6 | Cult.fit | Health & Fitness | ₹999 | Monthly | 2026-11-10 |
| 6 | Microsoft 365 | Productivity | ₹489 | Monthly | 2026-11-20 |

These values reflect the visible demo dashboard and may change if records are edited. The live database is the source of truth.

### Categories visible in the web form

- Entertainment
- Music
- Streaming
- Productivity
- Gaming
- Utilities
- Education
- Health & Fitness
- Shopping
- Other

Use the exact category label shown in the form.

## 7. Suggested final-review demo flow

1. Start MySQL.
2. Start Tomcat and confirm the web app deploys.
3. Open the web customer login and show the customer dashboard.
4. Show subscriptions, categories, upcoming expiries, profile and payment history.
5. Log out and open the web admin login.
6. Show subscription totals, customer records, search/filtering and category analytics.
7. Launch JavaFX from `member1` using `mvn javafx:run`.
8. Demonstrate the desktop welcome screen, customer login and dashboard.
9. Demonstrate the JavaFX admin dashboard, expiry controls and category spending chart.
10. Explain that demo payments are simulated and the two applications have separate UI/controller implementations.

## 8. Troubleshooting

### Maven says there is no POM
You are in the wrong directory. Run Maven inside `member1` for JavaFX or `member2/subwise` for the web app.

### JavaFX cannot connect to MySQL
Check `member1/src/main/java/com/subwise/database/DBConnection.java`, confirm MySQL is running, and verify that the `subwise` database exists.

### Web app has a database connection error
Check `member2/subwise/src/main/java/com/subwise/util/DBConnection.java`, `SUBWISE_DB_PASSWORD`, and the Tomcat service environment. Restart Tomcat after changing service-level environment settings.

### Web app returns HTTP 404
Confirm Tomcat is running, `subwise.war` is deployed as `webapps/subwise.war`, and Tomcat completed deployment. Check Tomcat logs for deployment errors.

### Admin login fails
Check that the admin account exists and has role `ADMIN`:

```sql
USE subwise;
SELECT user_id, name, email, role
FROM users
WHERE email = 'admin@subwise.com';
```

The web admin login checks the database account and role. The JavaFX demo login uses predefined credentials in the current implementation.

