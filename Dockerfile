FROM bellsoft/liberica-openjdk-alpine-musl:17

ARG JAR_FILE="./server/target/gateway-1.0.0-SNAPSHOT.jar"

ENV JAVA_OPTS "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
EXPOSE 8080

WORKDIR /usr/app
COPY ${JAR_FILE} app.jar

CMD [ "sh", "-c", "java -jar -Dfile.encoding=UTF-8 ${JAVA_OPTS} app.jar" ]