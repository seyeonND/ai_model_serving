#!/bin/bash

# app 폴더로 이동
cd /app

# 스크립트를 백그라운드에서 실행
python app.py &

# 프로세스가 완료될 때까지 대기
wait