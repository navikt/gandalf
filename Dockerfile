FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27
WORKDIR /app
COPY ./build/libs/gandalf-*.jar ./app.jar
USER nonroot


ENTRYPOINT ["java", "-jar"]
CMD ["app.jar"]
