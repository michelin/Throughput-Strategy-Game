FROM maven:3.8.7-openjdk-18-slim AS builder

WORKDIR /usr/src/app

COPY . /usr/src/app

RUN mvn install -DskipTests

FROM ubuntu/jre:edge

COPY --from=builder /usr/src/app/target/*.jar /app.jar

EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-jar", "/app.jar"]
