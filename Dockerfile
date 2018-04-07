FROM openjdk:8

VOLUME /tmp
ADD config config
ADD buffer.jar buffer.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/buffer.jar"]
