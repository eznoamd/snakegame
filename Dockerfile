FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY server/ ./server/

RUN cd server && \
    find src -name "*.java" > sources.txt && \
    javac -cp "src/lib/*" -d out @sources.txt

EXPOSE 3000/udp 8080 3001/udp

CMD ["java", "-cp", "server/out:server/src/lib/*", "Main"]