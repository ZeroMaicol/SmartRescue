package it.unibo.smartcity_pervasive.aws_iot.alarm

import it.unibo.smartcity_pervasive.aws_iot.alarm.DijkstraAlarm.Path
import it.unibo.smartcity_pervasive.aws_iot.things.SensorsController
import software.amazon.awssdk.crt.mqtt.{MqttClientConnection, QualityOfService}
import software.amazon.awssdk.iot.iotshadow.IotShadowClient
import software.amazon.awssdk.iot.iotshadow.model.{ShadowDeltaUpdatedEvent, ShadowDeltaUpdatedSubscriptionRequest, ShadowState, UpdateShadowRequest}

import java.util
import java.util.concurrent.{ScheduledFuture, ScheduledThreadPoolExecutor, TimeUnit}
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

object AlarmController {

  private var alarm: Boolean = false
  def alarmState: Boolean = alarm

  private var shadowClient: Option[IotShadowClient] = None
  private var mqttConnection: Option[MqttClientConnection] = None
  private var thingId: String = ""
  var poolExecutor: Option[ScheduledThreadPoolExecutor] = None
  var task: Option[ScheduledFuture[_]] = None
  var alarmStartPath: String = ""
  var currentPath: List[(String, String)] = List()

  val runDijkstra: Runnable = () => {
    println("running dijkstra alarm")
    val safeSensors = SensorsController.sensors.filter { case (_,shadow) =>
      shadow.reported.get("smoke") match {
        case Some(java.lang.Boolean.FALSE) => true
        case _ => false
      }
    }.keySet

    val currentAlarmStartPath = alarmStartPath
    val paths: ListBuffer[Path] = buildPathsFromAdjacencyMatrix(safeSensors, currentAlarmStartPath)
    if (paths.size > 1) {
      DijkstraAlarm.runDijkstra(paths.head.vertex, paths.last.vertex, paths) match {
        case Some(optimalPathReverse) =>
          //instead of having a,b,c,d,e it's (a,b),(b,c),(c,d),(d,e)
//          val optimalPath = optimalPathReverse.reverse
          val optimalPath = ListBuffer("Start","B","A","Exit")
          val twoByTwoPath = optimalPath.dropRight(1).zip(optimalPath.drop(1)).toList
          AlarmController.updateOptimalPath(twoByTwoPath)
      }
    }

    //todo remove
    val optimalPath = ListBuffer("Start","B","A","Exit")
    val twoByTwoPath = optimalPath.dropRight(1).zip(optimalPath.drop(1)).toList
    AlarmController.updateOptimalPath(twoByTwoPath)
  }


  private def buildPathsFromAdjacencyMatrix(safeSensors: Set[String], from: String): ListBuffer[Path] = {
    val adjacencyMatrix = SensorsController.adjacencyMatrix
    var pathsToBeAdded: Set[String] = Set(from)
    val paths: ListBuffer[Path] = ListBuffer(Path(from,from,0,label = true))
    while (pathsToBeAdded.nonEmpty) {
      val currentNode = pathsToBeAdded.head
      val list = adjacencyMatrix.filter { case((p1,p2), _) => p1 == currentNode && safeSensors.contains(p2) }
      list.foreach { case ((p1, p2), weight) =>
        val path = Path(vertex = p2, from = p1, weight = weight, label = false)
        paths.addOne(path)
        pathsToBeAdded = pathsToBeAdded + p2
      }
      pathsToBeAdded = pathsToBeAdded - currentNode
    }
    paths
  }



  def start(client: IotShadowClient,
            threadPoolExecutor: ScheduledThreadPoolExecutor,
            buildingName: String
           ): Unit = {
    shadowClient = Some(client)
    thingId = s"alarm_$buildingName"
    poolExecutor = Some(threadPoolExecutor)

    println(s"subscribing to $thingId")

    val requestShadowDeltaUpdated: ShadowDeltaUpdatedSubscriptionRequest = new ShadowDeltaUpdatedSubscriptionRequest
    requestShadowDeltaUpdated.thingName = thingId
    client.SubscribeToShadowDeltaUpdatedEvents(
      requestShadowDeltaUpdated,
      QualityOfService.AT_MOST_ONCE,
      onShadowUpdate)
      .get()
  }


  def onShadowUpdate(deltaEvent: ShadowDeltaUpdatedEvent): Unit = {
    println("received delta event from alarm shadow")
    deltaEvent.state.asScala.get("alarm") match {
      case Some(java.lang.Boolean.TRUE) => startAlarm()
      case Some(java.lang.Boolean.FALSE) => stopAlarm()
      case _ => print(_)
    }
  }

  def startAlarm(): Unit = if (!alarm) {
    setAlarmState(true)
    println("starting alarm")
    alarm = true
    (poolExecutor, task) match {
      case (Some(ex), None) =>
        task = Some(ex.scheduleAtFixedRate(runDijkstra,1,1,TimeUnit.SECONDS))
      case _ =>
    }
  }

  def stopAlarm(): Unit = if (alarm) {
    println("stopping alarm")

    setAlarmState(false)
    task match {
      case Some(t) =>
        t.cancel(false)
        task = None
      case None =>
    }
  }

  private def setAlarmState(alarmState: Boolean): Unit = {
    alarm = alarmState
    val request = new UpdateShadowRequest()
    request.thingName = thingId
    request.state = new ShadowState()
    request.state.reported = new util.HashMap[String, Object] { put("alarm", java.lang.Boolean.valueOf(alarm)) }
    request.state.desired = new util.HashMap[String,Object](request.state.reported)
    println("updating alarm shadow")
    shadowClient.head.PublishUpdateShadow(request, QualityOfService.AT_LEAST_ONCE)
      .thenRun(() => println(s"Updated alarm shadow"))
      .exceptionally((ex: Throwable) => { println("Update request failed: " + ex.getMessage); null })
//      .get(5, TimeUnit.SECONDS) //dunno why but it stops here with this
  }

  def updateOptimalPath(path: List[(String,String)]): Unit = this.synchronized {
    if (path != currentPath) {
      val request = new UpdateShadowRequest()
      request.thingName = thingId
      request.state = new ShadowState()
      request.state.reported = new util.HashMap[String, Object] {
        put("escape_path", path.toArray)
      }
      shadowClient.head.PublishUpdateShadow(request, QualityOfService.AT_LEAST_ONCE)
        .thenRun(() => {
          println(s"Updated optimal path with $path")
          currentPath = path
        })
    }
  }

}
