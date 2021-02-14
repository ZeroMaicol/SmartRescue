package it.unibo.smartcity_pervasive.aws_iot.things

import it.unibo.smartcity_pervasive.aws_iot.model.{Binary, MqttDeviceInfo, NamedShadow, NumericUnlimited, NumericWithRange, PropertyEnum, SmokeDetector}
import org.json4s.jvalue2extractable
import software.amazon.awssdk.crt.mqtt.{MqttClientConnection, MqttMessage, QualityOfService}
import software.amazon.awssdk.iot.iotshadow.IotShadowClient
import software.amazon.awssdk.iot.iotshadow.model.{ShadowDeltaUpdatedEvent, ShadowDeltaUpdatedSubscriptionRequest, ShadowState, UpdateNamedShadowRequest}

import scala.jdk.CollectionConverters._
import java.util
import scala.util.Try

object SensorsController {
  implicit def funToRunnable(fun: () => Unit): Runnable = new Runnable() {
    def run(): Unit = fun()
  }


  //todo init both
  var sensors: Map[String, NamedShadow] = Map()
  var adjacencyMatrix: Map[(String, String), Int] = Map()
  private var shadowClient: Option[IotShadowClient] = None
  private var mqttConnection: Option[MqttClientConnection] = None
  private var thingId: String = ""
  private var buildingId: String = ""


  def start(client: IotShadowClient, connection: MqttClientConnection, thingName: String, buildingName: String): Unit = {
    shadowClient = Some(client)
    mqttConnection = Some(connection)
    thingId = thingName
    buildingId = buildingName
  }


  def listenForDeltaUpdates(newSensors: Set[String]): Unit = shadowClient match {
    case None =>
    case Some(client) =>
      (newSensors -- sensors.keySet).foreach { shadowName =>
        val requestShadowDeltaUpdated: ShadowDeltaUpdatedSubscriptionRequest = new ShadowDeltaUpdatedSubscriptionRequest
        requestShadowDeltaUpdated.thingName = thingId
        client.SubscribeToShadowDeltaUpdatedEvents(
          requestShadowDeltaUpdated,
          QualityOfService.AT_LEAST_ONCE,
          event => onShadowUpdate(shadowName, event))
          .get
      }
  }


  def registerDevices(devices: Map[String, MqttDeviceInfo]): Unit = {
    updateDynamoDB(devices)
    val newDevices = devices.flatMap { case (name, info) =>
      if (info.exposes.contains("smoke")) {
        val reported: Map[String, AnyRef] = info.exposes.flatMap { case (name, sensorProperty) =>
          sensorProperty match {
            case _ if !sensorProperty.readable => List()
            case _: Binary => List((name,java.lang.Boolean.FALSE))
            case _: NumericUnlimited => List((name, java.lang.Integer.valueOf(0)))
            case n: NumericWithRange => List((name, java.lang.Integer.valueOf(n.value_min)))
            case e: PropertyEnum => List((name, e.values.headOption.getOrElse("")))
            case _ => List()
          }
        }

        val desired = reported
        val smokeDetector = SmokeDetector(
          name = name,
          description = info.properties.getOrElse("description", "").toString,
          model = info.properties.getOrElse("model", "").toString,
          ieee_address = info.properties.getOrElse("ieee_address", "").toString,
          exposes = info.exposes,
          reported = reported,
          desired = desired,
          properties = info.properties
        )
        List((name, smokeDetector))
      } else {
        List()
      }
    }

    sensors = sensors ++ newDevices
    listenForDeltaUpdates(newDevices.keySet)
  }

  import org.json4s._
  import org.json4s.native.Serialization._
  import org.json4s.native.Serialization
  implicit val formats = Serialization.formats(NoTypeHints)
  def updateDynamoDB(newDevices: Map[String, MqttDeviceInfo]): Unit = mqttConnection match {
    case Some (connection) =>
      val shadows = newDevices.map {
        case (name, info) =>
        val properties = write (info.properties)
        val exposes = write (info.exposes)
        val shadow = s"""{ s"$name": { "properties": $properties, "exposes": $exposes }}"""
        shadow
      }.mkString (",")
      val msg = s"""{ "buildingId": "$buildingId", "thingId": "$thingId", "shadows" : {$shadows} } """
      println ("sending devices to lambda, to update dynamoDB")
      val mqttMessage = new MqttMessage ("iot/devices", msg.getBytes)
      //connection.publish(mqttMessage, QualityOfService.AT_LEAST_ONCE, false).get()
      println (msg)
    case None =>
}


  def updateNamedShadow(shadowName: String, shadow: NamedShadow): Unit =
    sensors.get(shadowName) match {
      case None =>
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

  def onZigbeeDataUpdate(name: String, updateMap: Map[String, String]): Unit = sensors.get(name) match {
    case None =>
    case Some(namedShadow) =>
      val newReported = updateMap.foldRight(namedShadow.reported) { case ((pName,pValue),acc) =>
        pValue match {
          case _ if scala.util.Try(pValue.toDouble).isSuccess => acc.updated(pName, java.lang.Double.valueOf(pValue))
          case "true" => acc.updated(pName, java.lang.Boolean.TRUE)
          case "false" => acc.updated(pName, java.lang.Boolean.FALSE)
          case _ => acc.updated(pName, pValue)
        }
      }
      val newShadow = namedShadow.update(newReported)
      updateNamedShadow(name, newShadow)
  }

  def addSensor(name: String, sensor: NamedShadow): Unit = sensors = sensors + (name -> sensor)


  def parseAdjacencyMatrix(stringMatrix: String): Unit = {
    adjacencyMatrix = Map()
  }


}
