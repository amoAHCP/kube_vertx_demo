FROM maven:3.3.3-jdk-8
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
ONBUILD ADD . /usr/src/app

ADD cluster/cluster.xml /cluster/
ENV httpPort 8181
EXPOSE $httpPort
COPY . /usr/src/app
CMD ["java", "-jar", "/usr/src/app/target/frontend-verticle-1.0-SNAPSHOT-fat.jar", "-cluster","-cp","/cluster/"]