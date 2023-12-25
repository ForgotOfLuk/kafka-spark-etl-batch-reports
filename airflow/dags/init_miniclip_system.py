from datetime import timedelta, datetime
from airflow import DAG
from airflow.operators.bash import BashOperator
from airflow.models import Variable

default_args = {
    'owner': 'airflow',
    'retries': 1,
    'retry_delay': timedelta(minutes=1),
}

dag = DAG('init_miniclip_system', default_args=default_args, schedule_interval=None, start_date=datetime(2023, 1, 1))

project_dir = Variable.get("MINICLIP_PROJECT_DIR", "/Users/pedrolera/IdeaProjects/Miniclip")

# Docker Compose Down
docker_compose_down = BashOperator(
    task_id='docker_compose_down',
    bash_command=f'cd {project_dir} && docker compose down',
    dag=dag,
)

# Compile and Assemble Project
compile_assemble_project = BashOperator(
    task_id='compile_assemble_project',
    bash_command=f'cd {project_dir} && sbt clean compile assembly',
    dag=dag,
)

# Start Services with Docker Compose (including Spark and MongoDB)
start_services = BashOperator(
    task_id='start_services',
    bash_command=f'cd {project_dir} && docker-compose up -d zookeeper miniclip_kafka schema-registry spark-master spark-worker mongodb',
    dag=dag,
)

# Check Kafka Readiness
check_kafka_readiness = BashOperator(
    task_id='check_kafka_readiness',
    bash_command=f'docker exec miniclip_kafka kafka-topics.sh --list --bootstrap-server localhost:9092',
    retries=5,
    dag=dag,
)

# Check Spark Master Readiness
check_spark_readiness = BashOperator(
    task_id='check_spark_readiness',
    bash_command=f'docker exec spark-master /opt/bitnami/spark/bin/run-example SparkPi 10',
    retries=5,
    dag=dag,
)

# Define task order
docker_compose_down >> compile_assemble_project >> start_services >> check_kafka_readiness >> check_spark_readiness
