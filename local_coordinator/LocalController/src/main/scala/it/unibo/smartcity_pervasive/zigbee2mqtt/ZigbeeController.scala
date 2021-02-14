package it.unibo.smartcity_pervasive.zigbee2mqtt

import it.unibo.smartcity_pervasive.aws_iot.things.SensorsController
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttMessage}


case class ZigbeeController(mqttClient: MqttClient) {
  val topicPrefix: String = "/zigbee2mqtt"

  mqttClient.subscribe(s"$topicPrefix/bridge/devices", (_: String, mqttMessage: MqttMessage) => {
    val devices = MqttMessageParser.fromMqttMessage(mqttMessage).parseDevices
    devices.keySet.foreach(subscribeToDeviceUpdates)
    SensorsController.registerDevices(devices)
  })

  def subscribeToDeviceUpdates(name: String): Unit = {
    mqttClient.subscribe(s"$topicPrefix/$name", (_: String, mqttMessage: MqttMessage) => {
      println(s"received message from Zigbee Sensor $name")
      println(mqttMessage.toString)
      val updateMap = MqttMessageParser.fromMqttMessage(mqttMessage).fromUpdateToMap
      SensorsController.onZigbeeDataUpdate(name, updateMap)
    })
  }
}
