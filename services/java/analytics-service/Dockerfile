FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl services/java/analytics-service -am -DskipTests package
FROM eclipse-temurin:21-jre
COPY --from=build /workspace/services/java/analytics-service/target/analytics-service-*.jar /app.jar
EXPOSE 8092
ENTRYPOINT ["java","-jar","/app.jar"]
