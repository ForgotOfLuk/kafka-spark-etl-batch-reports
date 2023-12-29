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
    meta_field=$4
    ttl_seconds=$5

    docker exec mongodb mongosh -u root -p example --authenticationDatabase admin --eval \
    "db.createCollection('$collection', {
        timeseries: {
            timeField: '$time_field'
            metaField: '$meta_field'
          },
        expireAfterSeconds: $ttl_seconds
    })" $db 2>/dev/null
}

# Create the Database and Collections
create_time_series_collection "timeseriesAggregations" "dailyUserAggregations" "timestamp" "userData" "157680000" #5 years
create_time_series_collection "timeseriesAggregations" "minuteUserAggregations" "timestamp" "2592000" #30 days

echo "Time Series Collections Created."

echo "Starting Mock-Data service..."
docker-compose up -d mock-data
sleep 90

echo "Starting Kafka Data Quality service..."
docker-compose up -d kafka-data-quality
sleep 90

echo "Starting Spark Daily Aggregation Service..."
docker-compose up -d spark-daily-aggregation
sleep 120

echo "Starting Spark Minute Aggregation Service..."
docker-compose up -d spark-minute-aggregation

echo "Setup complete."
