package it.unibo.smartcity_pervasive

import it.unibo.smartcity_pervasive.aws_iot.alarm.AlarmController
import it.unibo.smartcity_pervasive.aws_iot.things.SensorsController
import it.unibo.smartcity_pervasive.utils.{ArgsUtils, Simulators}
import it.unibo.smartcity_pervasive.zigbee2mqtt.ZigbeeController
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttConnectOptions}
import software.amazon.awssdk.crt.CRT
import software.amazon.awssdk.crt.io.{ClientBootstrap, EventLoopGroup, HostResolver}
import software.amazon.awssdk.crt.mqtt.{MqttClientConnection, MqttClientConnectionEvents}
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder
import software.amazon.awssdk.iot.iotshadow.IotShadowClient

import java.util.Timer
import java.util.concurrent.CompletableFuture
import scala.sys.exit
import scala.util.Using

object Controller extends App {

  val arguments = ArgsUtils.argMap(Map(), args.toList)
  if ((ArgsUtils.settables -- arguments.keySet).nonEmpty) {
    ArgsUtils.printUsage()
    exit
  }

  val localMqttClient = new MqttClient("tcp://127.0.0.1:1883", "test")
  val options = new MqttConnectOptions
  options.setAutomaticReconnect(true)
  options.setCleanSession(true)
  options.setConnectionTimeout(10)
  localMqttClient.connect(options)
  ZigbeeController(localMqttClient)

  val simulators = Set("0x10158d0003467624", "0x20158d0003467624")
  val timer = new Timer()
  simulators.map { s => Simulators.simulateSmokeSensor(s) }
    .foreach { t => timer.schedule(t, 5000L) }


  val callbacks: MqttClientConnectionEvents = new MqttClientConnectionEvents() {
    override def onConnectionInterrupted(errorCode: Int): Unit = {
      if (errorCode != 0) println("Connection interrupted: " + errorCode + ": " + CRT.awsErrorString(errorCode))
    }

    override def onConnectionResumed(sessionPresent: Boolean): Unit = {
      println("Connection resumed: " + (if (sessionPresent) "existing session" else "clean session"))
    }
  }

  val certPath = arguments("certPath")
  val keyPath = arguments("keyPath")
  val rootCaPath = arguments("rootCaPath")
  val clientId = arguments("clientId")
  val endpoint = arguments("endpoint")
  val thingId: String = arguments("thingId")
  val buildingId: String = arguments("buildingId")

  Using(new EventLoopGroup(1)) { eventLoopGroup =>
    Using(new HostResolver(eventLoopGroup)) { resolver =>
      Using(new ClientBootstrap(eventLoopGroup, resolver)) { clientBootstrap =>
        Using(AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(certPath, keyPath)) { builder =>
          if (rootCaPath != null) builder.withCertificateAuthorityFromPath(null, rootCaPath)

          AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(certPath, keyPath)
            .withClientId(clientId)
            .withEndpoint(endpoint)
            .withCleanSession(true)
            .withConnectionEventCallbacks(callbacks)
            .withBootstrap(clientBootstrap);

          Using(builder.build()) { connection =>
            val connected = connection.connect
            try {
              val sessionPresent = connected.get
              println("Connected to " + (if (!sessionPresent) "clean" else "existing") + " session!")
              initializeControllers(new IotShadowClient(connection), connection, thingId, buildingId)
            } catch {
              case ex: Exception =>
                throw new RuntimeException("Exception occurred during connect", ex)
            }

          }
        }
      }
    }
  }

  System.in.read()


  def initializeControllers(shadowClient: IotShadowClient,
                            connection: MqttClientConnection,
                            thingName: String,
                            buildingName: String): Unit = {

    AlarmController.start(shadowClient, connection, thingName, buildingName)
    SensorsController.start(shadowClient, connection, buildingName, buildingName)
  }
}
