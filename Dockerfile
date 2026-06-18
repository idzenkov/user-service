FROM maven:3.9-eclipse-temurin-17 AS build
ARG MODULE_NAME
WORKDIR /app
COPY pom.xml .
COPY /${MODULE_NAME}/pom.xml ./${MODULE_NAME}/
COPY /${MODULE_NAME}/src ./${MODULE_NAME}/src
RUN mvn -pl ${MODULE_NAME} -am clean package -DskipTests

FROM eclipse-temurin:17-jre
ARG MODULE_NAME
WORKDIR /app
COPY ${MODULE_NAME}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]