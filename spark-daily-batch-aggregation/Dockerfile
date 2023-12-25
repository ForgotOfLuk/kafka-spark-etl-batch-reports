FROM bitnami/spark:3.2.4

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container at /app
COPY target/scala-2.12/spark-daily-batch-aggregation-assembly.jar /app/spark-daily-batch-aggregation-assembly.jar

# Copy Avro schema files
COPY src/main/resources/avro/*.avsc /app/schemas/

# Command to execute the Spark application with the necessary Kafka and Avro packages
CMD ["spark-submit", "--packages", "org.apache.spark:spark-sql-kafka-0-10_2.12:3.2.4,org.apache.spark:spark-avro_2.12:3.2.4,org.mongodb.spark:mongo-spark-connector_2.12:10.2.1", "--class", "KafkaToMongoAggregatorService", "/app/spark-daily-batch-aggregation-assembly.jar"]
