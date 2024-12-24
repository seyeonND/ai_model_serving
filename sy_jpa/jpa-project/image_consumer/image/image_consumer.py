from minio_client import MinioClient
from mongo_client import MongoClientDB
from elasticsearch_client import ElasticsearchClient
from kafka_consumer import KafkaConsumerHandler
from datetime import datetime

from PIL import Image
from io import BytesIO

import json
import os

import clip
import torch

import numpy as np


device = "cuda" if torch.cuda.is_available() else "cpu"
model, preprocess = clip.load('ViT-B/32', device)

# 객체 초기화
minio_client = MinioClient(
    endpoint="minio:9000", 
    access_key="admin", 
    secret_key="Admin1234", 
    secure=False)
mongo_client = MongoClientDB(
    uri="mongodb://admin:Admin1234@mongo:27017", 
    db_name="image_db1")
es_client = ElasticsearchClient(
    host="http://elasticsearch:9200", 
    index_name="images1")
consumer = KafkaConsumerHandler(
    topic="image-upload-topic", 
    bootstrap_servers="kafka:9092", 
    group_id="image-group")


mongo_client.create_index()
es_client.create_index()


# MinIO에서 이미지 다운로드 및 CLIP 추론 -> image_features 계산
def process_image(bucket_name, object_name):
    file_data  = minio_client.download_image(bucket_name, object_name)

    if file_data is not None:
        # Bytes 데이터를 PIL Image로 변환
        image = Image.open(BytesIO(file_data)).resize((224, 224))
        image_input = preprocess(image).unsqueeze(0).to(device)
    else:
        print("Failed to download the image from MinIO.")

    # Calculate features
    with torch.no_grad():
        image_features = model.encode_image(image_input)

    image_features /= image_features.norm(dim=-1, keepdim=True)

    return image_features

from nanoid import generate

def process_message(message):
    try:
        file_id = generate()
        file_name = message['file_name']
        bucket_name = "mybucket1"

        image_features = process_image(bucket_name, file_name)
        if image_features is None:
            return
        
        result = {
            "file_id": file_id,
            "features": image_features.flatten().tolist(),
       }

        es_client.save_to_elasticsearch(result)
        
    except Exception as e:
        print(f"Error processing message: {e}")

# 텍스트를 벡터로 변환하는 함수
def encode_text(text: str):
    text_input = clip.tokenize([text]).to(device)
    with torch.no_grad():
        text_features = model.encode_text(text_input)
    return text_features / text_features.norm(dim=-1, keepdim=True) 

# Kafka 메시지 소비 및 처리 루프
print("[image-upload-topic] Kafka Consumer is starting...")
for msg in consumer:
    process_message(msg.value)

