# elasticsearch_client.py
from elasticsearch import Elasticsearch

class ElasticsearchClient:
    def __init__(self, host, index_name):
        self.es = Elasticsearch([host])
        self.index_name = index_name
        self.create_index()

    def create_index(self):
        if not self.es.indices.exists(index=self.index_name):
            body = {
                "settings": {
                    "number_of_shards": 1,
                    "number_of_replicas": 0
                },
                "mappings": {
                    "properties": {
                        "file_id": {"type": "keyword"},  # 검색 최적화를 위해 keyword로 변경
                        "image_path": {"type": "keyword"},
                        "features": {
                            "type": "dense_vector",
                            "dims": 512  # 벡터 차원 수
                        },
                    }
                }
            }
            self.es.indices.create(index=self.index_name, body=body)
            print("Created index")

    def save_to_elasticsearch(self, data):
        features = data["features"]  

        if len(features) != 512:  
            raise ValueError(f"Invalid features length: expected 512, got {len(features)}")

        document = {
            "file_id": data["file_id"],
            "image_path": data["image_path"],
            "features": features,
        }
        try:
            self.es.index(index=self.index_name, id=data["file_id"], document=document)
            print("Data inserted or updated in Elasticsearch successfully.")
        except Exception as e:
            print(f"Error inserting data into Elasticsearch: {e}")

    # search 메서드 추가
    def search(self, body, index=None):
        index = index or self.index_name
        return self.es.search(index=index, body=body)