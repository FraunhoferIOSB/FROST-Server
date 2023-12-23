FROM eclipse-temurin:17-alpine

EXPOSE 1883
EXPOSE 9876

# Copy to images tomcat path
ARG ARTIFACT_FILE
COPY target/${ARTIFACT_FILE} /usr/local/FROST/FROST-Mqtt.jar
WORKDIR /usr/local/FROST
CMD ["java", "-jar", "FROST-Mqtt.jar"]
