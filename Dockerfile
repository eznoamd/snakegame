FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY server/pom.xml .
RUN mvn dependency:go-offline

COPY server/src ./src

RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=build /app/target/server-1.0-SNAPSHOT.jar app.jar

EXPOSE 3000/udp 8080 3001/udp

CMD ["java", "-jar", "app.jar"]