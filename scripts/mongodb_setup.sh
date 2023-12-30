#!/bin/bash

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
