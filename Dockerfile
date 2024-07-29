# OpenJDK 17 기반의 경량 Alpine Linux 이미지를 사용
FROM bellsoft/liberica-openjdk-alpine:17

# Docker 이미지의 메타데이터를 설정합니다.
LABEL authors="kmg"

# 애플리케이션 JAR 파일을 작업 디렉토리에 복사합니다.
ARG JAR_FILE=build/libs/Jisi-Server-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 애플리케이션이 사용할 포트를 지정합니다.
EXPOSE 8080

# 컨테이너가 시작될 때 실행할 명령을 지정합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]
