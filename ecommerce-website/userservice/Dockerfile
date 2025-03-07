# Use Java 21 Base Image
FROM eclipse-temurin:21-jdk

# Set working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/*.jar app.jar

# Expose the port for the service
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
