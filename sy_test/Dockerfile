FROM ubuntu:latest

RUN apt-get update && apt-get install -y openjdk-17-jdk

COPY ./ubuntu/sytest /app
COPY entrypoint.sh /app

WORKDIR /app

RUN ./mvnw clean package -DskipTests