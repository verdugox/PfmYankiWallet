FROM openjdk:11
VOLUME /tmp
EXPOSE 9081
ADD ./target/yanki-0.0.1-SNAPSHOT.jar ms-yanki.jar
ENTRYPOINT ["java", "-jar","/ms-yanki.jar"]

