import torch
import clip
from PIL import Image
from deep_translator import GoogleTranslator
from sklearn.metrics.pairwise import cosine_similarity

device = "cuda" if torch.cuda.is_available() else "cpu"
model, preprocess = clip.load("ViT-B/32", device=device)

image_paths = ["./dog.jpg","./IE003220843_STD.jpg","./133858_87006_2051.jpg"]

min_score = 0.25

def get_feature(type, image_path, text_query):
    if type == "image":
        image = preprocess(Image.open(image_path).convert("RGB")).unsqueeze(0).to(device)
        with torch.no_grad():
            image_features = model.encode_image(image)
            image_features /= image_features.norm(dim=-1, keepdim=True)
        return image_features.cpu().numpy().tolist()
    if type == "text":
        text_tokens = clip.tokenize([text_query]).to(device)
        with torch.no_grad():
            text_features = model.encode_text(text_tokens)
            text_features /= text_features.norm(dim=-1, keepdim=True)
        return text_features.cpu().numpy().tolist()
    
image_features_list = []

for path in image_paths:
    image_features = get_feature("image", path, None)
    image_features_list.append((path, image_features))

while True:
    text_query = input("검색 문장을 입력하세요: ")
    if text_query.lower() == 'exit':
        break

    translator = GoogleTranslator(source='auto', target='en')
    trans_text_query = translator.translate(text_query)
    text_features = get_feature("text", None, trans_text_query)

    similarities = {}
    for path, image_features in image_features_list:
        similarity = cosine_similarity(text_features, image_features)[0][0]
        similarity = round(similarity, 3)
        if similarity >= min_score:
            similarities[path] = similarity
    print("="*50)
    print(f"입력 텍스트        : {text_query} -> {trans_text_query}")
    if not similarities:
        print("유사한 이미지가 없습니다.")
    else:
        most_similar_image = max(similarities, key=similarities.get)
        print(f"가장 유사한 이미지 : {most_similar_image}")
        print(f"유사도             : {similarities[most_similar_image]}")
    print("="*50)

