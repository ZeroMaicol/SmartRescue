package it.unibo.smartcity_pervasive.aws_iot.alarm

import software.amazon.awssdk.crt.mqtt.{MqttClientConnection, QualityOfService}
import software.amazon.awssdk.iot.iotshadow.IotShadowClient
import software.amazon.awssdk.iot.iotshadow.model.{ShadowDeltaUpdatedEvent, ShadowDeltaUpdatedSubscriptionRequest, ShadowState, UpdateShadowRequest}

import java.util
import scala.jdk.CollectionConverters._

object AlarmController {

  private var alarm: Boolean = false
  def alarmState: Boolean = alarm

  private var shadowClient: Option[IotShadowClient] = None
  private var mqttConnection: Option[MqttClientConnection] = None
  private var thingId: String = ""
  private var buildingId: String = ""


  def start(client: IotShadowClient, connection: MqttClientConnection, thingName: String, buildingName: String): Unit = {
    shadowClient = Some(client)
    mqttConnection = Some(connection)
    thingId = thingName
    buildingId = buildingName

    val requestShadowDeltaUpdated: ShadowDeltaUpdatedSubscriptionRequest = new ShadowDeltaUpdatedSubscriptionRequest
    requestShadowDeltaUpdated.thingName = thingId
    client.SubscribeToShadowDeltaUpdatedEvents(
      requestShadowDeltaUpdated,
      QualityOfService.AT_LEAST_ONCE,
      onShadowUpdate)
      .get
  }


  def onShadowUpdate(deltaEvent: ShadowDeltaUpdatedEvent): Unit = {
    deltaEvent.state.asScala.get("alarm") match {
      case Some(java.lang.Boolean.TRUE) => startAlarm()
      case Some(java.lang.Boolean.FALSE) => stopAlarm()
      case _ =>
    }
  }

  def startAlarm(): Unit = if (!alarm) {
    setAlarmState(true)
    DijkstraAlarm.startAlarm()
  }

  def stopAlarm(): Unit = if (alarm) {
    setAlarmState(false)
    DijkstraAlarm.stopAlarm()
  }

  private def setAlarmState(alarmState: Boolean): Unit = {
    alarm = alarmState
    val request = new UpdateShadowRequest()
    request.thingName = thingId
    request.state = new ShadowState()
    request.state.reported = new util.HashMap[String, Object] { put("alarm", java.lang.Boolean.valueOf(alarm)) }
    shadowClient.head.PublishUpdateShadow(request, QualityOfService.AT_LEAST_ONCE)
      .thenRun(() => println(s"Updated alarm shadow"))
      .get()
  }

}
