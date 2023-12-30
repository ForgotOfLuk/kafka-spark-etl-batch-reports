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

echo "Creating Collections in MongoDB..."

# Function to create a collection
function create_collection {
    db=$1
    collection=$2
    ttlSeconds=$3
    isTimeSeries=$4

    if [ "$isTimeSeries" = true ]; then
        # Create a time series collection
        docker exec mongodb mongosh -u root -p example --authenticationDatabase admin --eval \
        "db.createCollection('$collection', {
            timeseries: {
              timeField: 'timestamp' ,
              metaField: 'metadata',
              granularity: 'minutes'
            },
            expireAfterSeconds: $ttlSeconds
        })" $db 2>/dev/null
    else
        # Create a regular collection
        docker exec mongodb mongosh -u root -p example --authenticationDatabase admin --eval \
        "db.createCollection('$collection')" $db 2>/dev/null
    fi
}

# Create the Database and Collections
create_collection "timeseriesAggregations" "dailyUserAggregations" "157680000" false #5 years, regular collection
create_collection "timeseriesAggregations" "minutePurchaseAggregations" "2592000" true #30 days, time series
create_collection "timeseriesAggregations" "minutePurchaseCountryAggregations" "2592000" true #30 days, time series
create_collection "timeseriesAggregations" "minuteMatchAggregations" "2592000" true #30 days, time series
create_collection "timeseriesAggregations" "minuteMatchCountryAggregations" "2592000" true #30 days, time series

echo "Collections Created."


echo "Starting Mock-Data service..."
docker-compose up -d mock-data
sleep 30

echo "Starting Kafka Data Quality service..."
docker-compose up -d kafka-data-quality
sleep 30

echo "Starting Spark Daily Aggregation Service..."
docker-compose up -d spark-daily-aggregation
sleep 30

echo "Starting Spark Minute Purchase Aggregation Service..."
docker-compose up -d spark-minute-purchase-aggregation
sleep 30
echo "Starting Spark Minute Match Aggregation Service..."
docker-compose up -d spark-minute-match-aggregation

echo "Setup complete."
