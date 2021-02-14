# Smart Evacuation

## Tutorial

cd app  
npm install  
npm audit fix  

cd public    
sass scss:css  

cd ../  
node index.js  

NOTA: Le credenziali sono state tolte dal file di configurazione, per poterlo testare sono necessarie.

//Compilare backend Raspberry Pi (richiede maven installato)

cd local_coordinator/LocalController

mvn clean install -DskipTests 

//to execute it locally, you need certificates and a private key

mvn exec:java -X -Dexec.mainClass=it.unibo.smartcity_pervasive.Controller "-Dexec.args=--endpoint a2spllhdjdikn-ats.iot.eu-central-1.amazonaws.com   --rootca /path/to/AmazonRootCA1.pem --cert /path/to/certificate.pem.crt  --key /path/to/private.pem.key --thingName zigbeeCoordinator --buildingId building_1 --localMqttAddress tcp://127.0.0.1:1883"

//to deploy on the raspberry pi (you still need certificates and a private key)

cd ../

./build.sh


## License

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
