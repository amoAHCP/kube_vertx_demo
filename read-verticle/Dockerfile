FROM maven:3.3.3-jdk-8
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
ONBUILD ADD . /usr/src/app


COPY . /usr/src/app

CMD ["java", "-jar", "/usr/src/app/target/read-verticle-1.0-SNAPSHOT-fat.jar"]
