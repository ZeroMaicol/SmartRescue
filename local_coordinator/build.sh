#!/bin/bash

ORIG_DIR=$PWD

if ! [ -x "$(command -v docker)" ]; then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    newgrp docker
fi


if [ ! -d "./zigbee-herdsman-converters" ]
then
    git clone https://github.com/Koenkk/zigbee-herdsman-converters.git ./zigbee-herdsman-converters
    cd ./zigbee-herdsman-converters
    docker run -v "$PWD":/app -w /app --rm node:12 npm ci
fi

if ! [ -x "$(command -v docker-compose)" ]; then
    sudo curl -L "https://github.com/docker/compose/releases/download/1.27.4/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi
docker-compose up -d

if [ ! -d "/home/$USER/.keys" ]; then
    echo "no keys found at /home/$USER/.keys"
    exit
fi

docker build -t local_controller ./LocalController
docker run local_controller --endpoint a2spllhdjdikn-ats.iot.eu-central-1.amazonaws.com   --rootca /home/$USER/.keys/AmazonRootCA1.pem --cert /home/$USER/.keys/cert.pem.crt  --key /home/$USER/.keys/private.pem.key --thingName zigbeeCoordinator --buildingId building_1 --localMqttAddress tcp://172.18.1.2:1883
