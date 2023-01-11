FROM navikt/java:15-appdynamics
COPY 09-appdynamics-env.sh /init-scripts/
COPY 11-init.sh /init-scripts/
COPY ./build/libs/gandalf-*.jar "app.jar"
