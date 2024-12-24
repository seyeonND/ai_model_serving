import torch
import clip
from kafka import KafkaConsumer
from minio import Minio
import json
from pymongo import MongoClient
from elasticsearch import Elasticsearch
from datetime import datetime
from PIL import Image
import gc
from labels import labels  # labels.py에서 labels 리스트를 가져옵니다

# GPU 캐시 비우기 및 Python 가비지 컬렉터 호출
torch.cuda.empty_cache()
gc.collect()

# MinIO 클라이언트 초기화
minio_client = Minio(
    endpoint="minio:9000",
    access_key="admin",
    secret_key="Admin1234",
    secure=False
)

# MongoDB 클라이언트 초기화
mongo_client = MongoClient('mongodb://admin:Admin1234@mongo:27017')
db = mongo_client['image_db']

# Elasticsearch 클라이언트 초기화
es = Elasticsearch(["http://elasticsearch:9200"])

# Kafka Consumer 초기화
consumer = KafkaConsumer(
    'label-image-upload-topic',
    bootstrap_servers='kafka:9092',
    group_id='image-group',
    value_deserializer=lambda x: json.loads(x.decode('utf-8'))
)

# MongoDB 인덱스 생성
def create_mongo_index():
    collection = db['images']
    collection.create_index([('file_name', 1)], unique=True)

# Elasticsearch 인덱스 생성
def create_elasticsearch_index():
    index_name = "images"
    if not es.indices.exists(index=index_name):
        body = {
            "settings": {
                "number_of_shards": 1,
                "number_of_replicas": 0
            },
            "mappings": {
                "properties": {
                    "file_name": {"type": "text"},
                    "description": {"type": "text"},
                    "timestamp": {"type": "date"},
                    "features": {
                        "type": "dense_vector",
                        "dims": 10
                    },
                    "labels": {"type": "text"}
                }
            }
        }
        es.indices.create(index=index_name, body=body)

# 인덱스 생성 호출
create_mongo_index()
create_elasticsearch_index()

# MongoDB에 데이터 저장
def save_to_mongo(data):
    if not db['images'].find_one({"file_name": data["file_name"]}):
        print("Inserting data into MongoDB...")
        db['images'].insert_one(data)
        print("Data inserted into MongoDB successfully.")

# Elasticsearch에 데이터 저장
def save_to_elasticsearch(data):
    features = data["features"]
    if len(features) != 10:
        print(f"Error: features length is {len(features)}, but expected 10.")
        return
    document = {
        "file_name": data["file_name"],
        "description": data.get("description", ""),
        "timestamp": data["timestamp"],
        "features": features,
        "labels": data["labels"]
    }
    try:
        es.index(index="images", id=data["file_name"], document=document)
        print("Data inserted or updated in Elasticsearch successfully.")
    except Exception as e:
        print(f"Error inserting data into Elasticsearch: {e}")

# CLIP 모델 로드
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model, preprocess = clip.load("ViT-B/32", device=device)
model.to(device)

# MinIO에서 이미지 다운로드 및 CLIP 추론
def process_image(bucket_name, object_name):
    image_path = f"/tmp/{object_name}"
    minio_client.fget_object(bucket_name, object_name, image_path)
    image = preprocess(Image.open(image_path).resize((224, 224))).unsqueeze(0).to(device)
    
    with torch.no_grad():
        image_features = model.encode_image(image)  # 이미지 특징 추출
        features = image_features.flatten()
        if features.shape[0] != 512:  # 512 차원의 특징 벡터를 기대
            print(f"Error: Features have incorrect shape. Expected 512, got {features.shape[0]}")
            return None
        
        text_input = clip.tokenize(labels).to(device)  # 라벨 토큰화
        text_features = model.encode_text(text_input)  # 텍스트 특징 추출

        # 유사도 계산
        similarities = torch.matmul(image_features, text_features.T).squeeze(0)  # 1차원 텐서로 변환

        # 유사도 정렬 (Top 10)
        top_10_indices = torch.topk(similarities, k=10).indices.tolist()  # 상위 10개 인덱스 추출
        top_10_labels = [labels[i] for i in top_10_indices]  # 인덱스를 사용해 라벨 매칭
        
        # 해당 인덱스에 해당하는 features만 추출 (512 차원의 특징)
        top_10_features = features[top_10_indices]  # 해당 인덱스에 해당하는 features만 추출
        
    return top_10_features, top_10_labels





# Kafka 메시지 처리
def process_message(message):
    try:
        file_name = message['file_name']
        top_10_features, top_10_labels = process_image("mybucket", file_name)
        if top_10_features is None:
            return
        result = {
            "file_name": file_name,
            "features": top_10_features.tolist(),
            "labels": top_10_labels,
            "status": message.get("status", "processed"),
            "timestamp": datetime.now().isoformat()  # JSON 직렬화 가능한 형식
        }
        save_to_mongo(result)
        save_to_elasticsearch(result)
    except Exception as e:
        print(f"Error processing message: {e}")

# Kafka 메시지 소비 및 처리 루프
print("Kafka Consumer is starting...")
for msg in consumer:
    process_message(msg.value)
