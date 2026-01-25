# PortPeace - Professional Port Management Tool

## Overview

PortPeace is a professional-grade port management tool designed to eliminate port conflicts in local development environments. It provides intelligent port allocation, conflict prevention, and team-aware port management using Java and MySQL.

### Key Features

- **Smart Port Allocation** - Automatically finds and reserves available ports
- **Conflict Prevention** - Prevents port conflicts before they happen
- **Persistence** - Remembers your allocations across sessions
- **Team Awareness** - Track port usage across team members
- **Auto-cleanup** - Automatically removes stale allocations
- **Rich CLI** - Beautiful command-line interface
- **Logging** - Comprehensive audit trail of all operations

## Architecture

```
PortPeace
├── CLI Application (User Interface)
├── Service Layer (Business Logic)
├── Repository Layer (Data Access)
├── MySQL Database (Persistent Storage)
└── Utilities (Port Scanning, System Info)
```

## Prerequisites

- **Java 11 or higher** - Required to run the application
- **Maven 3.6+** - Required to build the project
- **MySQL 8.0+** - Required for data storage
- **Operating System** - Windows, macOS, or Linux

## Installation

### Step 1: Install MySQL

#### On macOS (using Homebrew):
```bash
brew install mysql
brew services start mysql
```

#### On Ubuntu/Debian:
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
```

#### On Windows:
Download and install from [MySQL Downloads](https://dev.mysql.com/downloads/installer/)

### Step 2: Setup MySQL Database

```sql
-- Connect to MySQL
mysql -u root -p

-- Create database and user
CREATE DATABASE portpeace;
CREATE USER 'portpeace'@'localhost' IDENTIFIED BY 'portpeace';
GRANT ALL PRIVILEGES ON portpeace.* TO 'portpeace'@'localhost';
FLUSH PRIVILEGES;
exit;
```

### Step 3: Clone and Build

```bash
# Clone the repository (or extract the source files)
cd portpeace

# Build with Maven
mvn clean package

# This creates: target/portpeace.jar
```

### Step 4: Create Executable Script

#### On macOS/Linux:

Create a file named `portpeace` (no extension):

```bash
#!/bin/bash
java -jar /path/to/portpeace/target/portpeace.jar "$@"
```

Make it executable:
```bash
chmod +x portpeace
sudo mv portpeace /usr/local/bin/
```

#### On Windows:

Create a file named `portpeace.bat`:

```batch
@echo off
java -jar C:\path\to\portpeace\target\portpeace.jar %*
```

Add the directory containing `portpeace.bat` to your PATH environment variable.

### Step 5: Verify Installation

```bash
portpeace version
portpeace help
```

## Usage Guide

### Basic Commands

#### Allocate a Port

```bash
# Auto-allocate next available port
portpeace alloc frontend

# Allocate specific port
portpeace alloc backend 4000

# Allocate with description
portpeace alloc api 5000 "REST API Service"
```

**Output:**
```
✓ Port allocated successfully!

  Service:     frontend
  Port:        3000
  URL:         http://localhost:3000
  Local URL:   http://frontend.dev:3000
  Status:      ACTIVE

Start your service on port 3000
```

#### List All Allocations

```bash
portpeace list
```

**Output:**
```
Current Port Allocations:
═══════════════════════════════════════════════════════════════════════
SERVICE              PORT     STATUS     USER            LAST USED           
───────────────────────────────────────────────────────────────────────
frontend             3000     ACTIVE     john            2025-01-01 10:30:15
backend              4000     ACTIVE     john            2025-01-01 10:31:22
api                  5000     ACTIVE     sarah           2025-01-01 09:15:43
═══════════════════════════════════════════════════════════════════════
Total: 3 allocation(s)
```

#### Check Status

```bash
# System status
portpeace status

# Service status
portpeace status frontend

# Port status
portpeace status 3000
```

#### Free a Port

```bash
portpeace free frontend
```

**Output:**
```
✓ Port freed successfully for service: frontend
```

#### Cleanup Old Allocations

```bash
# Cleanup allocations older than 7 days (default)
portpeace cleanup

# Cleanup allocations older than 30 days
portpeace cleanup 30
```

### Advanced Usage

#### Integration with Development Scripts

**package.json (Node.js):**
```json
{
  "scripts": {
    "prestart": "portpeace alloc frontend",
    "start": "PORT=$(portpeace status frontend | grep Port | awk '{print $2}') react-scripts start",
    "poststop": "portpeace free frontend"
  }
}
```

**Docker Compose:**
```yaml
services:
  frontend:
    build: ./frontend
    ports:
      - "${FRONTEND_PORT:-3000}:3000"
    command: sh -c "portpeace alloc frontend && npm start"
