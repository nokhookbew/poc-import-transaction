FROM openjdk:17.0.2-jdk-slim
VOLUME /tmp
COPY target/poc-import-transaction-0.0.1-SNAPSHOT.jar poc-import-transaction.jar
ENTRYPOINT ["java", "-jar", "poc-import-transaction.jar"]