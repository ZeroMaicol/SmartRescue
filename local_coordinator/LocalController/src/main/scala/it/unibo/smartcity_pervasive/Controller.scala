package it.unibo.smartcity_pervasive

import it.unibo.smartcity_pervasive.aws_iot.alarm.AlarmController
import it.unibo.smartcity_pervasive.aws_iot.things.{SensorsController, SensorsSimulator, SimulatedSensor}
import it.unibo.smartcity_pervasive.utils.ArgsUtils
import it.unibo.smartcity_pervasive.zigbee2mqtt.ZigbeeController
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttConnectOptions}
import software.amazon.awssdk.crt.CRT
import software.amazon.awssdk.crt.io.{ClientBootstrap, EventLoopGroup, HostResolver}
import software.amazon.awssdk.crt.mqtt.{MqttClientConnection, MqttClientConnectionEvents, QualityOfService}
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder
import software.amazon.awssdk.iot.iotshadow.IotShadowClient
import software.amazon.awssdk.iot.iotshadow.model.ShadowDeltaUpdatedSubscriptionRequest

import java.util.concurrent.ScheduledThreadPoolExecutor
import scala.sys.exit
import scala.util.Using

object Controller extends App {

  println(args.toList)
  val arguments = ArgsUtils.argMap(Map(), args.toList)
  if ((ArgsUtils.mustSet -- arguments.keySet).nonEmpty) {
    ArgsUtils.printUsage()
    exit
  }

  val certPath = arguments("cert")
  val keyPath = arguments("key")
  val rootCaPath = arguments("rootca")
  val clientId = java.util.UUID.randomUUID.toString
  val endpoint = arguments("endpoint")
  val thingName: String = arguments("thingName")
  val buildingId: String = arguments("buildingId")
  val mqttClientPath: String = arguments("localMqttAddress")


  val localMqttClient = new MqttClient(mqttClientPath, "test")
  val options = new MqttConnectOptions
  options.setAutomaticReconnect(true)
  options.setCleanSession(true)
  options.setConnectionTimeout(10)
  localMqttClient.connect(options)
  ZigbeeController(localMqttClient)


  val callbacks: MqttClientConnectionEvents = new MqttClientConnectionEvents() {
    override def onConnectionInterrupted(errorCode: Int): Unit = {
      if (errorCode != 0) println("Connection interrupted: " + errorCode + ": " + CRT.awsErrorString(errorCode))
    }

    override def onConnectionResumed(sessionPresent: Boolean): Unit = {
      println("Connection resumed: " + (if (sessionPresent) "existing session" else "clean session"))
    }
  }


  Using(new EventLoopGroup(1)) { eventLoopGroup =>
    Using(new HostResolver(eventLoopGroup)) { resolver =>
      Using(new ClientBootstrap(eventLoopGroup, resolver)) { clientBootstrap =>
        Using(AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(certPath, keyPath)) { builder =>
          if (rootCaPath != "") builder.withCertificateAuthorityFromPath(null, rootCaPath)

          builder
            .withClientId(clientId)
            .withEndpoint(endpoint)
            .withCleanSession(true)
            .withConnectionEventCallbacks(callbacks)
            .withBootstrap(clientBootstrap);

          Using(builder.build()) { connection =>
            val shadowClient = new IotShadowClient(connection)
            val connected = connection.connect()
            try {
              val sessionPresent = connected.get()
              println("Connected to " + (if (!sessionPresent) "clean" else "existing") + " session!")
              val threadPoolExecutor = new ScheduledThreadPoolExecutor(2)
              initializeControllers(shadowClient, connection, threadPoolExecutor, thingName, buildingId)
              initializeSimulators(threadPoolExecutor, shadowClient, connection, thingName, buildingId)

              System.in.read()
              exit()
            } catch {
              case ex: Exception =>
                println(ex)
                throw new RuntimeException("Exception occurred during connect", ex)
            }

          }
        }
      }
    }
  }


  def initializeControllers(shadowClient: IotShadowClient,
                            connection: MqttClientConnection,
                            threadPoolExecutor: ScheduledThreadPoolExecutor,
                            thingName: String,
                            buildingName: String): Unit = {

    AlarmController.start(shadowClient, threadPoolExecutor, buildingName)
    SensorsController.start(shadowClient, connection, thingName, buildingName)
  }

  def initializeSimulators(threadPoolExecutor: ScheduledThreadPoolExecutor,
                           shadowClient: IotShadowClient,
                           connection: MqttClientConnection,
                           thingName: String,
                           buildingName: String): Unit = {

    //todo parse from adjacency matrix
    SensorsSimulator.simulateSmokeSensors(
      withSmokeOn = Set(
        SimulatedSensor("Start", -78.500317, 38.031992),
        SimulatedSensor("A", -78.500505, 38.032072),
        SimulatedSensor("B", -78.500577, 38.031817),
        SimulatedSensor("Exit", -78.499473, 38.032102)),
      withoutSmokeOn = Set(SimulatedSensor("C", -78.499944, 38.031599)),
      poolExecutor = threadPoolExecutor,
      shadowClient = shadowClient,
      mqttConnection = connection,
      buildingId = buildingName,
      thingName = thingName)
  }

}
