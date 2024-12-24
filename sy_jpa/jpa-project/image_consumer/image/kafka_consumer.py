# kafka_consumer.py
import json
from kafka import KafkaConsumer

class KafkaConsumerHandler:
    def __init__(self, topic, bootstrap_servers, group_id):
        self.consumer = KafkaConsumer(
            topic,
            bootstrap_servers=bootstrap_servers,
            group_id=group_id,
            value_deserializer=lambda x: json.loads(x.decode('utf-8'))
        )
        
    def __iter__(self):
        return iter(self.consumer)

    def consume_messages(self):
        for message in self.consumer:
            print(f"Consumed message: {message.value}")
            # Here you can pass message to respective functions for image processing
