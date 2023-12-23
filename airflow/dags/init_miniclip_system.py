from datetime import timedelta

from airflow import DAG

from airflow.operators.bash import BashOperator

default_args = {
    'owner': 'airflow',
    'retries': 1,
}

dag = DAG('init_miniclip_system', default_args=default_args)

# Docker Compose Down
docker_compose_down = BashOperator(
    task_id='docker_compose_down',
    bash_command='cd /Users/pedrolera/IdeaProjects/Miniclip && docker compose down',
    dag=dag,
)

# Compile and Assemble Project
compile_assemble_project = BashOperator(
    task_id='compile_assemble_project',
    bash_command='cd /Users/pedrolera/IdeaProjects/Miniclip && sbt clean compile assembly',
    dag=dag,
)

# Start Services with Docker Compose
start_services = BashOperator(
    task_id='start_services',
    bash_command='cd /Users/pedrolera/IdeaProjects/Miniclip && docker-compose up -d zookeeper miniclip_kafka schema-registry',
    dag=dag,
)


# Check Kafka Readiness
check_kafka_readiness = BashOperator(
    task_id='check_kafka_readiness',
    bash_command='docker exec miniclip_kafka kafka-topics.sh --list --bootstrap-server localhost:9092',
    retries=5,
    retry_delay=timedelta(minutes=1),
    dag=dag,
)


docker_compose_down >> compile_assemble_project >> start_services >> check_kafka_readiness
