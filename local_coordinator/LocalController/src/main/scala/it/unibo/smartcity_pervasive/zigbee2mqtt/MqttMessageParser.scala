package it.unibo.smartcity_pervasive.zigbee2mqtt


import it.unibo.smartcity_pervasive.aws_iot.model.{Binary, PropertyEnum, MqttDeviceInfo, NumericUnlimited, NumericWithRange, SensorProperty, SmokeDetector}
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json4s._
import org.json4s.native.JsonMethods._

object MqttMessageParser {
  def fromMqttMessage(payload: MqttMessage): MqttMessageParser = new MqttMessageParser(payload.toString)
}

case class MqttMessageParser(message: String) {
  val parsed: JValue = parse(message)

  def eventType: String = try {
    val JString(eType) = parsed \ "type"
    eType
  } catch {
    case _: MappingException => ""
  }

  def interviewSuccessful: Boolean = try {
    val JString(status) =  parsed \ "data" \ "status"
    status == "successful"
  } catch {
    case _: MappingException => false
  }


  def parseDevices: Map[String,MqttDeviceInfo] = {
    val JArray(deviceList) = parsed
    deviceList.flatMap { case JObject(device) =>

      val attributes: Map[String, AnyRef] = device.flatMap { case (property, value) =>
        value match {
          case JString(s) => List((property,s))
          case JInt(i) => List((property, Int.box(i.toInt)))
          case JObject(_) if property == "definition" => parseDefinition(value).toList
          case _ => List()
        }
      }.toMap

      val devices = (attributes.get("type"), attributes.get("friendly_name"), MqttDeviceInfo.fromAttributes(attributes)) match {
        case (Some(deviceType), Some(name), Some(deviceInfo)) if deviceType != "Coordinator" && name.isInstanceOf[String] =>
          List((name.asInstanceOf[String], deviceInfo))
        case _ =>
          List()
      }

      devices
    }.toMap
  }

  def parseJsonToMap: Option[Map[String,Any]] = {
    implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
    try { Some(parsed.extract[Map[String, Any]]) }
    catch { case x: Exception => None }
  }

  def parseDefinition(definitionValue: JValue): Map[String, AnyRef] = {
    val JObject(list) = definitionValue
    list.flatMap { case(defProperty, defValue) =>
      defValue match {
        case JString(s2) => List((defProperty,s2))
        case JInt(i2) => List((defProperty,i2))
        case JArray(_) if defProperty == "exposes" => List((defProperty, parseExposes(defValue)))
        case _ => List()
      }
    }.toMap
  }

  //{"data":{"definition":{"description":"MiJia Honeywell smoke detector","exposes":[{"access":1,"description":"Indicates whether the device detected smoke","name":"smoke","property":"smoke","type":"binary","value_off":false,"value_on":true},{"access":1,"description":"Indicates if the battery of this device is almost empty","name":"battery_low","property":"battery_low","type":"binary","value_off":false,"value_on":true},{"access":1,"description":"Indicates whether the device is tampered","name":"tamper","property":"tamper","type":"binary","value_off":false,"value_on":true},{"access":1,"description":"Remaining battery in %","name":"battery","property":"battery","type":"numeric","unit":"%","value_max":100,"value_min":0},{"access":3,"name":"sensitivity","property":"sensitivity","type":"enum","values":["low","medium","high"]},{"access":1,"name":"smoke_density","property":"smoke_density","type":"numeric"},{"access":2,"name":"selftest","property":"selftest","type":"enum","values":[""]},{"access":1,"description":"Link quality (signal strength)","name":"linkquality","property":"linkquality","type":"numeric","unit":"lqi","value_max":255,"value_min":0}],"model":"JTYJ-GD-01LM/BW","vendor":"Xiaomi"},"friendly_name":"0x00158d0003467624","ieee_address":"0x00158d0003467624","status":"successful","supported":true},"type":"device_interview"}
  def parseSmokeDetector: Option[SmokeDetector] = try {
    val data = parsed \ "data"

    parsed \ "data" \ "status" match {
      case JString(status) if status == "successful" =>
        val definition = data \ "definition"
        val JString(description) = definition \ "description"
        val JString(model) = definition \ "model"

        val JString(ieee_address) = data \ "ieee_address"
        val JString(name) = data \ "friendly_name"

        val exposes = parseExposes(definition \ "exposes")

        val smokeDetector = SmokeDetector(
          name = name,
          description = description,
          model = model,
          ieee_address = ieee_address,
          exposes = exposes,
          desired = Map(),
          reported = Map(),
          properties = Map()
        )
        Some(smokeDetector)

      case _ => None
    }
  } catch {
    case _: MappingException => None
  }

  def parseExposes(exposesValue: JValue): Map[String,SensorProperty] = {
    val JArray(exposes) = exposesValue
//    exposes.foreach(println)
//    println
    val stringProperties = Set("description", "property", "type", "unit")
    val intProperties = Set("value_max", "value_min", "access")
    val boolProperties = Set("value_on", "value_off")
    val arrayProperties = Set("values")

    val properties: Map[String,SensorProperty] = exposes.flatMap { case JObject(propertyAttributeList) =>
      val map = propertyAttributeList.flatMap { case (name, value) =>
        value match {
          case JString(s) if stringProperties.contains(name) =>
            List((name, s))
          case JInt(i) if intProperties.contains(name) =>
            List((name, i))
          case JBool(b) if boolProperties.contains(name) =>
            List((name, b))
          case JArray(l) if arrayProperties.contains(name) =>
            val fixedList = l.map { case JString(s) => s }
            List((name, fixedList))
          case _ => List()
        }
      }.toMap

      val exposes = (map.get("type"), map.get("property")) match {
        case (Some(propertyType), Some(propertyName)) =>
          val desc = map.getOrElse("description", "").asInstanceOf[String]
          val name = propertyName.asInstanceOf[String]
          val access = map.getOrElse("access", 1).asInstanceOf[BigInt].toInt

          //access is a 3-bit bitmask stating that:
          //if bit 1 is set => the property will be found in the updates
          //if bit 2 is set => the property can be set
          //if bit 3 is set => the property can be read
          val inUpdates = ((access & 1 << 1) != 0)
          val writable = ((access & 1 << 2) != 0)
          val readable = ((access & 1 << 3) != 0)

          propertyType match {
            case "binary" =>
              List(Binary(
                name = name,
                description = desc,
                inUpdates = inUpdates,
                readable = readable,
                writable = writable,
                value_off = map.getOrElse("value_off", "false").asInstanceOf[Boolean].toString,
                value_on = map.getOrElse("value_on", "true").asInstanceOf[Boolean].toString,
              ))

            case "numeric" =>
              val unit = map.getOrElse("unit", "").asInstanceOf[String]
              val property = (map.get("value_max"), map.get("value_max")) match {
                case (Some(max), Some(min))
                  if (max.isInstanceOf[Int] && min.isInstanceOf[Int]) => NumericWithRange(
                  description = desc,
                  name = name,
                  unit = unit,
                  inUpdates = inUpdates,
                  readable = readable,
                  writable = writable,
                  value_min = min.asInstanceOf[Int],
                  value_max = max.asInstanceOf[Int]
                )
                case _ => NumericUnlimited(
                  name = name,
                  description = desc,
                  inUpdates = inUpdates,
                  readable = readable,
                  writable = writable,
                  unit = unit
                )
              }
              List(property)

            case "enum" =>
              List(PropertyEnum(
                name = name,
                description = desc,
                inUpdates = inUpdates,
                readable = readable,
                writable = writable,
                values = map.getOrElse("values", List[String]()).asInstanceOf[List[String]]
              ))
          }
        case _ => List()
      }
      exposes
    }.map(t => t.name -> t).toMap

    properties
  }

  //{"ac_status":false,"battery":100,"battery_low":false,"enrolled":false,"linkquality":139,"restore_reports":false,"smoke":false,"smoke_density":0,"supervision_reports":false,"tamper":false,"trouble":false,"voltage":3055}
  def fromFlatJObjectToMap(jObject: JObject): Map[String, String] = try {
    val JObject(elements) = jObject
    elements
    Map[String,String]()
  } catch {
    case _: MappingException => Map()
  }

  def fromUpdateToMap: Map[String, String] = fromFlatJObjectToMap(parsed.asInstanceOf[JObject])
}