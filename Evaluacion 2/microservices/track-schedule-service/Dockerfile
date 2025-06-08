FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN mvn dependency:go-offline -B

COPY src src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

# Instalar tzdata y configurar zona horaria Santiago
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/America/Santiago /etc/localtime && \
    echo "America/Santiago" > /etc/timezone && \
    apk del tzdata

RUN addgroup -g 1000 spring && \
	adduser -u 1000 -G spring -s /bin/sh -D spring

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

USER spring:spring

# Configurar zona horaria como variable de entorno
ENV TZ=America/Santiago

ENTRYPOINT ["java", \
	"-XX:+UseContainerSupport", \
	"-XX:MaxRAMPercentage=75.0", \
	"-XX:InitialRAMPercentage=50.0", \
	"-XX:+UseG1GC", \
	"-Djava.security.egd=file:/dev/./urandom", \
	"-Duser.timezone=America/Santiago", \
	"-jar", \
	"app.jar"]
