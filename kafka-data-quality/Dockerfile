# Use a base image with Java
FROM openjdk:21-ea-25-jdk

# Set the working directory in the container
WORKDIR /usr/src/app

# Copy the assembled JAR file into the container
COPY ./target/scala-2.12/kafka-data-quality-assembly.jar /usr/src/app

# Run the application
CMD ["java", "-jar", "/usr/src/app/kafka-data-quality-assembly.jar"]
