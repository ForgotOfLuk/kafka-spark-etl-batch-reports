#!/bin/bash

cd ..

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
