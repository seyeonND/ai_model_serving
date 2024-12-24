# mongo_client.py
from pymongo import MongoClient

class MongoClientDB:
    def __init__(self, uri, db_name):
        self.client = MongoClient(uri)
        self.db = self.client[db_name]
        self.collection = self.db['images1']
        self.create_index()

    def create_index(self):
        self.collection.create_index([('file_name', 1)], unique=True)

    def save_to_mongo(self, data):
        if not self.collection.find_one({"file_name": data["file_name"]}):
            print("Inserting data into MongoDB...")
            self.collection.insert_one(data)
            print("Data inserted into MongoDB successfully.")

    def get_image_data(self, file_name):
        return self.collection.find_one({"file_name": file_name})
    
    
