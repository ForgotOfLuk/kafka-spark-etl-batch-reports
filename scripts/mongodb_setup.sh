#!/bin/bash

echo "Creating Time Series Collections in MongoDB..."

# Function to create a time series collection
function create_time_series_collection {
    db=$1
    collection=$2
    time_field=$3
    ttlSeconds=$4

    docker exec mongodb mongosh -u root -p example --authenticationDatabase admin --eval \
    "db.createCollection('$collection', {
        timeseries: { timeField: '$time_field' },
        expireAfterSeconds: $ttlSeconds
    })" $db 2>/dev/null
}

# Create the Database and Collections
create_time_series_collection "timeseriesAggregations" "dailyUserAggregations" "time" "157680000" #5 years
create_time_series_collection "timeseriesAggregations" "minuteUserAggregations" "time" "2592000" #30 days

echo "Time Series Collections Created."
