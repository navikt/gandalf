FROM navikt/java:14-appdynamics
COPY ./build/libs/gandalf-*.jar "app.jar"
COPY init.sh /init-scripts/init.sh
