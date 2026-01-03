# PortPeace Quick Start Guide

## 5-Minute Setup

### Prerequisites Check

```bash
# Check Java (needs 11+)
java -version

# Check Maven (needs 3.6+)
mvn -version

# Check MySQL (needs 8.0+)
mysql --version
```

If any are missing, install them first.

---

## Step 1: Setup MySQL (2 minutes)

```bash
# Start MySQL
# macOS: brew services start mysql
# Linux: sudo systemctl start mysql
# Windows: net start MySQL80

# Create database and user
mysql -u root -p
```

Then run these SQL commands:

```sql
CREATE DATABASE portpeace;
CREATE USER 'portpeace'@'localhost' IDENTIFIED BY 'portpeace';
GRANT ALL PRIVILEGES ON portpeace.* TO 'portpeace'@'localhost';
FLUSH PRIVILEGES;
exit;
```

---

## Step 2: Build PortPeace (1 minute)

```bash
# Navigate to project directory
cd portpeace

# Build the JAR file
mvn clean package

# Result: target/portpeace.jar
```

---

## Step 3: Create Command (1 minute)

### macOS/Linux:

```bash
# Create executable script
echo '#!/bin/bash' > portpeace
echo 'java -jar '$(pwd)'/target/portpeace.jar "$@"' >> portpeace
chmod +x portpeace
sudo mv portpeace /usr/local/bin/
```

### Windows:

Create `portpeace.bat` in a directory that's in your PATH:

```batch
@echo off
java -jar C:\path\to\portpeace\target\portpeace.jar %*
```

---

## Step 4: Test It! (1 minute)

```bash
# Check version
portpeace version

# Allocate your first port
portpeace alloc frontend

# See the allocation
portpeace list

# Free it
portpeace free frontend
```

---

## Common First-Time Issues

### "Cannot connect to database"

MySQL isn't running. Start it:

```bash
# macOS
brew services start mysql

# Linux
sudo systemctl start mysql

# Windows
net start MySQL80
```

### "Command not found: portpeace"

Your shell can't find the command. Either:

1. Use the full path: `/usr/local/bin/portpeace`
2. Reload your shell: `source ~/.bashrc` or restart terminal
3. Check PATH includes the directory

### "Port already in use"

That's actually good! PortPeace detected a conflict. Try a different port:

```bash
portpeace alloc frontend 3001
```

---

## Your First Workflow

### Allocate ports for your project:

```bash
portpeace alloc frontend 3000 "React frontend"
portpeace alloc backend 4000 "Express API"
portpeace alloc database 5432 "PostgreSQL"
```

### Start your services:

```bash
# Frontend on port 3000
cd frontend && npm start

# Backend on port 4000
cd backend && npm run dev

# Database on port 5432
docker run -p 5432:5432 postgres
```

### Check status anytime:

```bash
portpeace list
portpeace status frontend
portpeace status 3000
```

### Clean up when done:

```bash
portpeace free frontend
portpeace free backend
# or cleanup all old allocations
portpeace cleanup
```

---

## Next Steps

- Read the full [README.md](README.md) for advanced features
- Set up shell aliases for faster access
- Integrate with your project scripts
- Share with your team!

---

## Still Having Issues?

1. Check logs: `cat ~/.portpeace/logs/portpeace.log`
2. Verify database: `mysql -u portpeace -p portpeace`
3. Test connection: `portpeace status`
4. Check configuration: `cat ~/.portpeace/config.properties`

---

**You're all set! Happy coding! 🚀**
