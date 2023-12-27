#!/bin/bash
cd ..

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
