#!/bin/bash

# PortPeace Installation Script
# This script automates the installation of PortPeace

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Main installation function
main() {
    echo "╔════════════════════════════════════════════╗"
    echo "║     PortPeace Installation Script         ║"
    echo "║  Professional Port Management Tool        ║"
    echo "╚════════════════════════════════════════════╝"
    echo ""

    # Check prerequisites
    print_info "Checking prerequisites..."
    
    # Check Java
    if ! command_exists java; then
        print_error "Java is not installed. Please install Java 11 or higher."
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 11 ]; then
        print_error "Java 11 or higher is required. Current version: $JAVA_VERSION"
        exit 1
    fi
    print_info "✓ Java $JAVA_VERSION found"

    # Check Maven
    if ! command_exists mvn; then
        print_error "Maven is not installed. Please install Maven 3.6 or higher."
        exit 1
    fi
    print_info "✓ Maven found"

    # Check MySQL
    if ! command_exists mysql; then
        print_warning "MySQL client not found in PATH"
        print_warning "Please ensure MySQL server is running"
    else
        print_info "✓ MySQL client found"
    fi

    # Build the project
    print_info "Building PortPeace..."
    if mvn clean package -q; then
        print_info "✓ Build successful"
    else
        print_error "Build failed"
        exit 1
    fi

    # Check if JAR was created
    if [ ! -f "target/portpeace.jar" ]; then
        print_error "JAR file not found in target/"
        exit 1
    fi

    # Install command
    print_info "Installing command..."
    
    INSTALL_DIR="/usr/local/bin"
    
    if [ -w "$INSTALL_DIR" ]; then
        # Can write without sudo
        cp target/portpeace.jar "$INSTALL_DIR/portpeace.jar"
        cat > "$INSTALL_DIR/portpeace" << 'EOF'
#!/bin/bash
java -jar /usr/local/bin/portpeace.jar "$@"
EOF
        chmod +x "$INSTALL_DIR/portpeace"
    else
        # Need sudo
        print_warning "Installing to $INSTALL_DIR requires sudo privileges"
        sudo cp target/portpeace.jar "$INSTALL_DIR/portpeace.jar"
        echo '#!/bin/bash' | sudo tee "$INSTALL_DIR/portpeace" > /dev/null
        echo 'java -jar /usr/local/bin/portpeace.jar "$@"' | sudo tee -a "$INSTALL_DIR/portpeace" > /dev/null
        sudo chmod +x "$INSTALL_DIR/portpeace"
    fi
    
    print_info "✓ Command installed to $INSTALL_DIR/portpeace"

    # Setup database (optional)
    echo ""
    read -p "Do you want to setup the MySQL database now? (y/n) " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        setup_database
    else
        print_warning "Skipping database setup"
        print_info "You can setup the database later by running:"
        print_info "  mysql -u root -p < setup.sql"
    fi

    # Verify installation
    echo ""
    print_info "Verifying installation..."
    if command_exists portpeace; then
        VERSION=$(portpeace version 2>&1 | head -n 1 || echo "unknown")
        print_info "✓ Installation successful!"
        print_info "  Version: $VERSION"
    else
        print_error "Installation verification failed"
        print_error "Command 'portpeace' not found in PATH"
        exit 1
    fi

    # Print success message
    echo ""
    echo "╔════════════════════════════════════════════╗"
    echo "║      Installation Complete! 🎉            ║"
    echo "╚════════════════════════════════════════════╝"
    echo ""
    print_info "Quick start:"
    echo "  1. Allocate a port:  portpeace alloc frontend"
    echo "  2. List allocations: portpeace list"
    echo "  3. Check status:     portpeace status frontend"
    echo "  4. Free a port:      portpeace free frontend"
    echo ""
    print_info "Documentation:"
    echo "  - Quick Start: QUICKSTART.md"
    echo "  - Full Guide:  COMPLETE_GUIDE.md"
    echo "  - README:      README.md"
    echo ""
    print_info "Configuration: ~/.portpeace/config.properties"
    print_info "Logs:          ~/.portpeace/logs/portpeace.log"
}

# Setup database function
setup_database() {
    print_info "Setting up MySQL database..."
    
    # Check if setup.sql exists
    if [ ! -f "setup.sql" ]; then
        print_error "setup.sql not found"
        return 1
    fi

    # Get MySQL credentials
    echo ""
    print_info "Please enter MySQL root credentials:"
    read -p "MySQL root user (default: root): " MYSQL_USER
    MYSQL_USER=${MYSQL_USER:-root}
    
    read -sp "MySQL root password: " MYSQL_PASSWORD
    echo ""

    # Run setup script
    if mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" < setup.sql 2>/dev/null; then
        print_info "✓ Database setup complete"
        print_info "  Database: portpeace"
        print_info "  User:     portpeace"
        print_info "  Password: portpeace (change in production!)"
    else
        print_error "Database setup failed"
        print_warning "Please setup database manually:"
        print_info "  mysql -u root -p < setup.sql"
        return 1
    fi
}

# Run main installation
main
