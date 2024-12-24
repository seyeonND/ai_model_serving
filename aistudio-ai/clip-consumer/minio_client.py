# minio_client.py
from minio import Minio

class MinioClient:
    def __init__(self, endpoint, access_key, secret_key, secure=False):
        self.endpoint = endpoint  # 엔드포인트 저장
        self.client = Minio(endpoint, access_key=access_key, secret_key=secret_key, secure=secure)

    def upload_image(self, bucket_name, file_name, file_data):
        try:
            self.client.put_object(bucket_name, file_name, file_data, len(file_data))
            print(f"Image {file_name} uploaded successfully to MinIO.")
        except Exception as e:
            print(f"Error uploading image to MinIO: {e}")

    def download_image(self, bucket_name, file_name):
        try:
            data = self.client.get_object(bucket_name, file_name)
            return data.read()
        except Exception as e:
            print(f"Error downloading image from MinIO: {e}")
            return None
        
    def get_file_metadata(self, bucket_name, file_name):
        try:
            obj_stat = self.client.stat_object(bucket_name, file_name)
            return {
                "size": obj_stat.size,  # 파일 크기 (바이트)
                "last_modified": obj_stat.last_modified.isoformat(),  # ISO 형식의 최종 수정 시간
                "content_type": obj_stat.content_type,  # MIME 타입
            }
        except Exception as e:
            print(f"Error getting metadata from MinIO: {e}")
            return None
