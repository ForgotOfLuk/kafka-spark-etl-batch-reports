#!/bin/bash

echo "Starting Kafka, Zookeeper, Schema Registry, Spark, and MongoDB..."
cd ..  # Move to the root directory where docker-compose.yml is located
docker-compose build --no-cache
docker-compose up -d zookeeper miniclip_kafka schema-registry spark-master spark-worker mongodb

echo "Waiting for Containers to start..."
sleep 30
