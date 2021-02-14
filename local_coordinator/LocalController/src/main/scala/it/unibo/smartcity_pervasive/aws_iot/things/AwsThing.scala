package it.unibo.smartcity_pervasive.aws_iot.things

import it.unibo.smartcity_pervasive.aws_iot.model.{MqttDeviceInfo, NamedShadow}

//object AwsThing {
//
//  var namedShadows: Map[String, NamedShadow] = Map()
//
//  def init(): Unit = {
//    namedShadows = ???
//  }
//
//  def updateNamedShadow(name: String, reported: Map[String, Any]): Unit = namedShadows.get(name) match {
//    case Some(shadow) =>
//      reported.foreach { case (propertyName, value) =>
//        //        if (shadow.exposes.exists(_.name == propertyName)) {
//        //        }
//      }
//    case None =>
//  }
//
//  def createNamedShadow(deviceInfo: MqttDeviceInfo): Unit = {
//    val namedShadow: NamedShadow = ???
//    //instantiate named shadow
//    namedShadows = namedShadows + (deviceInfo.friendlyName -> namedShadow)
//    //publish MqttDeviceInfo to DynamoDB
//  }
//}
