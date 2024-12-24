from fastapi import APIRouter, HTTPException
from elasticsearch import Elasticsearch
from typing import List

# Elasticsearch 클라이언트 설정
es = Elasticsearch(["http://elasticsearch:9200"])

router2 = APIRouter()

# ES 인덱스 이름
INDEX_NAME = "images"

@router2.get("/search/")
async def search_labels(query: str):
    """
    텍스트 키워드를 기반으로 Elasticsearch에서 해당 키워드가 labels에 포함된 이미지 검색
    """
    # Elasticsearch 쿼리 설정: labels 배열에서 검색어와 매칭되는 문서 검색
    body = {
        "query": {
            "terms": {
                "labels": [query]  # labels 배열에서 정확한 텍스트 매칭
            }
        }
    }

    # Elasticsearch에서 쿼리 실행
    response = es.search(index=INDEX_NAME, body=body)

    if response["hits"]["total"]["value"] == 0:
        raise HTTPException(status_code=404, detail="No results found")

    results = []
    for hit in response["hits"]["hits"]:
        results.append({
            "file_name": hit["_source"]["file_name"],
            "description": hit["_source"]["description"],
            "timestamp": hit["_source"]["timestamp"],
            "labels": hit["_source"]["labels"],
            "score": hit["_score"]  # 유사도 점수
        })

    return {"results": results}
