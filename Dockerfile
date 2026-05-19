FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /usr/src/app

COPY . /usr/src/app

RUN mvn install -DskipTests

FROM eclipse-temurin:25-jre

COPY --from=builder /usr/src/app/target/*.jar /app.jar

EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-jar", "/app.jar"]
