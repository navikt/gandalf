FROM gcr.io/distroless/java17
WORKDIR /app
COPY ./build/libs/gandalf-*.jar ./app.jar
USER nonroot


CMD ["app.jar"]

