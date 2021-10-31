FROM maven:3.8.3-jdk-8-slim

COPY src ./src
COPY config  ./config

COPY pom.xml .
COPY asset-types.properties .
COPY scripts/run.sh .
COPY scripts/lister.sh .
COPY scripts/deleter.sh .
# We do not copy /config (for user-provided config).
RUN mvn install dependency:copy-dependencies

ENTRYPOINT ["sh", "./run.sh"]