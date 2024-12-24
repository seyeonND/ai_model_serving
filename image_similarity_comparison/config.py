import os

class Config:
    # MinIO 관련 환경 변수
    MINIO_SERVER_ACCESS_KEY = os.getenv('MINIO_SERVER_ACCESS_KEY')
    MINIO_SERVER_SECRET_KEY = os.getenv('MINIO_SERVER_SECRET_KEY')
    MINIO_SERVER_URL = os.getenv('MINIO_SERVER_URL')
    
    # redis 관련 환경 변수
    REDIS_HOST = os.getenv('REDIS_HOST')
    REDIS_PORT = os.getenv('REDIS_PORT')
    REDIS_DB = os.getenv('REDIS_DB')