#!/bin/bash

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    echo "Docker is not running. Please start Docker and rerun the script."
    exit 1
fi

# Start Kafka, Zookeeper, and Schema Registry
echo "Starting Kafka, Zookeeper, and Schema Registry..."
docker-compose up -d zookeeper miniclip_kafka schema-registry

# Wait for Kafka to start
echo "Waiting for Kafka to start..."
sleep 30

# Create Kafka topics
echo "Creating Input Kafka topics..."
docker exec -it miniclip_kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic init
docker exec -it miniclip_kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic match
docker exec -it miniclip_kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic in-app-purchase

# Register Avro schemas with the Schema Registry
echo "Registering Avro schemas with the Schema Registry..."
SCHEMA_REGISTRY_URL=http://localhost:8081

function register_schema {
    topic=$1
    schema_file="common/src/main/scala/common/model/schemas/avro/${topic}.avsc"
    schema=$(jq -c . < $schema_file | jq -s -R -r @json)
    curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
         --data "{\"schema\": $schema}" \
         http://localhost:8081/subjects/${topic}-value/versions
    echo " - Schema for ${topic} registered."
}

register_schema "init"
register_schema "match"
register_schema "in_app_purchase"

# Wait for Schema Registry to register schemas
echo "Waiting for Schema Registry..."
sleep 10

# Build and run Scala applications (assuming Dockerfiles are present in the Scala app directories)
#echo "Building and running Scala applications..."
#cd path/to/scala-mock-data-app
#docker build -t scala-mock-data-app .
#docker run -d --network=your-docker-compose-network-name scala-mock-data-app

#cd path/to/scala-data-processing-app
#docker build -t scala-data-processing-app .
#docker run -d --network=your-docker-compose-network-name scala-data-processing-app

echo "Setup complete."
