# BUILD
FROM eclipse-temurin:23-jdk AS build
WORKDIR /app

COPY . .
RUN ./mvnw clean package -DskipTests

# RUN
FROM eclipse-temurin:23-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# DEFAULT ENV VARIABLES
ENV DB_URL=jdbc:postgresql://db:5432/auth.sys
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=password
ENV MAIL_USERNAME=philipsmolka.business@gmail.com
ENV MAIL_PASSWORD=brqymhtubksxfkoo
ENV API_SECRET_KEY=dev-api-key-2024
ENV JWT_SECRET=dev-jwt-secret-2024-very-long-and-random
ENV ADMIN_EMAIL=admin@inventory.pro
ENV ADMIN_PASSWORD=admin123
ENV FRONTEND_URL=http://localhost:3000

ENTRYPOINT ["java", "-jar", "app.jar"]