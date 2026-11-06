FROM gradle:9.8.2-jdk25

WORKDIR /workspace
COPY . .

COPY docker/journeys-runner/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

WORKDIR /workspace/journeys
ENTRYPOINT ["/entrypoint.sh"]
CMD ["gradle", "--no-daemon", "test"]
