package it.unibo.smartcity_pervasive.aws_iot.things

import it.unibo.smartcity_pervasive.aws_iot.model.NamedShadow
import software.amazon.awssdk.crt.mqtt.{MqttClientConnection, QualityOfService}
import software.amazon.awssdk.iot.iotshadow.IotShadowClient
import software.amazon.awssdk.iot.iotshadow.model.{ShadowDeltaUpdatedEvent, ShadowDeltaUpdatedSubscriptionRequest, ShadowState, UpdateNamedShadowRequest}

import scala.jdk.CollectionConverters._
import java.util

object SensorsController {
  implicit def funToRunnable(fun: () => Unit): Runnable = new Runnable() { def run(): Unit = fun() }


  //todo init both
  var sensors: Map[String, NamedShadow] = Map()
  var adjacencyMatrix: Map[(String,String),Int] = Map()
  private var shadowClient: Option[IotShadowClient] = None
  private var mqttConnection: Option[MqttClientConnection] = None
  private var thingId: String = ""
  private var buildingId: String = ""


  def start(client: IotShadowClient, connection: MqttClientConnection, thingName: String, buildingName: String): Unit = {
    shadowClient = Some(client)
    mqttConnection = Some(connection)
    thingId = thingName
    buildingId = buildingName
    sensors.keys.foreach { shadowName =>
      val requestShadowDeltaUpdated: ShadowDeltaUpdatedSubscriptionRequest = new ShadowDeltaUpdatedSubscriptionRequest
      requestShadowDeltaUpdated.thingName = thingName
      client.SubscribeToShadowDeltaUpdatedEvents(
        requestShadowDeltaUpdated,
        QualityOfService.AT_LEAST_ONCE,
        event => onShadowUpdate(shadowName, event))
      .get
    }
  }

  def updateNamedShadow(shadowName: String, shadow: NamedShadow): Unit =
    sensors.get(shadowName) match {
      case Some(localValue) if localValue != shadow =>
        sensors = sensors.updated(shadowName, shadow)
        val request = new UpdateNamedShadowRequest()
        request.thingName = thingId
        request.shadowName = shadowName
        request.state = new ShadowState()
        request.state.reported =  new util.HashMap[String,Object](shadow.reported.asJava)
        request.state.desired =  new util.HashMap[String,Object](shadow.desired.asJava)

        shadowClient.head.PublishUpdateNamedShadow(request, QualityOfService.AT_LEAST_ONCE)
          .thenRun(() => println(s"Updated namedShadow $shadowName"))
          .get()

      case None =>
    }

  def onShadowUpdate(shadowName: String, deltaEvent: ShadowDeltaUpdatedEvent): Unit =
    sensors.get(shadowName) match {
      case Some(shadow) =>
        val delta = deltaEvent.state.asScala.toMap
        val newShadow = shadow.requestChange(delta)
        if (newShadow != shadow) {
          updateNamedShadow(shadowName, newShadow)
        }
      case None =>
    }

  def addSensor(name: String, sensor: NamedShadow): Unit = sensors = sensors + (name -> sensor)


  def parseAdjacencyMatrix(stringMatrix: String): Unit = {
    adjacencyMatrix = Map()
  }


}
