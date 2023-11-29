FROM gcr.io/distroless/java21
WORKDIR /app
COPY ./build/libs/gandalf-*.jar ./app.jar
USER nonroot


CMD ["app.jar"]

