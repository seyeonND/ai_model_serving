from scipy.spatial.distance import cosine
import clip
import torch

def find_most_similar_image(text: str, image_features_list: list, image_ids: list):
    text_features = encode_text(text)  # 입력된 텍스트를 벡터화

    similarities = []
    for image_features in image_features_list:
        similarity = 1 - cosine(text_features.cpu().numpy(), image_features.cpu().numpy())  # 코사인 유사도
        similarities.append(similarity)

    # 가장 유사한 이미지의 인덱스 반환
    most_similar_index = similarities.index(max(similarities))
    return image_ids[most_similar_index], similarities[most_similar_index]

# 텍스트 검색 함수
def search_images_by_text(query_text: str):
    query_vector = encode_text(query_text).cpu().numpy().tolist()  # 텍스트 벡터화
    response = es.search(
        index="images",
        body={
            "query": {
                "script_score": {
                    "query": {
                        "match_all": {}  # 모든 이미지 검색
                    },
                    "script": {
                        "source": "cosineSimilarity(params.query_vector, 'features') + 1.0",  # 코사인 유사도 계산
                        "params": {
                            "query_vector": query_vector
                        }
                    }
                }
            },
            "_source": ["file_name", "features", "labels"]  # 필요한 필드만 반환
        }
    )
    
    hits = response['hits']['hits']
    if hits:
        most_similar_image = hits[0]
        return most_similar_image['_source'], most_similar_image['_score']
    else:
        return None, 0.0

# 예시: 텍스트로 이미지 검색
query_text = "a photo of a ballerina"
result, score = search_images_by_text(query_text)
print(f"Most similar image: {result['file_name']}, Similarity score: {score}")

# 예시로 사용하는 함수
def ex_process_image(message):
    file_name = message.get("file_name")
    file_data = message.get("file_data")
    labels = message.get("labels")
    features = message.get("features")

    timestamp = datetime.utcnow().isoformat()

    # MinIO 업로드
    minio_client.upload_image(file_name, file_data)

    # MongoDB에 저장
    mongo_data = {
        "file_name": file_name,
        "description": "",
        "timestamp": timestamp,
        "labels": labels
    }
    mongo_client.save_to_mongo(mongo_data)

    # Elasticsearch에 저장
    es_data = {
        "file_name": file_name,
        "description": "",
        "timestamp": timestamp,
        "features": features,
        "labels": labels
    }
    es_client.save_to_elasticsearch(es_data)

# Kafka 메시지를 소비하면서 이미지 처리
kafka_consumer_handler.consume_messages()
