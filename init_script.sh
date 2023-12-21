#!/bin/bash

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    echo "Docker is not running. Please start Docker and rerun the script."
    exit 1
fi

echo "Deleting docker containers in case they already exist..."
docker compose down

echo "Compiling and assembling project..."
sbt clean compile assembly

echo "Starting Kafka, Zookeeper and Schema Registry..."
docker-compose build --no-cache #for handling intermediate container issues
docker-compose up -d zookeeper miniclip_kafka schema-registry

echo "Waiting for Kafka to start..."
sleep 30

echo "Creating Input Kafka topics..."
docker exec -it miniclip_kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic init
docker exec -it miniclip_kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic init_validated
docker exec -it miniclip_kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic init_side_output
docker exec -it miniclip_kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic match
docker exec -it miniclip_kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic match_validated
docker exec -it miniclip_kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic match_side_output
docker exec -it miniclip_kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic in_app_purchase
docker exec -it miniclip_kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic in_app_purchase_validated
docker exec -it miniclip_kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic in_app_purchase_side_output

echo "Creating GlobalKTable topics..."
docker exec -it miniclip_kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic platforms_topic
docker exec -it miniclip_kafka kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic countries_topic

echo "Registering Avro schemas with the Schema Registry..."

function register_schema {
    topic=$1
    name=$2
    schema_file="common/src/main/scala/common/model/schemas/avro/${name}.avsc"
    schema=$(jq -c . < "$schema_file" | jq -s -R -r @json)
    curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
         --data "{\"schema\": $schema}" \
         http://localhost:8081/subjects/"${topic}"-value/versions
    echo " - Schema for ${topic} registered."
}

register_schema "init" "init"
register_schema "init-validated" "init"
register_schema "match" "match"
register_schema "match-validated" "match"
register_schema "in_app_purchase" "in_app_purchase"
register_schema "in_app_purchase-validated" "in_app_purchase"

echo "Starting Mock-Data service..."
docker-compose up -d mock-data
sleep 30

echo "Starting Kafka Data Quality service..."
docker-compose up -d kafka-data-quality

echo "Setup complete."
