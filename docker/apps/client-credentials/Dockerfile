FROM gradle:8.14.4-jdk17 AS build
WORKDIR /workspace
COPY . .
RUN gradle -p client-credentials --no-daemon clean bootJar

FROM eclipse-temurin:17-jre AS runtime
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/client-credentials/build/libs/client-credentials.jar /app/app.jar
COPY docker/apps/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
EXPOSE 8887
ENTRYPOINT ["/app/entrypoint.sh"]
CMD ["java", "-jar", "/app/app.jar"]
