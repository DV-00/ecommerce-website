# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the application's jar file to the container
COPY target/notificationservice-0.0.1-SNAPSHOT.jar notificationservice.jar

# Expose the port that the application will run on
EXPOSE 8085

# Run the jar file
ENTRYPOINT ["java", "-jar", "notificationservice.jar"]
