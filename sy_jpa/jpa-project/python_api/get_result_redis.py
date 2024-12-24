from fastapi import APIRouter
from fastapi.responses import JSONResponse
import redis
import json

router4 = APIRouter()

# Redis 연결 (결과 저장용)
redis_client = redis.StrictRedis(
    host="redis", 
    port=6379,
    password="Admin1234", 
    db=0
)

# 결과 조회 API
@router4.get("/{request_id}")
async def get_result(request_id: str):
    result = redis_client.get(request_id)

    if result:
        # 저장된 결과 반환
        return JSONResponse(content=json.loads(result))
    else:
        # 결과가 없으면 처리 중 메시지 반환
        return {"status": "processing", "message": "Result not yet available"}
