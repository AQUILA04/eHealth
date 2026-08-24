FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl services/java/patient-portal-service -am -DskipTests package
FROM eclipse-temurin:21-jre
COPY --from=build /workspace/services/java/patient-portal-service/target/patient-portal-service-*.jar /app.jar
EXPOSE 8091
ENTRYPOINT ["java","-jar","/app.jar"]
