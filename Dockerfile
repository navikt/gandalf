FROM navikt/java:11-appdynamics

COPY ./build/libs/no.nav.gandalf.* "app.jar"