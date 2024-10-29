FROM maven:latest as builder
WORKDIR /usr/src/app
COPY pom.xml ./
RUN mvn dependency:resolve
COPY src ./src
RUN mvn clean package

FROM eclipse-temurin:23-jre-alpine as final
WORKDIR /usr/app
COPY --from=builder /usr/src/app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
