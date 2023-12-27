#!/bin/bash

# Function to set script permissions
set_script_permissions() {
    echo "Setting script permissions..."
    chmod +x ./scripts/*.sh
}

# Detect the operating system
case "$(uname -s)" in
    Linux*|Darwin*)
        set_script_permissions
        ;;
    CYGWIN*|MINGW*|MSYS*)
        # Windows environments with Unix-like shell
        set_script_permissions
        ;;
    *)
        echo "Running on Windows without Unix-like shell, skipping permission setting."
        ;;
esac

# Check Docker status and start Docker services
./scripts/docker_setup.sh

# Compile and assemble the project
sbt clean compile assembly

# Start Kafka, Zookeeper, Schema Registry, Spark, and MongoDB
./scripts/kafka_services_setup.sh

# Setup MongoDB collections
./scripts/mongodb_setup.sh

# Register Avro schemas
./scripts/avro_schema_registration.sh

# Start various services
./scripts/service_startup.sh

echo "Setup complete."