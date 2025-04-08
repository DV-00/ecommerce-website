# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the application's jar file to the container
COPY target/orderservice-0.0.1-SNAPSHOT.jar orderservice.jar

# Expose the port that the application will run on
EXPOSE 8083

# Run the jar file
ENTRYPOINT ["java", "-jar", "orderservice.jar"]
