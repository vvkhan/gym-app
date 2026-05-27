FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY gym-core/pom.xml gym-core/pom.xml
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY trainer-report-service/pom.xml trainer-report-service/pom.xml
RUN mvn dependency:go-offline -pl gym-core -am -q
COPY gym-core/src gym-core/src
RUN mvn package -pl gym-core -am -DskipTests -q

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/gym-core/target/*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=standalone
ENTRYPOINT ["java", "-jar", "app.jar"]
