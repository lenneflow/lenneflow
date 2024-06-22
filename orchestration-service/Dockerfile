FROM gradle:8.7.0-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle bootJar --no-daemon

FROM openjdk:17-alpine

EXPOSE 47002

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/orchestration-service.jar

ENTRYPOINT ["java","-jar","/app/orchestration-service.jar"]