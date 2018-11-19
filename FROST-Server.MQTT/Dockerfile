FROM openjdk:8

EXPOSE 1883
EXPOSE 9876

# Copy to images tomcat path
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/local/FROST/FROST-Mqtt.jar
WORKDIR /usr/local/FROST
CMD ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-jar", "FROST-Mqtt.jar"]
