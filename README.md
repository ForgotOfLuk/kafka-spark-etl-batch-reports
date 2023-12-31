# Miniclip Data Engineering Project

## Prerequisites
- [SBT](https://www.scala-sbt.org/): Scala Build Tool
- [Java Runtime Environment (JRE) 21](https://adoptium.net/)
- [Docker](https://www.docker.com/)

## Optional Prerequisites:
- [Mongo Compass](https://www.mongodb.com/try/download/compass)
  - or a similar app to view databases

## Getting Started
- Run the following script to set up and start the project:

   ```bash
   /bin/bash /path/to/Miniclip/init_script.sh
   

This script performs the following tasks:

Checks if Docker is running and prompts you to start it if necessary.
Deletes any existing Docker containers.
Compiles and assembles the project using SBT.
Starts Kafka, Zookeeper, and Schema Registry in Docker containers.
Creates Kafka topics for input and GlobalKTable topics.
Registers Avro schemas with the Schema Registry.
Starts the Mock-Data service and Kafka Data Quality service.
