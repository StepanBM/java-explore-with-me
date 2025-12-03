FROM amazoncorretto:21-alpine-jdk
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENV JAVA_OPTS="-Duser.timezone=Europe/Moscow"
ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar