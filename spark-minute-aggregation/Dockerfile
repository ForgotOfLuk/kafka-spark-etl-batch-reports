FROM bitnami/spark:3.2.4

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container at /app
COPY target/scala-2.12/spark-minute-aggregation-assembly.jar /app/spark-minute-aggregation-assembly.jar

RUN mkdir -p /app/state && chmod -R 777 /app/state
RUN mkdir -p /app/data && chmod -R 777 /app/data
RUN mkdir -p /app/data/checkpoint && chmod -R 777 /app/data/checkpoint

# Command to execute the Spark application with the necessary Kafka and Avro packages
CMD ["spark-submit", "--packages", "org.apache.spark:spark-sql-kafka-0-10_2.12:3.2.4,org.mongodb.spark:mongo-spark-connector_2.12:10.2.1", "--class", "SparkMinuteAggregatorService", "--driver-memory", "2g", "--executor-memory", "2g", "/app/spark-minute-aggregation-assembly.jar"]
