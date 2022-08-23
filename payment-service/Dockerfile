FROM openjdk:8-jdk-alpine
EXPOSE 8084
COPY build/libs/payment-service-1.0-SNAPSHOT.jar .
RUN apk add --no-cache bash
COPY wait-for.sh /wait-for.sh
RUN chmod +x /wait-for.sh
ENTRYPOINT ["/wait-for.sh", "deposit-service:8083", "--timeout=90", "--", "java", "-jar", "payment-service-1.0-SNAPSHOT.jar"]