```

#### Shell Alias for Quick Access

Add to your `.bashrc` or `.zshrc`:
```bash
alias pp='portpeace'
alias ppa='portpeace alloc'
alias ppf='portpeace free'
alias ppl='portpeace list'
```

## 🔧 Configuration

Configuration is stored in `~/.portpeace/config.properties`:

```properties
# Database Configuration
db.host=localhost
db.port=3306
db.database=portpeace
db.username=portpeace
db.password=portpeace
```

Edit this file to customize your database connection.

## Project Structure

```
portpeace/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── portpeace/
│   │   │           ├── PortPeaceApplication.java    # Main entry point
│   │   │           ├── cli/
│   │   │           │   └── CommandLineInterface.java # CLI handler
│   │   │           ├── config/
│   │   │           │   └── DatabaseConfig.java      # DB configuration
│   │   │           ├── model/
│   │   │           │   └── PortAllocation.java      # Data model
│   │   │           ├── repository/
│   │   │           │   └── PortAllocationRepository.java # Data access
│   │   │           ├── service/
│   │   │           │   └── PortManagementService.java    # Business logic
│   │   │           └── util/
│   │   │               ├── PortScanner.java         # Port utilities
│   │   │               └── SystemUtils.java         # System utilities
│   │   └── resources/
│   │       └── logback.xml                          # Logging config
│   └── test/
│       └── java/
│           └── com/
│               └── portpeace/
│                   └── ...                          # Unit tests
├── pom.xml                                          # Maven configuration
└── README.md                                        # This file
```

## Database Schema

### port_allocations
| Column | Type | Description |
|--------|------|-------------|
| id | INT | Primary key |
| service_name | VARCHAR(255) | Service identifier (unique) |
| port_number | INT | Allocated port |
| username | VARCHAR(255) | User who allocated |
| hostname | VARCHAR(255) | Machine hostname |
| status | ENUM | ACTIVE, INACTIVE, RESERVED |
| allocated_at | TIMESTAMP | When allocated |
| last_used_at | TIMESTAMP | Last usage time |
| description | TEXT | Optional description |

### port_history
| Column | Type | Description |
|--------|------|-------------|
| id | INT | Primary key |
| service_name | VARCHAR(255) | Service identifier |
| port_number | INT | Port involved |
| username | VARCHAR(255) | User involved |
| hostname | VARCHAR(255) | Machine hostname |
| action | ENUM | ALLOCATED, FREED, CONFLICT, AUTO_CLEANED |
| timestamp | TIMESTAMP | When action occurred |
| details | TEXT | Additional details |

### user_preferences
| Column | Type | Description |
|--------|------|-------------|
| id | INT | Primary key |
| username | VARCHAR(255) | Username |
| hostname | VARCHAR(255) | Machine hostname |
| preferred_port_range_start | INT | Preferred range start (default: 3000) |
| preferred_port_range_end | INT | Preferred range end (default: 9999) |
| created_at | TIMESTAMP | Creation time |
| updated_at | TIMESTAMP | Last update time |

## Troubleshooting

### Cannot connect to database

**Error:** `Database Error: Communications link failure`

**Solution:**
1. Ensure MySQL is running: `mysql -u root -p`
2. Check credentials in `~/.portpeace/config.properties`
3. Verify database exists: `SHOW DATABASES;`
4. Test connection: `mysql -u portpeace -p portpeace`

### Port already in use

**Error:** `Port 3000 is already allocated to service 'frontend'`

**Solution:**
```bash
# Free the existing allocation
portpeace free frontend

# Or use a different port
portpeace alloc frontend 3001
```

### Permission denied

**Error:** `Permission denied when binding to port 80`

**Solution:**
Ports 1-1023 require admin/root privileges. Use ports 3000+ or run with sudo.

### Maven build fails

**Error:** `Could not resolve dependencies`

**Solution:**
```bash
# Clear Maven cache and rebuild
mvn clean
rm -rf ~/.m2/repository/com/portpeace
mvn package
```

## Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=PortScannerTest

# Run with coverage
mvn clean test jacoco:report
```

## Logging

Logs are stored in `~/.portpeace/logs/`:

- `portpeace.log` - Current log file
- `portpeace.YYYY-MM-DD.log` - Daily rotated logs
- Maximum 30 days of logs retained
- Maximum 100MB total size

View logs:
```bash
tail -f ~/.portpeace/logs/portpeace.log
```

## Exit Codes

- `0` - Success
- `1` - Error (database connection, invalid arguments, etc.)

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes and test thoroughly
4. Commit with clear messages: `git commit -m "Add feature X"`
5. Push to your fork: `git push origin feature-name`
6. Submit a pull request

## License

This project is licensed under the MIT License.

## Acknowledgments

- HikariCP for connection pooling
- SLF4J and Logback for logging
- MySQL for reliable data storage

**Made with ❤️ for developers who are tired of port conflicts**
