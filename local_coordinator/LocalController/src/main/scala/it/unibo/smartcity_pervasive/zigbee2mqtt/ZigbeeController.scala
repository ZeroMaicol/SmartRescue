package it.unibo.smartcity_pervasive.zigbee2mqtt

import it.unibo.smartcity_pervasive.aws_iot.things.AwsThing
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttMessage}


case class ZigbeeController(mqttClient: MqttClient) {
  val topicPrefix: String = "/zigbee2mqtt"

  mqttClient.subscribe(s"$topicPrefix/bridge/devices", (_: String, mqttMessage: MqttMessage) => {
    val devices = MqttMessageParser.fromMqttMessage(mqttMessage).parseDevices
    devices.keySet.foreach(subscribeToDeviceUpdates)
    devices.foreach { case (_, info) => AwsThing.createNamedShadow(info) }
  })

//  val requestDevicesMessage = new MqttMessage()
//  requestDevicesMessage.setQos(1)
//  mqttClient.publish("/zigbee2mqtt/bridge/config/devices/get", requestDevicesMessage)


  def subscribeToDeviceUpdates(name: String): Unit = {
    mqttClient.subscribe(s"$topicPrefix/$name", (_: String, mqttMessage: MqttMessage) => {
      val updateMap = MqttMessageParser.fromMqttMessage(mqttMessage).fromUpdateToMap
      AwsThing.updateNamedShadow(name, updateMap)
    })
  }
}
