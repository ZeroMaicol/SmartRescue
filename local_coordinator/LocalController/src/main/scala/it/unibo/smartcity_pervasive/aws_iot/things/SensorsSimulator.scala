package it.unibo.smartcity_pervasive.aws_iot.things

import it.unibo.smartcity_pervasive.aws_iot.alarm.AlarmController
import it.unibo.smartcity_pervasive.aws_iot.things.SensorsController.{sensors, shadowClient, thingId}
import it.unibo.smartcity_pervasive.zigbee2mqtt.MqttMessageParser
import software.amazon.awssdk.crt.mqtt.{MqttClientConnection, MqttMessage, QualityOfService}
import software.amazon.awssdk.iot.iotshadow.IotShadowClient
import software.amazon.awssdk.iot.iotshadow.model.{ShadowState, UpdateNamedShadowRequest}

import java.text.DecimalFormat
import java.util
import java.util.TimerTask
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}
import scala.jdk.CollectionConverters.MapHasAsJava
import scala.util.Random

object SensorsSimulator {

  case class ShadowExampleJson(name: String, longitude: Double, latitude: Double) {
    private val formatter = new DecimalFormat("#.#############")
    val json = s"""
      "$name": {
            "properties": {
              "connection": "wired",
              "date_code": "20170314",
              "description": "Simulated Smoke Sensor",
              "model": "JTYJ-GD-01LM/BW",
              "vendor": "Xiaomi",
              "friendly_name": "$name",
              "ieee_address": "$name",
              "model_id": "simulated.sensor_smoke",
              "network_address": 37966,
              "power_source": "AC",
              "software_build_id": "3000-0001",
              "supported": true,
              "type": "simulated smoke detector",
              "lat": ${formatter.format(latitude)},
              "long": ${formatter.format(longitude)},
              "zFloor": 1,
              "img_path": "https://api.iconify.design/mdi-smoke-detector.svg"
            },
            "exposes": {
              "smoke": {
                "access": 1,
                "description": "Indicates whether the device detected smoke",
                "name": "smoke",
                "property": "smoke",
                "type": "binary",
                "value_off": false,
                "value_on": true
              },
              "battery_low": {
                "access": 1,
                "description": "Indicates if the battery of this device is almost empty",
                "name": "battery_low",
                "property": "battery_low",
                "type": "binary",
                "value_off": false,
                "value_on": true
              },
              "battery": {
                "access": 1,
                "description": "Remaining battery in %",
                "name": "battery",
                "property": "battery",
                "type": "numeric",
                "unit": "%",
                "value_max": 100,
                "value_min": 0
              },
              "smoke_density": {
                "access": 1,
                "name": "smoke_density",
                "property": "smoke_density",
                "type": "numeric"
              }
            }
          }
      """
  }
  private def registerSmokeSensors(names: Set[SimulatedSensor], mqttConnection: MqttClientConnection, buildingId: String, thingId: String): Unit = {
    val shadows = names.map { simulatedSensor =>
      ShadowExampleJson(name = simulatedSensor.name,
        latitude = simulatedSensor.latitude,
        longitude = simulatedSensor.longitude).json
    }.mkString(",")

    val msg = s"""{ "buildingId": "$buildingId", "thingId": "$thingId", "shadows" : {$shadows} } """

    println("sending simulated devices to lambda, to update dynamoDB")
    val mqttMessage = new MqttMessage("iot/devices", msg.getBytes)
    mqttConnection.publish(mqttMessage, QualityOfService.AT_LEAST_ONCE, false).get()
  }

  private def smokeSensorTask(name: String, thingName: String, smokeOn: Boolean, shadowClient: IotShadowClient): TimerTask = {
    new TimerTask {
      var battery: Int = 100

      override def run(): Unit = {
        battery = if (Random.nextBoolean()) battery else Math.max(battery - 1,0)
        val batteryLow = battery < 15
        val smoke = if (smokeOn) Random.nextFloat() > 0.9 else false
        val smokeDensity = if (smoke) Random.nextInt(100) else 0

        val updateMap = new util.HashMap[String,Object]() {
          put("battery", java.lang.Integer.valueOf(battery));
          put("battery_low", java.lang.Boolean.valueOf(batteryLow));
          put("smoke", java.lang.Boolean.valueOf(smoke));
          put("smoke_density", java.lang.Integer.valueOf(smokeDensity))
        }
        if (smoke) AlarmController.startAlarm()
        updateAwsShadow(name, thingName, updateMap, shadowClient)
      }
    }
  }

  def updateAwsShadow(shadowName: String, thingName: String, updateMap: util.HashMap[String,Object], shadowClient: IotShadowClient): Unit = {
    val request = new UpdateNamedShadowRequest()
    request.thingName = thingName
    request.shadowName = shadowName
    request.state = new ShadowState()
    request.state.reported = updateMap
    request.state.desired =  new util.HashMap[String,Object](updateMap)

    shadowClient.PublishUpdateNamedShadow(request, QualityOfService.AT_LEAST_ONCE)
      .thenRun(() => println(s"Updated namedShadow $shadowName"))
//      .get()
  }


  def simulateSmokeSensors( withSmokeOn: Set[SimulatedSensor],
                            withoutSmokeOn: Set[SimulatedSensor],
                            poolExecutor: ScheduledThreadPoolExecutor,
                            shadowClient: IotShadowClient,
                            mqttConnection: MqttClientConnection,
                            buildingId: String,
                            thingName: String): Unit = {

    registerSmokeSensors(withoutSmokeOn ++ withSmokeOn, mqttConnection, buildingId, thingName)

    val tasks = withSmokeOn.map { s => smokeSensorTask(s.name, thingName, smokeOn = true, shadowClient) } ++
      withoutSmokeOn.map { s => smokeSensorTask(s.name, thingName, smokeOn = false, shadowClient) }

    tasks.foreach(poolExecutor.scheduleAtFixedRate(_,1,1,TimeUnit.SECONDS))
  }
}

case class SimulatedSensor(name: String, longitude: Double, latitude: Double)
