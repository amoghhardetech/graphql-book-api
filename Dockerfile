# Render web service.
# In the Render dashboard, set:
#   DATABASE_URL=postgres://<user>:<password>@<host>:5432/<database>
#     Render's postgres:// URL is converted to jdbc:postgresql:// at startup.
#   SPRING_AI_OPENAI_API_KEY=<key>   # required only when calling OpenAI
# Render sets PORT (default 10000). The app listens on 0.0.0.0:$PORT.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY src src

RUN chmod +x gradlew \
	&& ./gradlew bootJar --no-daemon -x test \
	&& find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -exec cp {} /workspace/app.jar \;

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/app.jar /app/app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
