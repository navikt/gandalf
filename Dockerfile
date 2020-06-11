FROM navikt/java:11-appdynamics
COPY ./build/libs/gandalf-*.jar "app.jar"
COPY init.sh /init-scripts/init.sh
