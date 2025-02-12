FROM openjdk:17-jdk-alpine

COPY target/marlowbank.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]

EXPOSE 8080