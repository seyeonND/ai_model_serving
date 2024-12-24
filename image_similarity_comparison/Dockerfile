# Base 이미지 선택
FROM nvidia/cuda:12.1.0-base-ubuntu22.04

# 필요한 시스템 라이브러리 설치
RUN apt-get clean && apt-get update && apt-get install -y \
    python3.10 \
    python3.10-distutils \
    python3-pip \
    libgl1-mesa-glx \
    libglib2.0-0 \
    libglib2.0-dev

# Python 3.10 기본 버전으로 설정
RUN ln -s /usr/bin/python3.10 /usr/bin/python

# pip 최신 버전으로 업그레이드
RUN python -m pip install --upgrade pip

# 작업 디렉토리 설정
WORKDIR /app

# 로컬 파일 복사
COPY . /app

# 쉘 스크립트 실행 권한 부여
RUN chmod +x /app/similarity-starts.sh

# 필요한 패키지 설치
RUN pip install --no-cache-dir -r requirements.txt