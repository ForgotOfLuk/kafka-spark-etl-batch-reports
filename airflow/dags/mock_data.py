from airflow import DAG

from airflow.operators.bash import BashOperator

default_args = {
    'owner': 'airflow',
    'retries': 1,
}

dag = DAG('mock_data', default_args=default_args)


# Start Mock-Data Service
start_mock_data_service = BashOperator(
    task_id='start_mock_data_service',
    bash_command='cd /Users/pedrolera/IdeaProjects/Miniclip && docker-compose up -d mock-data',
    dag=dag,
)
