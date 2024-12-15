FROM openjdk:17-jdk-slim

WORKDIR /app
COPY . /app

RUN chmod +x ./gradlew
RUN ./gradlew clean shadowJar --no-daemon --rerun-tasks

ENTRYPOINT ["java", "-jar", "build/libs/ArkhamVolume-1-all.jar"]