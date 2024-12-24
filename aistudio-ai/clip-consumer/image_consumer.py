from minio_client import MinioClient
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
from config import Config


env_config = Config()

device = "cuda" if torch.cuda.is_available() else "cpu"
model, preprocess = clip.load('ViT-B/32', device)

# 객체 초기화
minio_client = MinioClient(
    endpoint=env_config.MINIO_SERVER_URL, 
    access_key=env_config.MINIO_SERVER_ACCESS_KEY,
    secret_key=env_config.MINIO_SERVER_SECRET_KEY, 
    secure=False)
es_client = ElasticsearchClient(
    host=env_config.ES_HOST, 
    index_name=env_config.ES_INDEX_NAME)
consumer = KafkaConsumerHandler(
    topic=env_config.KAFKA_IMAGE_TOPIC, 
    bootstrap_servers=env_config.KAFKA_HOST, 
    group_id=env_config.KAFKA_IMAGE_GROUP)



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


def process_message(message):
    try:
        
        # 메시지에서 bucketName/fileName 추출
        parsed_message = json.loads(message)
        
        text_flag = parsed_message.get('text')
        
        if text_flag == 'true':

            bucket_name = parsed_message.get('bucketName')

            if file_name is None or file_name == "":
                print("Error: file_name is missing or empty!")
            else:
                if file_name.startswith('/'):
                    file_name = file_name[1:]
                    path = f"{bucket_name}/{file_name}"
                else:
                    path = f"{bucket_name}/{file_name}"
                    
            print(f"bucket_name : {bucket_name} \nfile_name : {file_name} \nfile_id : {file_id} \npath : {path}\n")

            image_features = process_image(bucket_name, file_name)
            if image_features is None:
                return
            
            result = {
                "image_path": path,
                "features": image_features.flatten().tolist(),
            }

            es_client.save_to_elasticsearch(result)
            
        else:
            print("[text] clip not use")
        
    except Exception as e:
        print(f"Error processing message: {e}")

# Kafka 메시지 소비 및 처리 루프
print("[image-model] Kafka Consumer is starting...")
for msg in consumer:
    process_message(msg.value)

