<<<<<<< HEAD
# appointment-scheduler
appointment-scheduler desktop app for scheduling the appointments
=======
# Appointment Scheduler System

A comprehensive JavaFX-based appointment scheduling application with Spring Boot backend, featuring role-based access control for Users, Staff, and Admins.

## Features

### Phase 1 (Current)
- ✅ User registration and secure authentication
- ✅ Role-based dashboards (User, Staff, Admin)
- ✅ Appointment booking with conflict detection
- ✅ Appointment management (reschedule, cancel, status updates)
- ✅ Staff schedule management
- ✅ Admin staff/admin account creation
- ✅ Review and rating system
- ✅ Analytics dashboard
- ✅ Profile management

### Phase 2 (Planned)
- 📧 Email notifications and receipts
- 📢 Broadcast messages and announcements
- 📰 News and offers feed
- 🔔 In-app notifications
- 📊 Advanced analytics and reporting

## Technology Stack

- **Frontend**: JavaFX 21, FXML, CSS
- **Backend**: Spring Boot 3.2, Spring Security, Spring Data JPA
- **Database**: MySQL
- **Build Tool**: Maven
- **Java Version**: 17

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## Setup Instructions

### 1. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE appointment_scheduler;
```

### 2. Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/appointment_scheduler?createDatabaseIfNotExist=true
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn javafx:run
```

Or run the main class:
```bash
java -jar target/appointment-scheduler-1.0.0.jar
```

## Default Credentials

The application starts with no default users. You need to:

1. **Register a User Account**: Use the registration screen
2. **Create Admin Account**: Manually insert into database or use the first-time setup

### Manual Admin Creation (SQL)

Generate a BCrypt hash for your chosen password using the included utility:
```bash
java -cp target/appointment-scheduler-1.0.0.jar com.scheduler.util.PasswordHashGenerator
```

Then insert the admin account:
```sql
INSERT INTO users (name, username, email, password_hash, role, status, created_at, updated_at)
VALUES ('Admin User', 'admin', 'admin@example.com', 
        '<YOUR_BCRYPT_HASH>', 
        'ADMIN', 'ACTIVE', NOW(), NOW());
```

## User Roles

### User
- Register and login
- Book appointments
- View appointment history
- Reschedule/cancel appointments
- Submit reviews and ratings
- Update profile

### Staff
- View assigned appointments
- Update appointment status
- Manage daily schedule
- View personal statistics

### Admin
- Create staff and admin accounts
- View all bookings
- Manage users (deactivate accounts)
- View system analytics
- Monitor reviews and ratings
- Full system oversight

## Project Structure

```
appointment-scheduler/
├── src/
│   ├── main/
│   │   ├── java/com/scheduler/
│   │   │   ├── config/          # Security and app configuration
│   │   │   ├── controller/      # JavaFX controllers
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── repository/      # Data repositories
│   │   │   ├── service/         # Business logic
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── security/        # Authentication logic
│   │   │   ├── util/            # Utility classes
│   │   │   └── exception/       # Custom exceptions
│   │   └── resources/
│   │       ├── fxml/            # JavaFX views
│   │       ├── css/             # Stylesheets
│   │       └── application.properties
│   └── test/                    # Unit tests
├── pom.xml
└── README.md
```

## Key Features Explained

### Appointment Booking
- Select staff member
- Choose date and time
- Automatic conflict detection
- Prevents double booking

### Status Management
- PENDING: Initial booking state
- CONFIRMED: Staff/admin confirmed
- COMPLETED: Service completed
- CANCELLED: User/admin cancelled
- RESCHEDULED: Time changed
- NO_SHOW: User didn't attend

### Security
- BCrypt password hashing
- Role-based access control
- Session management
- Protected endpoints

## API Endpoints (Internal)

The application uses internal service calls, but REST endpoints are configured:

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - Authentication
- `GET /api/user/appointments` - User appointments
- `POST /api/user/book` - Book appointment
- `GET /api/staff/appointments` - Staff appointments
- `PUT /api/staff/status` - Update status
- `POST /api/admin/create-staff` - Create staff/admin
- `GET /api/admin/analytics` - System analytics

## Development

### Running Tests

```bash
mvn test
```

### Building for Production

```bash
mvn clean package
```

## Troubleshooting

### Database Connection Issues
- Verify MySQL is running
- Check credentials in `application.properties`
- Ensure database exists

### JavaFX Issues
- Verify Java 17+ with JavaFX support
- Check JavaFX SDK installation
- Ensure proper Maven configuration

### Login Issues
- Verify user exists in database
- Check account status is ACTIVE
- Ensure password is correct

## Future Enhancements (Phase 2)

- Email integration for notifications
- SMS reminders
- Calendar integration
- Mobile app
- Payment processing
- Multi-branch support
- Advanced reporting

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.

## Support

For issues and questions:
- Create an issue in the repository
- Contact: support@appointmentscheduler.com

## Acknowledgments

Built with Spring Boot, JavaFX, and MySQL for a modern, secure appointment scheduling experience.
>>>>>>> c065111 (Initial commit)
