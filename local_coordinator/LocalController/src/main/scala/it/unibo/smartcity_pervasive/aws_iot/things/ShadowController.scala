package it.unibo.smartcity_pervasive.aws_iot.things

import it.unibo.smartcity_pervasive.aws_iot.things.SensorsController.{buildingId, mqttConnection, onShadowUpdate, sensors, shadowClient, thingId}
import software.amazon.awssdk.crt.mqtt.{MqttClientConnection, QualityOfService}
import software.amazon.awssdk.iot.iotshadow.IotShadowClient
import software.amazon.awssdk.iot.iotshadow.model.ShadowDeltaUpdatedSubscriptionRequest

object ShadowController {

  private var shadowClient: Option[IotShadowClient] = None
  private var thingId: String = ""
  private var buildingId: String = ""

  def start(client: IotShadowClient, connection: MqttClientConnection, thingName: String, buildingName: String): Unit = {
//    shadowClient = Some(client)
//    mqttConnection = Some(connection)
//    thingId = thingName
//    buildingId = buildingName
//    sensors.keys.foreach { shadowName =>
//      val requestShadowDeltaUpdated: ShadowDeltaUpdatedSubscriptionRequest = new ShadowDeltaUpdatedSubscriptionRequest
//      requestShadowDeltaUpdated.thingName = thingName
//      client.SubscribeToShadowDeltaUpdatedEvents(
//        requestShadowDeltaUpdated,
//        QualityOfService.AT_LEAST_ONCE,
//        event => onShadowUpdate(shadowName, event))
//        .get
//    }
  }
}
