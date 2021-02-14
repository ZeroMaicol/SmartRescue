FROM java:8
FROM maven:alpine

# image layer
WORKDIR /app
ADD pom.xml /app
RUN mvn verify clean --fail-never

# Image layer: with the application
COPY . /app
RUN mvn -v
RUN mvn clean compile assembly:single
EXPOSE 8883
ADD ./target/LocalController-1.0-SNAPSHOT-jar-with-dependencies.jar /developments/
ENTRYPOINT ["java","-jar","/developments/LocalController-1.0-SNAPSHOT-jar-with-dependencies.jar  --endpoint a2spllhdjdikn-ats.iot.eu-central-1.amazonaws.com   --rootca /app/keys/AmazonRootCA1.pem --cert /app/keys/cert.pem.crt  --key /app/keys/private.pem.key --thingName zigbeeCoordinator --buildingId building_1 --localMqttAddress tcp://172.18.1.2:1883"]