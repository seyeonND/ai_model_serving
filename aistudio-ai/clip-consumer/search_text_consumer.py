from elasticsearch_client import ElasticsearchClient
from kafka_consumer import KafkaConsumerHandler

import clip
import torch
import json
from deep_translator import GoogleTranslator
import redis

from config import Config


env_config = Config()

device = "cuda" if torch.cuda.is_available() else "cpu"
model, preprocess = clip.load('ViT-B/32', device)

es_client = ElasticsearchClient(
    host=env_config.ES_HOST, 
    index_name=env_config.ES_INDEX_NAME)
consumer = KafkaConsumerHandler(
    topic=env_config.KAFKA_SEARCH_TOPIC, 
    bootstrap_servers=env_config.KAFKA_HOST, 
    group_id=env_config.KAFKA_SEARCH_GROUP)
redis_client = redis.StrictRedis(
    host=env_config.REDIS_HOST,
    port=env_config.REDIS_PORT,
    db=env_config.REDIS_DB
)
    
def process_message(message):
    print(f"message : \n{message}")
    
    parsed_message = json.loads(message)
    text = parsed_message.get('searchText')
    redis_key = parsed_message.get('redisKey')
    
    translator = GoogleTranslator(source='auto', target='en')
    en_text = translator.translate(text)
    print(f"text : {text} \nen_text : {en_text}")
    
    try:
        # 1. 텍스트 특징 추출
        text_input = clip.tokenize([en_text]).to(device)
        with torch.no_grad():
            text_features = model.encode_text(text_input)
        
        # 2. 텍스트 특징 정규화
        text_features /= text_features.norm(dim=-1, keepdim=True)
        print(f"텍스트 특징 정규화 완료. 텍스트 특징 크기: {text_features.shape}")
        
        print("="*50)
        cosine_similar_image = find_most_similar_image_cosine("data_features", text_features)

        print("="*50)
        
        # Redis에 저장 (cosine_similar_image 저장)
        redis_client.lpush(redis_key, json.dumps({
            "cosine_similar_image": cosine_similar_image
        }))
        print(f"Result for {redis_key} saved in Redis.")
    
    except Exception as e:
        print(f"Error processing message {redis_key}: {str(e)}")


def find_most_similar_image_cosine(index, text_features):
    print("find_most_similar_image_cosine...")
    
    text_features_list = text_features.cpu().numpy().flatten().tolist()

    query = {
        "query": {
            "function_score": {
            "query": {
                "match_all": {}
            },
            "functions": [
                {
                    "script_score": {
                        "script": {
                            "source": "cosineSimilarity(params.query_vector, 'features')",
                            "params": {
                                "query_vector": text_features_list
                            }
                        }
                    }
                }
            ],
            "boost_mode": "replace",
            "min_score": 0.21
            }
        },
        "_source": ["file_id", "image_path"]
    }
    # Elasticsearch에서 검색
    try:
        response = es_client.search(index=index, body=query)
    except Exception as e:
        print(f"Error during Elasticsearch search: {e}")
        return []
    
    # 검색 결과에서 이미지 파일 이름과 유사도 점수 반환
    results = []

    for hit in response['hits']['hits']:
        image_path = hit['_source'].get('image_path', 'Unknown')
        score = hit['_score']  # Elasticsearch에서 반환된 유사도 점수

        results.append({
            "image_path": image_path,
            "score": score
        })

    if not results:
        print("No images found in Elasticsearch.")
        return []

    print(f"검색된 이미지 개수: {len(results)}")
    print(f"검색된 결과: {results}")

    # 모든 검색 결과 반환
    return results


print("[search-text] Kafka Consumer is starting...")
print(f"[kafka topic] : {env_config.KAFKA_SEARCH_TOPIC}")
for msg in consumer:
    process_message(msg.value)