FROM eclipse-temurin:21-alpine

# Expose the MCP endpoint (mcp.port) and the optional internal Prometheus
# metrics endpoint (metrics.endpointPort, only active when enabled with
# metrics.useInternalHttpServer=T).
EXPOSE 8080
EXPOSE 9400

# Copy to images frost path
ARG ARTIFACT_FILE
COPY target/${ARTIFACT_FILE} /usr/local/FROST/FROST-Mcp.jar
WORKDIR /usr/local/FROST
CMD ["java", "-jar", "FROST-Mcp.jar"]
