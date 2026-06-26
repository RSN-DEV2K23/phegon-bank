# Stage 1: to build the application
FROM Eclipse-Temurin:21-jdk-alpine as build

# Set the working directory
WORKDIR /app

# Install Apache Maven build tool
# Update the package lists and install Maven without recommending packages to keep the image size small
RUN apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

#Stage 2: to build a production image
FROM Eclipse-Temurin:21-jre-alpine
WORKDIR /app

#copy the final jar file from the build stage to the production image
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","app.jar"]
