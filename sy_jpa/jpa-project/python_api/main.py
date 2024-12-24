from fastapi import FastAPI
from upload_image import router1  
from label_search_image import router2 
from search_image import router3
from get_result_redis import router4

app = FastAPI()

# 라우터 추가
app.include_router(router1, prefix="/upload", tags=["IMAGE UPLOAD"])
app.include_router(router2, prefix="/label", tags=["LABEL SEARCH"])
app.include_router(router3, prefix="/text", tags=["TEXT SEARCH"])
app.include_router(router4, prefix="/result", tags=["GET RESULT"])