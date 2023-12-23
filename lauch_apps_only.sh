#!/bin/bash

echo "Starting Mock-Data service..."
docker-compose up -d mock-data
sleep 30

echo "Starting Kafka Data Quality service..."
docker-compose up -d kafka-data-quality

echo "Setup complete."
