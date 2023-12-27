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


echo "Waiting for Containers to start..."
sleep 30

echo "Creating Time Series Collections in MongoDB..."

# Function to create a time series collection
function create_time_series_collection {
    db=$1
    collection=$2
    time_field=$3
    ttlSeconds=$4

    docker exec mongodb mongo --eval \
    "db.createCollection('$collection', {
        timeseries: { timeField: '$time_field' },
        expireAfterSeconds: $ttlSeconds   # Optional: TTL for data (5 year in this case)
    })" $db
}

# Create the Database and Collections
create_time_series_collection "timeseriesAggregations" "dailyUserAggregations" "time" "157680000" #5 years
create_time_series_collection "timeseriesAggregations" "minuteUserAggregations" "time" "2592000" #30 days

echo "Time Series Collections Created."

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
register_schema "init_side_output" "init"
register_schema "match" "match"
register_schema "match_side_output" "match"
register_schema "in_app_purchase" "in_app_purchase"
register_schema "in_app_purchase_side_output" "in_app_purchase"

echo "Starting Mock-Data service..."
docker-compose up -d mock-data
sleep 30

echo "Starting Kafka Data Quality service..."
docker-compose up -d kafka-data-quality
sleep 30

echo "Starting Spark Daily Aggregation Service..."
docker-compose up -d spark-daily-aggregation
sleep 30

echo "Starting Spark Minute Aggregation Service..."
docker-compose up -d spark-minute-aggregation

echo "Setup complete."
