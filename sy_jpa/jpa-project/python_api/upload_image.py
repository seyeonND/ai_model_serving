import logging
from fastapi import APIRouter, File, UploadFile
from fastapi.responses import JSONResponse
from minio import Minio
from minio.error import S3Error
from kafka import KafkaProducer
import json


# 로그 설정
logging.basicConfig(level=logging.DEBUG)  # 로그 레벨을 DEBUG로 설정
logger = logging.getLogger(__name__)

# MinIO 클라이언트 초기화
minio_client = Minio(
    endpoint="minio:9000",  # MinIO URL
    access_key="admin",         # MinIO Root User
    secret_key="Admin1234",     # MinIO Root Password
    secure=False                # HTTP 사용 (HTTPS가 아님)
)

# MinIO 버킷 이름
BUCKET_NAME = "mybucket1"


# Kafka Producer 초기화
kafka_bootstrap_servers = 'kafka:9092'
kafka_topic = 'image-upload-topic'

# Kafka Producer 초기화
producer = KafkaProducer(
    bootstrap_servers=kafka_bootstrap_servers,  # Kafka 서버 주소
    value_serializer=lambda v: json.dumps(v).encode('utf-8')  # 메시지 직렬화
)

# FastAPI 앱 초기화
router1 = APIRouter()

# MinIO 버킷 생성 함수
def create_bucket_if_not_exists(bucket_name: str):
    logger.debug(f"Checking if bucket '{bucket_name}' exists...")
    if not minio_client.bucket_exists(bucket_name):
        logger.info(f"Bucket '{bucket_name}' does not exist, creating it...")
        minio_client.make_bucket(bucket_name)
        logger.info(f"Bucket '{bucket_name}' created successfully.")
    else:
        logger.debug(f"Bucket '{bucket_name}' already exists.")

# Kafka 메시지 전송 함수
def send_kafka_message(file_name: str):
    message = {
        "file_name": file_name,
        "status": "new_image_uploaded"
    }
    logger.info(f"Sending Kafka message: {message}")
    # Kafka로 메시지 전송
    producer.send(kafka_topic, message)
    producer.flush()  # 메시지가 즉시 전송되도록 함
    logger.info(f"Kafka message for file '{file_name}' sent successfully.")

# API 엔드포인트: 파일 업로드
@router1.post("/upload-image")
async def upload_image(file: UploadFile = File(...)):
    """이미지를 MinIO에 업로드합니다."""
    logger.info(f"Received file upload request for: {file.filename}")
    try:
        create_bucket_if_not_exists(BUCKET_NAME)  # 버킷 생성 (없으면 생성)

        # 파일 크기 구하기
        file_size = len(await file.read())
        file.file.seek(0)  # 파일 포인터를 처음으로 이동
        logger.debug(f"File size for {file.filename}: {file_size} bytes")

        # MinIO에 파일 업로드
        logger.info(f"Uploading file '{file.filename}' to MinIO...")
        minio_client.put_object(
            bucket_name=BUCKET_NAME,
            object_name=file.filename,
            data=file.file,
            length=file_size,  # 파일 크기를 정확히 전달
            content_type=file.content_type,  # MIME 타입을 정확히 전달
            part_size=5 * 1024 * 1024  # 5MB (파일 크기가 크면 적절히 조정)
        )
        logger.info(f"File '{file.filename}' uploaded successfully to MinIO.")

        # Kafka 메시지 전송
        send_kafka_message(file.filename)

        return {"message": f"File '{file.filename}' uploaded successfully!"}
    
    except ValueError as e:
        logger.error(f"ValueError: {e}")
        return JSONResponse(status_code=400, content={"error": str(e)})
    except S3Error as err:
        logger.error(f"S3Error: {err}")
        return JSONResponse(status_code=500, content={"error": str(err)})
    except Exception as e:
        logger.error(f"Unexpected error: {e}")
        return JSONResponse(status_code=500, content={"error": "An unexpected error occurred."})

