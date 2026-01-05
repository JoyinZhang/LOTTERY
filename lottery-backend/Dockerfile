FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/lottery-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
