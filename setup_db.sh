#!/bin/bash
set -e

echo "=============================================="
echo "   Expenso PostgreSQL Database Setup Tool"
echo "=============================================="

# 1. Create database 'expenso' if it doesn't exist
echo "Checking/creating 'expenso' database..."
if sudo -u postgres psql -lqt | cut -d \| -f 1 | grep -qw expenso; then
    echo "  - Database 'expenso' already exists. Dropping it for a clean slate..."
    sudo -u postgres psql -c "SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = 'expenso' AND pid <> pg_backend_pid();"
    sudo -u postgres psql -c "DROP DATABASE expenso;"
fi
echo "  - Creating database 'expenso'..."
sudo -u postgres psql -c "CREATE DATABASE expenso;"

# 2. Create user 'expenso_user' if it doesn't exist
echo "Checking/creating 'expenso_user' role..."
USER_EXISTS=$(sudo -u postgres psql -tAc "SELECT 1 FROM pg_roles WHERE rolname='expenso_user'")
if [ "$USER_EXISTS" = "1" ]; then
    echo "  - User 'expenso_user' already exists. Updating password..."
    sudo -u postgres psql -c "ALTER ROLE expenso_user WITH PASSWORD 'secure_password';"
else
    echo "  - Creating user 'expenso_user' with password 'secure_password'..."
    sudo -u postgres psql -c "CREATE ROLE expenso_user WITH LOGIN PASSWORD 'secure_password';"
fi

# 3. Grant privileges
echo "Granting privileges on 'expenso' database to 'expenso_user'..."
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE expenso TO expenso_user;"
sudo -u postgres psql -d expenso -c "GRANT ALL ON SCHEMA public TO expenso_user;"
sudo -u postgres psql -d expenso -c "ALTER SCHEMA public OWNER TO expenso_user;"
sudo -u postgres psql -d expenso -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO expenso_user;"
sudo -u postgres psql -d expenso -c "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO expenso_user;"

echo "=============================================="
echo "   Database setup completed successfully!"
echo "=============================================="
