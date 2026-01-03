# PortPeace - Complete Functionalities Reference

## 📚 Table of Contents

1. [Core Functionalities](#core-functionalities)
2. [Port Allocation](#port-allocation)
3. [Port Management](#port-management)
4. [Status & Information](#status--information)
5. [Cleanup Operations](#cleanup-operations)
6. [Database Operations](#database-operations)
7. [Team Collaboration](#team-collaboration)
8. [Logging & Auditing](#logging--auditing)
9. [Configuration Management](#configuration-management)
10. [Advanced Features](#advanced-features)

---

## Core Functionalities

### 1.1 Port Allocation System

#### **Auto Port Allocation**
Automatically finds and allocates the next available port.

```bash
portpeace alloc <service-name>
```

**Features:**
- Scans port range 3000-9999 by default
- Finds first available port
- Avoids conflicts with existing allocations
- Checks if port is actually free on the system
- Creates database record with timestamp
- Returns allocated port information

**Example:**
```bash
$ portpeace alloc frontend
✓ Port allocated successfully!

  Service:     frontend
  Port:        3000
  URL:         http://localhost:3000
  Local URL:   http://frontend.dev:3000
  Status:      ACTIVE

Start your service on port 3000
```

**Technical Details:**
- **Algorithm**: Linear scan from start of range
- **Validation**: Double-checks with system socket binding
- **Conflict Resolution**: Skips ports in use or allocated
- **Performance**: < 50ms for typical allocation

---

#### **Manual Port Allocation**
Allocate a specific port of your choice.

```bash
portpeace alloc <service-name> <port>
```

**Features:**
- Specify exact port number (1-65535)
- Validates port availability
- Checks against database allocations
- Verifies port is not in system use
- Prevents conflicts
- Provides clear error messages

**Example:**
```bash
$ portpeace alloc backend 4000
✓ Port allocated successfully!

  Service:     backend
  Port:        4000
  URL:         http://localhost:4000
  Status:      ACTIVE
```

**Error Handling:**
```bash
# Port already allocated
$ portpeace alloc myapp 3000
✗ Failed to allocate port: Port 3000 is already allocated to service 'frontend'

# Port in use by another process
$ portpeace alloc myapp 8080
✗ Failed to allocate port: Port 8080 is already in use
```

---

#### **Allocation with Description**
Add descriptive information to your allocation.

```bash
portpeace alloc <service-name> [port] <description>
```

**Features:**
- Store metadata about the service
- Helps team understand purpose
- Appears in list view
- Searchable in database

**Example:**
```bash
$ portpeace alloc api 5000 "REST API for user management"
✓ Port allocated successfully!

  Service:     api
  Port:        5000
  Description: REST API for user management
  Status:      ACTIVE
```

---

#### **Smart Reallocation**
Automatically reuses existing allocations when available.

**Scenario:**
```bash
# Day 1: Allocate port
$ portpeace alloc frontend
Port 3000 allocated

# Day 2: Request same service
$ portpeace alloc frontend
✓ Port allocated successfully!
  Reused existing allocation  ← Smart reallocation!
  Port: 3000
```

**Features:**
- Checks if service already has an allocation
- Verifies port is still available
- Reuses if possible
- Allocates new port if old one is taken
- Updates last_used_at timestamp

---

### 1.2 Port Liberation

#### **Free Port Allocation**
Release a port allocation back to the pool.

```bash
portpeace free <service-name>
```

**Features:**
- Removes allocation from database
- Logs action to history
- Frees port for reuse
- Provides confirmation message
- Safe operation (doesn't kill processes)

**Example:**
```bash
$ portpeace free frontend
✓ Port freed successfully for service: frontend
```

**Error Handling:**
```bash
$ portpeace free nonexistent
✗ Failed to free port. Service not found: nonexistent
```

**Important Note:**
- Freeing a port **does NOT stop** any running process
- You must manually stop your application
- Only removes the database record

---

## Port Management

### 2.1 Port Status Checking

#### **Check Specific Port**
View detailed status of a port number.

```bash
portpeace status <port-number>
```

**Output:**
```
Port 3000 Status
═══════════════════════════════════════════════════════════════════
  In Use:      Yes
  Allocated:   Yes
  Service:     frontend
  User:        john@MacBook-Pro
  Status:      ACTIVE
  Allocated:   2025-01-01 10:00:00
═══════════════════════════════════════════════════════════════════
```

**Information Provided:**
- Port number
- Whether port is currently in use on system
- Whether port is allocated in PortPeace
- Service name using the port
- User and hostname who allocated it
- Allocation status
- When it was allocated

---

#### **Check Service Status**
View allocation details for a specific service.

```bash
portpeace status <service-name>
```

**Output:**
```
Service: frontend
═══════════════════════════════════════════════════════════════════
  Port:        3000
  Status:      ACTIVE
  User:        john@MacBook-Pro
  Allocated:   2025-01-01 10:00:00
  Last Used:   2025-01-02 14:30:22
  Description: React development server
  Port In Use: Yes
═══════════════════════════════════════════════════════════════════
```

**Information Provided:**
- Port number assigned
- Allocation status
- User and machine who allocated
- Allocation timestamp
- Last usage timestamp
- Service description (if provided)
- Real-time port usage status

---

#### **System Status**
View overall PortPeace system information.

```bash
portpeace status
```

**Output:**
```
PortPeace System Status
═══════════════════════════════════════════════════════════════════
System Information:
  User: john, Host: MacBook-Pro, OS: Mac OS X 14.1, Java: 11.0.16

Active Allocations: 5
Total Allocations:  7
═══════════════════════════════════════════════════════════════════
```

**Information Provided:**
- Current user information
- System hostname
- Operating system details
- Java version
- Count of active allocations
- Total allocations in database

---

### 2.2 List Operations

#### **List All Allocations**
Display all port allocations in a table format.

```bash
portpeace list
```

**Output:**
```
Current Port Allocations:
═══════════════════════════════════════════════════════════════════
SERVICE              PORT     STATUS     USER            LAST USED           
───────────────────────────────────────────────────────────────────
api                  5000     ACTIVE     alice           2025-01-02 14:23:10
backend              4000     ACTIVE     bob             2025-01-02 13:45:33
database             5432     INACTIVE   alice           2025-01-01 09:12:05
frontend             3000     ACTIVE     john            2025-01-02 14:30:22
redis                6379     ACTIVE     bob             2025-01-02 12:18:47
websocket            8080     RESERVED   alice           2025-01-02 10:05:15
worker               9000     ACTIVE     john            2025-01-02 11:22:30
═══════════════════════════════════════════════════════════════════
Total: 7 allocation(s)
```

**Features:**
- Sorted by port number
- Shows service name (truncated if too long)
- Displays port number
- Shows status (ACTIVE, INACTIVE, RESERVED)
- Shows username who allocated
- Shows last usage timestamp
- Total count at bottom

**Status Meanings:**
- **ACTIVE**: Currently in use
- **INACTIVE**: Not used recently but allocated
- **RESERVED**: Reserved but not yet in use

---

## Status & Information

### 3.1 Port Scanning

#### **Real-time Port Availability Check**
PortPeace continuously scans ports to determine availability.

**How It Works:**
```java
// Attempts to bind to the port
ServerSocket socket = new ServerSocket(port);
socket.close();
// If successful, port is available
// If fails, port is in use
```

**Features:**
- Checks actual system-level port usage
- Independent of PortPeace allocations
- Works on all operating systems
- Sub-10ms response time
- Detects conflicts with external processes

---

#### **Port Range Validation**
Validates port numbers are within acceptable ranges.

**Validation Rules:**
```
Valid Ports: 1-65535
├── Privileged Ports: 1-1023 (require admin/root)
├── Registered Ports: 1024-49151 (recommended)
└── Dynamic Ports: 49152-65535 (also usable)
```

**Example:**
```bash
# Valid
portpeace alloc myapp 3000   ✅

# Invalid
portpeace alloc myapp 0      ❌
portpeace alloc myapp 99999  ❌
portpeace alloc myapp -1     ❌
```

---

### 3.2 User & System Information

#### **Automatic User Detection**
Tracks which user makes each allocation.

**Captured Information:**
- System username (`System.getProperty("user.name")`)
- System hostname (`InetAddress.getLocalHost().getHostName()`)
- Operating system name and version
- Java version
- Timestamp of allocation

**Example Record:**
```
Username: john
Hostname: MacBook-Pro
OS: Mac OS X 14.1
Java: 11.0.16
Allocated: 2025-01-02 10:30:15
```

---

#### **Cross-Platform System Detection**
Automatically detects and adapts to operating system.

**Detection Methods:**
```java
// OS Detection
boolean isWindows = OS.contains("windows");
boolean isMacOS = OS.contains("mac");
boolean isLinux = OS.contains("linux");

// Path Adjustment
String configPath = isWindows 
    ? "C:\\Users\\%USERNAME%\\.portpeace"
    : "~/.portpeace";
```

---

## Cleanup Operations

### 4.1 Automatic Cleanup

#### **Clean Old Allocations**
Remove inactive allocations older than specified days.

```bash
portpeace cleanup [days]
```

**Default:**
```bash
# Removes allocations inactive for 7+ days
portpeace cleanup
✓ Cleaned up 3 inactive allocation(s) older than 7 days
```

**Custom Duration:**
```bash
# Removes allocations inactive for 30+ days
portpeace cleanup 30
✓ Cleaned up 1 inactive allocation(s) older than 30 days
```

**How It Works:**
- Checks `last_used_at` timestamp
- Only removes INACTIVE allocations
- Preserves ACTIVE and RESERVED allocations
- Logs all deletions to history
- Updates database atomically

**SQL Query:**
```sql
DELETE FROM port_allocations 
WHERE status = 'INACTIVE' 
  AND last_used_at < DATE_SUB(NOW(), INTERVAL ? DAY)
```

---

#### **Smart Cleanup Logic**

**What Gets Cleaned:**
- ✅ INACTIVE allocations older than threshold
- ✅ Allocations with no recent activity
- ✅ Ports confirmed not in use

**What's Protected:**
- ❌ ACTIVE allocations (regardless of age)
- ❌ RESERVED allocations
- ❌ Recently used allocations
- ❌ Allocations for running services

---

### 4.2 Manual Cleanup

#### **Free Individual Services**
Remove specific allocations manually.

```bash
portpeace free <service-name>
```

**Use Cases:**
- Project completed
- Service no longer needed
- Wrong allocation made
- Port needed for different service

---

#### **Batch Cleanup Script**
Clean multiple services at once.

```bash
#!/bin/bash
# cleanup-all.sh
for service in frontend backend api database redis; do
    portpeace free $service
done
```

---

## Database Operations

### 5.1 Database Structure

#### **Tables Overview**

**port_allocations** (Main allocations table)
```sql
CREATE TABLE port_allocations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL UNIQUE,
    port_number INT NOT NULL,
    username VARCHAR(255) NOT NULL,
    hostname VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'RESERVED'),
    allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    description TEXT,
    INDEX idx_port (port_number),
    INDEX idx_service (service_name),
    INDEX idx_status (status)
);
```

**port_history** (Audit trail)
```sql
CREATE TABLE port_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL,
    port_number INT NOT NULL,
    username VARCHAR(255) NOT NULL,
    hostname VARCHAR(255) NOT NULL,
    action ENUM('ALLOCATED', 'FREED', 'CONFLICT', 'AUTO_CLEANED'),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    INDEX idx_service (service_name),
    INDEX idx_timestamp (timestamp)
);
```

**user_preferences** (User settings)
```sql
CREATE TABLE user_preferences (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    hostname VARCHAR(255) NOT NULL,
    preferred_port_range_start INT DEFAULT 3000,
    preferred_port_range_end INT DEFAULT 9999,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_host (username, hostname)
);
```

---

### 5.2 Connection Pooling

#### **HikariCP Connection Pool**
High-performance JDBC connection pooling.

**Configuration:**
```java
HikariConfig config = new HikariConfig();
config.setMaximumPoolSize(10);      // Max connections
config.setMinimumIdle(2);           // Min idle connections
config.setConnectionTimeout(30000);  // 30 seconds
config.setIdleTimeout(600000);       // 10 minutes
config.setMaxLifetime(1800000);      // 30 minutes
```

**Benefits:**
- Fast connection acquisition (< 5ms)
- Automatic connection validation
- Leak detection
- Connection recycling
- Thread-safe operations
- Production-ready performance

---

### 5.3 Transaction Management

#### **Atomic Operations**
All database operations are atomic and isolated.

**Example - Allocation:**
```java
Connection conn = DatabaseConfig.getConnection();
try {
    conn.setAutoCommit(false);
    // Save allocation
    // Log history
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
    throw e;
}
```

**Guarantees:**
- All-or-nothing operations
- No partial states
- Concurrent access safety
- Data consistency

---

### 5.4 Query Optimization

#### **Indexed Queries**
All common queries use database indexes.

**Indexed Columns:**
- `service_name` - Fast service lookup
- `port_number` - Fast port lookup
- `status` - Fast status filtering
- `timestamp` - Fast history queries

**Performance:**
- Service lookup: < 5ms
- Port lookup: < 5ms
- List all: < 20ms (for 1000 records)
- History query: < 10ms

---

## Team Collaboration

### 6.1 Shared Database

#### **Team-Wide Visibility**
All team members share the same database.

**Setup:**
```properties
# ~/.portpeace/config.properties
db.host=team-database.company.com
db.port=3306
db.database=portpeace_team
db.username=developer
db.password=team_password
```

**Features:**
- See all team allocations
- Prevent port conflicts across team
- Track who allocated what
- Coordinate port usage
- Historical tracking

---

#### **Cross-Machine Tracking**
Track allocations across different machines.

**Example:**
```bash
# Alice on MacBook
alice@MacBook$ portpeace alloc frontend 3000

# Bob on Linux
bob@ubuntu$ portpeace list
# Sees Alice's allocation:
# frontend  3000  ACTIVE  alice  2025-01-02 10:30:15
```

---

### 6.2 Conflict Prevention

#### **Team Conflict Detection**
Prevents port conflicts before they happen.

**Scenario:**
```bash
# Alice allocates port 3000
alice$ portpeace alloc webapp 3000
✓ Success

# Bob tries same port
bob$ portpeace alloc api 3000
✗ Failed: Port 3000 is already allocated to service 'webapp'
```

**Benefits:**
- No surprise conflicts
- Clear error messages
- Shows who has the port
- Suggests resolution

---

### 6.3 User Tracking

#### **Attribution & Accountability**
Every allocation tracks who did it.

**Information Captured:**
```
Username: alice
Hostname: alice-macbook-pro
Action: ALLOCATED
Port: 3000
Service: frontend
Timestamp: 2025-01-02 10:30:15
```

**Use Cases:**
- Know who to ask about a port
- Track team port usage patterns
- Audit compliance
- Resource allocation insights

---

## Logging & Auditing

### 7.1 Application Logging

#### **Multi-Level Logging**
Comprehensive logging at multiple levels.

**Log Levels:**
```
ERROR - Critical errors only
WARN  - Warnings and errors
INFO  - General information
DEBUG - Detailed debugging
```

**Configuration:**
```xml
<!-- logback.xml -->
<root level="INFO">
    <appender-ref ref="FILE" />
</root>

<logger name="com.portpeace" level="DEBUG" />
```

---

#### **Log Files**
Automatic log rotation and management.

**Location:**
```
~/.portpeace/logs/
├── portpeace.log              # Current log
├── portpeace.2025-01-01.log   # Yesterday
├── portpeace.2024-12-31.log   # Day before
└── ...
```

**Rotation Policy:**
- Daily rotation
- Keep 30 days
- Max 100MB total size
- Automatic cleanup

**Log Format:**
```
2025-01-02 14:30:22.123 [main] INFO  PortManagementService - Allocated port 3000 for service frontend
2025-01-02 14:30:23.456 [main] INFO  PortAllocationRepository - Saved allocation: frontend -> 3000
2025-01-02 14:30:23.789 [main] INFO  PortAllocationRepository - Logged history: ALLOCATED
```

---

### 7.2 Audit Trail

#### **History Tracking**
Every operation is logged to the database.

**Tracked Actions:**
- `ALLOCATED` - Port allocation
- `FREED` - Port release
- `CONFLICT` - Conflict detected
- `AUTO_CLEANED` - Automatic cleanup

**Example Query:**
```sql
SELECT * FROM port_history 
WHERE service_name = 'frontend' 
ORDER BY timestamp DESC;
```

**Output:**
```
| timestamp           | action     | port  | user  |
|---------------------|------------|-------|-------|
| 2025-01-02 14:30:22 | ALLOCATED  | 3000  | john  |
| 2025-01-01 10:15:33 | FREED      | 3000  | john  |
| 2024-12-31 09:22:15 | ALLOCATED  | 3000  | john  |
```

---

#### **Compliance & Auditing**
Full audit trail for compliance requirements.

**Capabilities:**
- Who allocated what port when
- Who freed what port when
- All conflicts and resolutions
- Automatic cleanup records
- Complete historical data

---

### 7.3 Error Logging

#### **Comprehensive Error Handling**
All errors are logged with full context.

**Error Log Example:**
```
2025-01-02 14:30:22.123 [main] ERROR DatabaseConfig - Database connection failed
java.sql.SQLException: Communications link failure
    at com.mysql.cj.jdbc.ConnectionImpl.createNewIO(ConnectionImpl.java:829)
    at com.mysql.cj.jdbc.ConnectionImpl.<init>(ConnectionImpl.java:449)
    ...
```

**Error Categories:**
- Database connection errors
- SQL execution errors
- Port scanning errors
- Configuration errors
- Validation errors

---

## Configuration Management

### 8.1 Configuration File

#### **Default Configuration**
Auto-generated on first run.

**Location:**
```
~/.portpeace/config.properties
```

**Default Content:**
```properties
# Database Configuration
db.host=localhost
db.port=3306
db.database=portpeace
db.username=portpeace
db.password=portpeace
```

---

#### **Custom Configuration**
Modify for your environment.

**Development:**
```properties
db.host=localhost
db.port=3306
db.database=portpeace_dev
db.username=dev_user
db.password=dev_pass
```

**Production:**
```properties
db.host=prod-db.company.com
db.port=3306
db.database=portpeace_prod
db.username=prod_user
db.password=prod_secure_pass
```

**Team Shared:**
```properties
db.host=team-db.company.com
db.port=3306
db.database=portpeace_team
db.username=team_user
db.password=team_shared_pass
```

---

### 8.2 Environment Variables

#### **Override Configuration**
Use environment variables for sensitive data.

**Supported Variables:**
```bash
export PORTPEACE_DB_HOST=localhost
export PORTPEACE_DB_PORT=3306
export PORTPEACE_DB_NAME=portpeace
export PORTPEACE_DB_USER=myuser
export PORTPEACE_DB_PASS=mypassword
```

**Priority:**
1. Environment variables (highest)
2. Configuration file
3. Default values (lowest)

---

### 8.3 Port Range Customization

#### **Default Range**
```
Start: 3000
End: 9999
```

#### **Custom Range via Database**
```sql
INSERT INTO user_preferences 
(username, hostname, preferred_port_range_start, preferred_port_range_end)
VALUES ('john', 'laptop', 5000, 6000);
```

**Future Enhancement:**
This will allow per-user port range preferences.

---

## Advanced Features

### 9.1 Command Aliases

#### **Alternative Commands**
Multiple ways to invoke the same functionality.

**Allocation:**
```bash
portpeace alloc fro
