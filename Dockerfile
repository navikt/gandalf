FROM navikt/java:14-appdynamics
COPY 09-appdynamics-env.sh /init-scripts/
COPY ./build/libs/gandalf-*.jar "app.jar"
