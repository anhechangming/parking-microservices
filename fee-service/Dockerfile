FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/fee-service.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
