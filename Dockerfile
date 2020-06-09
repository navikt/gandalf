FROM navikt/java:11-appdynamics
ENV JAVA_OPTS="-Dlogback.configurationFile=logback-remote.xml -Dlogging.level.org.springframework=DEBUG"
COPY ./build/libs/*-all.jar "app.jar"
COPY init.sh /init-scripts/init.sh
