package it.unibo.smartcity_pervasive.utils

import it.unibo.smartcity_pervasive.aws_iot.things.AwsThing
import it.unibo.smartcity_pervasive.zigbee2mqtt.MqttMessageParser

import java.util.TimerTask
import scala.util.Random

object Simulators {

  def registerSmokeSensors(names: Set[String], buildingId: String, thingId: String): Unit = {
    val properties = Map
    val map: Map[String, Any] = Map("building_id" -> buildingId, "thing_id" -> thingId)
  }

  def simulateSmokeSensor(name: String): TimerTask = {
    //TODO: publish itself to lambda

    new TimerTask {
      var battery: Int = 100

      override def run(): Unit = {
        val mqttMessage = """{"ac_status":false,"battery":100,"battery_low":false,"enrolled":false,"linkquality":139,"restore_reports":false,"smoke":false,"smoke_density":0,"supervision_reports":false,"tamper":false,"trouble":false,"voltage":3055}"""

        battery = if (Random.nextBoolean()) battery else battery - 1
        val batteryLevel = battery < 15
        val smoke = Random.nextFloat() > 0.9
        val smokeDensity = if (smoke) Random.nextInt(100) else 0
        val updateMap = MqttMessageParser(mqttMessage).fromUpdateToMap
          .updated("battery", battery)
          .updated("battery_low", batteryLevel)
          .updated("smoke", smoke)
          .updated("smoke_density", smokeDensity)

        AwsThing.updateNamedShadow(name, updateMap)
      }
    }
  }
}
