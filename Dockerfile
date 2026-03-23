# Use OpenJDK 11 as base image (8 is deprecated, 11 is LTS)
FROM openjdk:11-jdk-slim

# Set working directory
WORKDIR /app

# Copy only the necessary files first to leverage Docker cache
COPY server/src/ ./server/src/
COPY server/src/lib/ ./server/src/lib/

# Compile the Java application
RUN cd server && \
    javac -cp "src/lib/*;." -d out src/**/*.java

# Expose the required ports
# 3000: UDP Server port
# 8080: WebSocket Gateway port
# 3001: Gateway return port
EXPOSE 3000 8080 3001

# Set the classpath and run the application
CMD ["java", "-cp", "server/src/lib/*;server/out", "Main"]
