FROM openjdk:17-jdk-slim

WORKDIR /app
COPY . /app

RUN chmod +x ./gradlew
RUN ./gradlew clean shadowJar --no-daemon --rerun-tasks

RUN ls -la build/libs/

COPY build/libs/ArkhamVolume-1-all.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]