from elasticsearch_client import ElasticsearchClient
from kafka_consumer import KafkaConsumerHandler

import clip
import torch
import redis
import json
from deep_translator import GoogleTranslator

device = "cuda" if torch.cuda.is_available() else "cpu"
model, preprocess = clip.load('ViT-B/32', device)

es_client = ElasticsearchClient(
    host="http://elasticsearch:9200", 
    index_name="images1")
consumer = KafkaConsumerHandler(
    topic="search-text-topic", 
    bootstrap_servers="kafka:9092", 
    group_id="search-group")
redis_client = redis.StrictRedis(
    host="redis",
    port="6379",
    password="Admin1234",
    db=0
)
    
def process_message(message):
    text = message['text']
    request_id = message['request_id']
    
    translator = GoogleTranslator(source='auto', target='en')
    en_text = translator.translate(text)
    print(f"text : {text} \n en_text : {en_text}")
    
    try:
        # 1. 텍스트 특징 추출
        text_input = clip.tokenize([en_text]).to(device)
        with torch.no_grad():
            text_features = model.encode_text(text_input)
        
        # 2. 텍스트 특징 정규화
        text_features /= text_features.norm(dim=-1, keepdim=True)
        print(f"텍스트 특징 정규화 완료. 텍스트 특징 크기: {text_features.shape}")
        
        # 이미지 데이터에서 유사한 이미지를 찾는 함수 호출
        # print("="*50)
        # similar_image = find_most_similar_image("images1", text_features)
        
        # print(f"{text} 와 가장 유사한 이미지 : \n"
        #       f"파일명 : {similar_image[0][0]} \n"
        #       f"스코어 : {similar_image[0][1]} \n"
        #       f"(유클리드 거리가 작은 값이 더 유사함)")
        
        print("="*50)
        cosine_similar_image = find_most_similar_image_cosine("images1", text_features)
        
        print(f"{text} 와 가장 유사한 이미지 : \n"
              f"파일명 : {cosine_similar_image[0][0]} \n"
              f"스코어 : {cosine_similar_image[0][1]} \n")
        print("="*50)
        
        # Redis에 저장 (similar_image와 cosine_similar_image 둘 다 저장)
        redis_client.set(request_id, json.dumps({
            "cosine_similar_image": cosine_similar_image
        }))
        print(f"Result for {request_id} saved in Redis.")
    
    except Exception as e:
        print(f"Error processing message {request_id}: {str(e)}")

    

def find_most_similar_image(index, text_features):
    print("1. Elasticsearch에서 이미지 데이터 가져오는 중...")
    
    # 2. ES에서 이미지 특징 가져오기
    query = {
        "query": {
            "match_all": {}  # 모든 문서 검색
        },
        "_source": ["file_name", "features"],
    }
    
    # ES에서 검색
    response = es_client.search(index=index, body=query)
    
    # 유사도 순으로 이미지 반환
    image_features = []
    image_file_names = []
    for hit in response['hits']['hits']:
        image_features.append(hit['_source']['features'])
        image_file_names.append(hit['_source']['file_name'])

    if not image_features:
        print("No images found in Elasticsearch.")
        return []

    print(f"2. {len(image_features)} 개의 이미지 특징을 가져왔습니다.")
    
    # 4. 이미지 특징들 정규화
    image_features = torch.tensor(image_features).to(device)
    image_features /= image_features.norm(dim=-1, keepdim=True)
    print(f"3. 이미지 특징 정규화 완료. 정규화된 이미지 특징 크기: {image_features.shape}")
    
    # Ensure text_features are of the same dtype (float32)
    text_features = text_features.to(torch.float32)
    print(f"4. 텍스트 특징 정규화 완료. 텍스트 특징 크기: {text_features.shape}")
    
    # 5. 유사도 계산: 유클리드 거리 계산 (거리 계산: 두 벡터 간의 차이의 제곱합)
    euclidean_distance = torch.norm(image_features - text_features, dim=1)  # 각 이미지와 텍스트 간의 유클리드 거리 계산
    print(f"5. 유클리드 거리 계산 완료. 거리 크기: {euclidean_distance.shape}, 거리 값:\n{euclidean_distance}")
    
    # 가장 유사한 이미지 1개 선택 (유클리드 거리가 작은 값이 더 유사함)
    value, idx = euclidean_distance.min(0)  # 가장 작은 유클리드 거리 값과 그 인덱스 반환
    print(f"6. 가장 유사한 이미지 인덱스: {idx.item()}, 유클리드 거리: {value.item()}")
    
    # 6. 결과 반환 (ID와 유사도)
    similar_image = (image_file_names[idx.item()], value.item())
    print(f"7. 최종 유사한 이미지: {similar_image}")
    
    return [similar_image]

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
            "min_score": 0.2
            }
        },
        "_source": ["file_id"]
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
        file_id = hit['_source'].get('file_id', 'Unknown')
        score = hit['_score']  # Elasticsearch에서 반환된 유사도 점수

        results.append((file_id, score))

    if not results:
        print("No images found in Elasticsearch.")
        return []

    print(f"검색된 이미지 개수: {len(results)}")
    print(f"검색된 결과: {results}")

    # 모든 검색 결과 반환
    return results




print("[search-text-topic] Kafka Consumer is starting...")
for msg in consumer:
    process_message(msg.value)