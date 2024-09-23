from airflow import DAG

from airflow.operators.bash import BashOperator

default_args = {
    'owner': 'airflow',
    'retries': 1,
}

dag = DAG('data_quality', default_args=default_args)

# Start Data-Quality Service
start_kafka_data_quality_service = BashOperator(
    task_id='start_kafka_data_quality_service',
    bash_command='cd /Users/pedrolera/IdeaProjects/kafka-spark-etl-batch-reports && docker-compose up -d kafka-data-quality',
    dag=dag,
)
