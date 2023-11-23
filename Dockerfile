FROM gcr.io/distroless/java17
WORKDIR /app
COPY ./build/libs/gandalf-*.jar ./app.jar
USER nonroot

ENV JAVA_TOOL_OPTIONS="${JAVA_OPTS} ${JAVA_PROXY_OPTIONS}"

CMD ["app.jar"]

