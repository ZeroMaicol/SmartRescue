package it.unibo.smartcity_pervasive.aws_iot.model

import it.unibo.smartcity_pervasive.aws_iot.alarm.AlarmController
import it.unibo.smartcity_pervasive.utils.JsonUtils

trait NamedShadow {
  val name: String
  val exposes: Map[String,SensorProperty]
  val reported: Map[String, AnyRef]
  val desired: Map[String, AnyRef]

  def update(reported: Map[String, AnyRef]): NamedShadow
  def requestChange(desired: Map[String, AnyRef]): NamedShadow
}

trait MqttDevice {
  val name: String
  val properties: Map[String,AnyRef]
  val description: String
  val model: String
  val ieee_address: String
  val exposes: Map[String,SensorProperty]
}

case class MqttDeviceInfo(friendlyName: String,
                          properties: Map[String,AnyRef],
                          exposes: Map[String,SensorProperty]) {

  def toMap(): Map[String, AnyRef] = Map(
    friendlyName -> Map(
      "properties" -> properties,
      "exposes" -> exposes.transform { case(_, p) => JsonUtils.caseClassToMap(p) }
    )
  )
}

object MqttDeviceInfo {
  def fromAttributes(attributes: Map[String,AnyRef]): Option[MqttDeviceInfo] = (attributes.get("friendly_name"), attributes.get("exposes")) match {
    case (Some(name), Some(exposes)) if name.isInstanceOf[String] && exposes.isInstanceOf[Map[String,SensorProperty]] =>
      val properties = attributes - "exposes"
      Some(MqttDeviceInfo(
        friendlyName = name.asInstanceOf[String],
        properties = properties,
        exposes = exposes.asInstanceOf[Map[String,SensorProperty]]))
    case _ => None
  }
}

case class SmokeDetector(name: String,
                         description: String,
                         model: String,
                         ieee_address: String,
                         exposes: Map[String,SensorProperty],
                         reported: Map[String, AnyRef],
                         desired: Map[String, AnyRef],
                         properties: Map[String, AnyRef]
                        ) extends NamedShadow with MqttDevice {

  override def update(newReported: Map[String, AnyRef]): NamedShadow = {
    if (reported.keySet.forall(canChangeReportedProperty)) {
      val updatedReported = newReported.foldLeft(reported)((accMap, entry) => accMap + entry)
      this.copy(reported = updatedReported, desired = updatedReported)
    }
    else this
  }

  override def requestChange(delta: Map[String, AnyRef]): NamedShadow = {
    //should be recursive
    val newReported = delta.foldLeft(reported) { case(accMap, (propertyName, property)) =>
      exposes.get(propertyName) match {
        case Some(propertyInfo) if propertyInfo.writable =>
          changeProperty(propertyName, property) match {
            case Some(reportedProperty) => accMap.updated(propertyName, reportedProperty)
            case None => accMap
          }
        case None => accMap
      }
    }
    if (newReported != reported) update(newReported)
    else this
  }

  def changeProperty(propertyName: String, property: AnyRef): Option[AnyRef] = propertyName match {
    //add rules for specific properties, default is that it can accept any property change
    case "smoke" =>
      val smokePresent = property.asInstanceOf[java.lang.Boolean]
      if (smokePresent) AlarmController.startAlarm()
      Some(property)
    case _ => Some(property)
  }

  def canChangeReportedProperty(propertyName: String): Boolean =
    propertyName match {
      case _ if exposes.values.exists(_.name == propertyName) => true
      case _ => false
    }
}

object SmokeDetector {
  def fromDeviceInfo(deviceInfo: MqttDeviceInfo) : Option[SmokeDetector] =
    if (deviceInfo.exposes.keySet.contains("smoke")) {
      Some(SmokeDetector(
        name = deviceInfo.friendlyName,
        description = deviceInfo.properties.getOrElse("description", "").toString,
        model = deviceInfo.properties.getOrElse("model", "").toString,
        ieee_address = deviceInfo.properties.getOrElse("ieee_address", "").toString,
        exposes = deviceInfo.exposes,
        reported = Map(),
        desired = Map(),
        properties = deviceInfo.properties
      ))
    }
    else None
}






