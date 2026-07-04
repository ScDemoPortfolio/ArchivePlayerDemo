FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /app

RUN apk add --no-cache bash

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw -e -X clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN apk add --no-cache curl
RUN addgroup -S springuser && adduser -S springuser -G springuser
USER springuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=5 CMD curl -f http://localhost:8080/status || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]