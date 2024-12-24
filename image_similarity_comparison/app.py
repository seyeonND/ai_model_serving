import os
import io
import torch
import rawpy
import requests
import numpy as np
from LPIPS.LPIPS import LPIPS
from util.util import calcualte_distance_with_pickles
from config import Config
from minio import Minio
from minio.error import S3Error
from util.util import resize_img, tensor_2_numpy_list, im2tensor
from util.Img_Vec import Img_Vec
from PIL import Image
import redis
import json

env_config = Config()

# MinIO 클라이언트 설정
minio_client = Minio(
    env_config.MINIO_SERVER_URL,
    access_key=env_config.MINIO_SERVER_ACCESS_KEY,
    secret_key=env_config.MINIO_SERVER_SECRET_KEY,
    secure=False
)

redis_client = redis.StrictRedis(
    host=env_config.REDIS_HOST,
    port=env_config.REDIS_PORT,
    db=env_config.REDIS_DB
)

# pickle 파일이 저장되는 곳
pickle_dir = ""

# projectId 
project_id = ""


# MinIO에서 이미지 조회
def getDataFromMinIO(bucket_name, object_name):
    response = minio_client.get_object(bucket_name, object_name)
    return response

def main(device, file_id, np_image, pickle_dir:str, width:int, height:int):
    
    model = LPIPS(device=device, net='squeeze').to(device)
    
    single_image(file_id, np_image, pickle_save_path=pickle_dir, model=model, width=width, height=height)
    
    final_img_list = calcualte_distance_with_pickles(pickle_dir=pickle_dir, model=model)
    
    print(final_img_list)

    return final_img_list

def getTotalFile(dataset_ids):
    file_id_list = []
    
    for dataset_id in dataset_ids:
        file_id_list.extend(getFileIdList(dataset_id))
    total_count = len(file_id_list)
    return total_count, file_id_list


# 새로운 데이터셋의 dataset_file 업데이트
def update_dataset(final_img_list, output_dataset_ids):
    url = ''
    
    dataset_id = output_dataset_ids[0]
    data = getDatasetData(dataset_id)
    workspace_id = data['workspaceId']
    dataset_name = data['datasetName']
    file_ids = final_img_list
    tags = data['tags']

    file_ids_payload = [{"fileId": file_id} for file_id in file_ids]

    payload = {
        "datasetId": dataset_id,
        "workspaceId": workspace_id,
        "datasetName": dataset_name,
        "fileIds": file_ids_payload,
        "tags": tags
    }

    response = requests.post(url, json=payload)
    if response.status_code == 200:
        print("전처리 완료")
    else:
        print(f"Error: {response.status_code}")
        print(f"Response Content: {response.text}")
        return None

def init(gpu):
    try:
        input_dataset_ids, output_dataset_ids = getProjectData(project_id)
        if not input_dataset_ids:
            print("No input dataset found.")
            return # input dataset 없으면 종료

        total_count, file_id_list = getTotalFile(input_dataset_ids)
        count_index = 0
            
        if not file_id_list:
            print("No fileIds found or error occurred.") 
            return  # 파일 ID 리스트가 없으면 종료
        
        for file_dict in file_id_list:
            file_id = file_dict['fileId']

            # file_id로 file 정보 조회
            file = getFileData(file_id)

            # MinIO에서 데이터 정보 조회
            bucket_name = file['bucketName']
            file_name = file['fileName']
            data = getDataFromMinIO(bucket_name, file_name)

            image_data = io.BytesIO(data.read())
        
            # 파일 확장자에 따라 처리
            if file_name.endswith('dng'):
                # DNG 파일 처리
                with rawpy.imread(image_data) as raw:
                    img = raw.postprocess()
            elif file_name.endswith(('bmp', 'jpg', 'png', 'jpeg')):
                # 다른 이미지 형식 처리 (cv2 사용)
                image = Image.open(image_data)
                img = np.array(image)
                img = img[:, :, ::-1]  # BGR to RGB (cv2는 BGR로 읽기 때문에 변환)
            else:
                print(f"is not an image file.")
                return None
            
            gpu = gpu
            device = torch.device("cpu")
            if gpu == None:
                pass
            else:
                os.environ["CUDA_VISIBLE_DEVICES"] = str(gpu)
                device = torch.device(f"cuda:{gpu}" if torch.cuda.is_available() else "cpu")

            count_index += 1
            print(f"[{count_index}/{total_count}] 진행 중 ....")
            redis_client.publish(project_id, json.dumps({
                "countIndex": count_index,
                "totalCount": total_count
            }))
            final_img_list = main(device, file_id, img, pickle_dir, width=192, height=384)

            if count_index == total_count:
                update_dataset(final_img_list, output_dataset_ids)

        delete_pickles(pickle_dir)
    except S3Error as e:
        print(f"Error: {e}")
        return None
    
init(0)