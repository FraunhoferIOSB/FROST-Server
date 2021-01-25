FROM openjdk:11

EXPOSE 1883
EXPOSE 9876

# Copy to images tomcat path
ARG JAR_FILE
COPY target/${JAR_FILE} /usr/local/FROST/FROST-Mqtt.jar
WORKDIR /usr/local/FROST
CMD ["java", "-jar", "FROST-Mqtt.jar"]
