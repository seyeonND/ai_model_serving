#!/bin/bash

# 에러 발생 시 스크립트 종료
set -e

# Spring Boot 애플리케이션 시작
exec java -jar target/sytest-0.0.1-SNAPSHOT.jar "$@"
