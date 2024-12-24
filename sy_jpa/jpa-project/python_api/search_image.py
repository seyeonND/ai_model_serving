from fastapi import APIRouter, HTTPException
from kafka import KafkaProducer
import json
import uuid

router3 = APIRouter()

# Kafka Producer 초기화
kafka_bootstrap_servers = 'kafka:9092'
kafka_topic = 'search-text-topic'


producer = KafkaProducer(
    bootstrap_servers=kafka_bootstrap_servers,  # Kafka 서버 주소
    value_serializer=lambda v: json.dumps(v).encode('utf-8')  # 메시지 직렬화
)

@router3.get("/search/")
async def search(text: str):
    # 고유한 요청 ID 생성
    request_id = str(uuid.uuid4())

    # Kafka 메시지 형성
    search_message = {
        "request_id": request_id,
        "text": text
    }

    # Kafka로 메시지 보내기
    send_message_to_kafka(search_message)

    # 결과는 별도로 조회할 수 있도록 고유 ID 반환
    return {"request_id": request_id, "message": "Search started, check back for results"}


# Kafka로 메시지 보내기
def send_message_to_kafka(message: dict):
    try:
        producer.send(kafka_topic, message)  # 메시지를 Kafka에 전송
        print(f"Message sent to Kafka: {message}")
    except Exception as e:
        print(f"Error sending message to Kafka: {e}")