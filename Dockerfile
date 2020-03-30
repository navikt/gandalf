FROM navikt/java:11-appdynamics

COPY ./build/libs/gandalf.* "app.jar"