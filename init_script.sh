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

echo "Starting Kafka, Zookeeper, Schema Registry, Spark, and MongoDB..."
docker-compose build --no-cache #for handling intermediate container issues
docker-compose up -d zookeeper miniclip_kafka schema-registry spark-master spark-worker mongodb


echo "Waiting for Kafka to start..."
sleep 30


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
register_schema "init_validated" "init"
register_schema "init_side_output" "init"
register_schema "match" "match"
register_schema "match_validated" "match"
register_schema "match_side_output" "match"
register_schema "in_app_purchase" "in_app_purchase"
register_schema "in_app_purchase_validated" "in_app_purchase"
register_schema "in_app_purchase_side_output" "in_app_purchase"

echo "Starting Mock-Data service..."
docker-compose up -d mock-data
sleep 30

echo "Starting Kafka Data Quality service..."
docker-compose up -d kafka-data-quality

sleep 30

echo "Starting Spark Data Aggregation service..."
docker-compose up -d spark-daily-batch-aggregation

echo "Setup complete."
