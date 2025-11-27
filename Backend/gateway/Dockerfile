FROM eclipse-temurin:17-jre-alpine

ARG JAR_FILE

COPY ${JAR_FILE} /deploy/app.jar

ENTRYPOINT exec java $JAVA_OPT  -jar /deploy/app.jar
