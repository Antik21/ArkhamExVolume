FROM openjdk:17-jdk-slim

WORKDIR /app
COPY . /app

RUN chmod +x ./gradlew
RUN ./gradlew clean shadowJar --no-daemon

RUN ls build/libs/ && test -f build/libs/ArkhamVolume-1-all.jar

COPY build/libs/ArkhamVolume-1-all.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]