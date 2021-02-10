package mqttTests

import it.unibo.smartcity_pervasive.aws_iot.alarm.AlarmController.alarm
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest._
import matchers._
import it.unibo.smartcity_pervasive.zigbee2mqtt.MqttMessageParser
import org.json4s._
import org.json4s.native.JsonMethods._
import it.unibo.smartcity_pervasive.aws_iot.model.{Binary, Enum, Numeric, NumericWithRange}

import java.util
import scala.jdk.CollectionConverters.{MapHasAsJava, MapHasAsScala}

class MqttMessageParsingSuite extends AnyFlatSpec with should.Matchers {

  behavior of "ZigbeeListener"

  it should "1" in {
    val y = new util.HashMap[String, AnyRef] { put("alarm", Boolean.box(true)) }
    val z = Map[String, AnyRef]("alarm" -> Boolean.box(true))
    val map = z
    println(true.getClass)
  }

  it should "" in {
    val buildingId: String = "building_1"
    val thingId: String = "thing_1"
    val payload = """[
                    |   {
                    |      "date_code":"20170314",
                    |      "definition":{
                    |         "description":"MiJia Honeywell smoke detector",
                    |         "exposes":[
                    |            {
                    |               "access":1,
                    |               "description":"Indicates whether the device detected smoke",
                    |               "name":"smoke",
                    |               "property":"smoke",
                    |               "type":"binary",
                    |               "value_off":false,
                    |               "value_on":true
                    |            },
                    |            {
                    |               "access":1,
                    |               "description":"Indicates if the battery of this device is almost empty",
                    |               "name":"battery_low",
                    |               "property":"battery_low",
                    |               "type":"binary",
                    |               "value_off":false,
                    |               "value_on":true
                    |            },
                    |            {
                    |               "access":1,
                    |               "description":"Indicates whether the device is tampered",
                    |               "name":"tamper",
                    |               "property":"tamper",
                    |               "type":"binary",
                    |               "value_off":false,
                    |               "value_on":true
                    |            },
                    |            {
                    |               "access":1,
                    |               "description":"Remaining battery in %",
                    |               "name":"battery",
                    |               "property":"battery",
                    |               "type":"numeric",
                    |               "unit":"%",
                    |               "value_max":100,
                    |               "value_min":0
                    |            },
                    |            {
                    |               "access":3,
                    |               "name":"sensitivity",
                    |               "property":"sensitivity",
                    |               "type":"enum",
                    |               "values":[
                    |                  "low",
                    |                  "medium",
                    |                  "high"
                    |               ]
                    |            },
                    |            {
                    |               "access":1,
                    |               "name":"smoke_density",
                    |               "property":"smoke_density",
                    |               "type":"numeric"
                    |            },
                    |            {
                    |               "access":2,
                    |               "name":"selftest",
                    |               "property":"selftest",
                    |               "type":"enum",
                    |               "values":[
                    |                  ""
                    |               ]
                    |            },
                    |            {
                    |               "access":1,
                    |               "description":"Link quality (signal strength)",
                    |               "name":"linkquality",
                    |               "property":"linkquality",
                    |               "type":"numeric",
                    |               "unit":"lqi",
                    |               "value_max":255,
                    |               "value_min":0
                    |            }
                    |         ],
                    |         "model":"JTYJ-GD-01LM/BW",
                    |         "vendor":"Xiaomi"
                    |      },
                    |      "endpoints":{
                    |         "1":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |                  "genBasic",
                    |                  "genIdentify",
                    |                  "genMultistateInput",
                    |                  "ssIasZone",
                    |                  "genAnalogInput",
                    |                  "genPowerCfg"
                    |               ],
                    |               "output":[
                    |                  "genOta"
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         }
                    |      },
                    |      "friendly_name":"0x20158d0003467624",
                    |      "ieee_address":"0x20158d0003467624",
                    |      "interview_completed":true,
                    |      "interviewing":false,
                    |      "model_id":"lumi.sensor_smoke",
                    |      "network_address":37965,
                    |      "power_source":"Battery",
                    |      "software_build_id":"3000-0001",
                    |      "supported":true,
                    |      "type":"EndDevice"
                    |   },
                    |   {
                    |      "date_code":"20170314",
                    |      "definition":{
                    |         "description":"MiJia Honeywell smoke detector",
                    |         "exposes":[
                    |            {
                    |               "access":1,
                    |               "description":"Indicates whether the device detected smoke",
                    |               "name":"smoke",
                    |               "property":"smoke",
                    |               "type":"binary",
                    |               "value_off":false,
                    |               "value_on":true
                    |            },
                    |            {
                    |               "access":1,
                    |               "description":"Indicates if the battery of this device is almost empty",
                    |               "name":"battery_low",
                    |               "property":"battery_low",
                    |               "type":"binary",
                    |               "value_off":false,
                    |               "value_on":true
                    |            },
                    |            {
                    |               "access":1,
                    |               "description":"Indicates whether the device is tampered",
                    |               "name":"tamper",
                    |               "property":"tamper",
                    |               "type":"binary",
                    |               "value_off":false,
                    |               "value_on":true
                    |            },
                    |            {
                    |               "access":1,
                    |               "description":"Remaining battery in %",
                    |               "name":"battery",
                    |               "property":"battery",
                    |               "type":"numeric",
                    |               "unit":"%",
                    |               "value_max":100,
                    |               "value_min":0
                    |            },
                    |            {
                    |               "access":3,
                    |               "name":"sensitivity",
                    |               "property":"sensitivity",
                    |               "type":"enum",
                    |               "values":[
                    |                  "low",
                    |                  "medium",
                    |                  "high"
                    |               ]
                    |            },
                    |            {
                    |               "access":1,
                    |               "name":"smoke_density",
                    |               "property":"smoke_density",
                    |               "type":"numeric"
                    |            },
                    |            {
                    |               "access":2,
                    |               "name":"selftest",
                    |               "property":"selftest",
                    |               "type":"enum",
                    |               "values":[
                    |                  ""
                    |               ]
                    |            },
                    |            {
                    |               "access":1,
                    |               "description":"Link quality (signal strength)",
                    |               "name":"linkquality",
                    |               "property":"linkquality",
                    |               "type":"numeric",
                    |               "unit":"lqi",
                    |               "value_max":255,
                    |               "value_min":0
                    |            }
                    |         ],
                    |         "model":"JTYJ-GD-01LM/BW",
                    |         "vendor":"Xiaomi"
                    |      },
                    |      "endpoints":{
                    |         "1":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |                  "genBasic",
                    |                  "genIdentify",
                    |                  "genMultistateInput",
                    |                  "ssIasZone",
                    |                  "genAnalogInput",
                    |                  "genPowerCfg"
                    |               ],
                    |               "output":[
                    |                  "genOta"
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         }
                    |      },
                    |      "friendly_name":"0x10158d0003467624",
                    |      "ieee_address":"0x10158d0003467624",
                    |      "interview_completed":true,
                    |      "interviewing":false,
                    |      "model_id":"lumi.sensor_smoke",
                    |      "network_address":37965,
                    |      "power_source":"Battery",
                    |      "software_build_id":"3000-0001",
                    |      "supported":true,
                    |      "type":"EndDevice"
                    |   },
                    |   {
                    |      "definition":null,
                    |      "endpoints":{
                    |         "1":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         },
                    |         "10":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         },
                    |         "11":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |                  "ssIasAce"
                    |               ],
                    |               "output":[
                    |                  "ssIasZone",
                    |                  "ssIasWd"
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         },
                    |         "110":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         },
                    |         "12":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         },
                    |         "13":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |                  "genOta"
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         },
                    |         "2":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         },
                    |         "242":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         },
                    |         "3":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         },
                    |         "4":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         },
                    |         "47":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         },
                    |         "5":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         },
                    |         "6":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         },
                    |         "8":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         }
                    |      },
                    |      "friendly_name":"Coordinator",
                    |      "ieee_address":"0x00124b0018e2f6a6",
                    |      "interview_completed":true,
                    |      "interviewing":false,
                    |      "network_address":0,
                    |      "supported":false,
                    |      "type":"Coordinator"
                    |   },
                    |   {
                    |      "date_code":"20170314",
                    |      "definition":{
                    |         "description":"MiJia Honeywell smoke detector",
                    |         "exposes":[
                    |            {
                    |               "access":1,
                    |               "description":"Indicates whether the device detected smoke",
                    |               "name":"smoke",
                    |               "property":"smoke",
                    |               "type":"binary",
                    |               "value_off":false,
                    |               "value_on":true
                    |            },
                    |            {
                    |               "access":1,
                    |               "description":"Indicates if the battery of this device is almost empty",
                    |               "name":"battery_low",
                    |               "property":"battery_low",
                    |               "type":"binary",
                    |               "value_off":false,
                    |               "value_on":true
                    |            },
                    |            {
                    |               "access":1,
                    |               "description":"Indicates whether the device is tampered",
                    |               "name":"tamper",
                    |               "property":"tamper",
                    |               "type":"binary",
                    |               "value_off":false,
                    |               "value_on":true
                    |            },
                    |            {
                    |               "access":1,
                    |               "description":"Remaining battery in %",
                    |               "name":"battery",
                    |               "property":"battery",
                    |               "type":"numeric",
                    |               "unit":"%",
                    |               "value_max":100,
                    |               "value_min":0
                    |            },
                    |            {
                    |               "access":3,
                    |               "name":"sensitivity",
                    |               "property":"sensitivity",
                    |               "type":"enum",
                    |               "values":[
                    |                  "low",
                    |                  "medium",
                    |                  "high"
                    |               ]
                    |            },
                    |            {
                    |               "access":1,
                    |               "name":"smoke_density",
                    |               "property":"smoke_density",
                    |               "type":"numeric"
                    |            },
                    |            {
                    |               "access":2,
                    |               "name":"selftest",
                    |               "property":"selftest",
                    |               "type":"enum",
                    |               "values":[
                    |                  ""
                    |               ]
                    |            },
                    |            {
                    |               "access":1,
                    |               "description":"Link quality (signal strength)",
                    |               "name":"linkquality",
                    |               "property":"linkquality",
                    |               "type":"numeric",
                    |               "unit":"lqi",
                    |               "value_max":255,
                    |               "value_min":0
                    |            }
                    |         ],
                    |         "model":"JTYJ-GD-01LM/BW",
                    |         "vendor":"Xiaomi"
                    |      },
                    |      "endpoints":{
                    |         "1":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |                  "genBasic",
                    |                  "genIdentify",
                    |                  "genMultistateInput",
                    |                  "ssIasZone",
                    |                  "genAnalogInput",
                    |                  "genPowerCfg"
                    |               ],
                    |               "output":[
                    |                  "genOta"
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         }
                    |      },
                    |      "friendly_name":"0x00158d0003467624",
                    |      "ieee_address":"0x00158d0003467624",
                    |      "interview_completed":true,
                    |      "interviewing":false,
                    |      "model_id":"lumi.sensor_smoke",
                    |      "network_address":37965,
                    |      "power_source":"Battery",
                    |      "software_build_id":"3000-0001",
                    |      "supported":true,
                    |      "type":"EndDevice"
                    |   },
                    |   {
                    |      "definition":null,
                    |      "endpoints":{
                    |         "1":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         }
                    |      },
                    |      "friendly_name":"0x5c0272fffe404781",
                    |      "ieee_address":"0x5c0272fffe404781",
                    |      "interview_completed":false,
                    |      "interviewing":false,
                    |      "network_address":25624,
                    |      "supported":false,
                    |      "type":"Unknown"
                    |   },
                    |   {
                    |      "definition":null,
                    |      "endpoints":{
                    |         "1":{
                    |            "bindings":[
                    |
                    |            ],
                    |            "clusters":{
                    |               "input":[
                    |
                    |               ],
                    |               "output":[
                    |
                    |               ]
                    |            },
                    |            "configured_reportings":[
                    |
                    |            ]
                    |         }
                    |      },
                    |      "friendly_name":"0x588e81fffe25360a",
                    |      "ieee_address":"0x588e81fffe25360a",
                    |      "interview_completed":false,
                    |      "interviewing":false,
                    |      "network_address":44630,
                    |      "supported":false,
                    |      "type":"Unknown"
                    |   }
                    |]""".stripMargin
    val x = MqttMessageParser(payload).parseDevices
    x.values.foreach(println)
    val map: Map[String, Any] = Map(
      "building_id"-> buildingId,
      "thing_id" -> thingId,
//      "shadows" -> x.transform( (s,info) => info.)
    )
  }


  it should "o" in {
    val message = "[{\"definition\":null,\"endpoints\":{\"1\":{\"bindings\":[],\"clusters\":{\"input\":[],\"output\":[]},\"configured_reportings\":[]},\"10\":{\"bindings\":[],\"clusters\":{\"input\":[],\"output\":[]},\"configured_reportings\":[]},\"11\":{\"bindings\":[],\"clusters\":{\"input\":[\"ssIasAce\"],\"output\":[\"ssIasZone\",\"ssIasWd\"]},\"configured_reportings\":[]},\"110\":{\"bindings\":[],\"clusters\":{\"input\":[],\"output\":[]},\"configured_reportings\":[]},\"12\":{\"bindings\":[],\"clusters\":{\"input\":[],\"output\":[]},\"configured_reportings\":[]},\"13\":{\"bindings\":[],\"clusters\":{\"input\":[\"genOta\"],\"output\":[]},\"configured_reportings\":[]},\"2\":{\"bindings\":[],\"clusters\":{\"input\":[],\"output\":[]},\"configured_reportings\":[]},\"242\":{\"bindings\":[],\"clusters\":{\"input\":[],\"output\":[]},\"configured_reportings\":[]},\"3\":{\"bindings\":[],\"clusters\":{\"input\":[],\"output\":[]},\"configured_reportings\":[]},\"4\":{\"bindings\":[],\"clusters\":{\"input\":[],\"output\":[]},\"configured_reportings\":[]},\"47\":{\"bindings\":[],\"clusters\":{\"input\":[],\"output\":[]},\"configured_reportings\":[]},\"5\":{\"bindings\":[],\"clusters\":{\"input\":[],\"output\":[]},\"configured_reportings\":[]},\"6\":{\"bindings\":[],\"clusters\":{\"input\":[],\"output\":[]},\"configured_reportings\":[]},\"8\":{\"bindings\":[],\"clusters\":{\"input\":[],\"output\":[]},\"configured_reportings\":[]}},\"friendly_name\":\"Coordinator\",\"ieee_address\":\"0x00124b0018e2f6a6\",\"interview_completed\":true,\"interviewing\":false,\"network_address\":0,\"supported\":false,\"type\":\"Coordinator\"},{\"date_code\":\"20170314\",\"definition\":{\"description\":\"MiJia Honeywell smoke detector\",\"exposes\":[{\"access\":1,\"description\":\"Indicates whether the device detected smoke\",\"name\":\"smoke\",\"property\":\"smoke\",\"type\":\"binary\",\"value_off\":false,\"value_on\":true},{\"access\":1,\"description\":\"Indicates if the battery of this device is almost empty\",\"name\":\"battery_low\",\"property\":\"battery_low\",\"type\":\"binary\",\"value_off\":false,\"value_on\":true},{\"access\":1,\"description\":\"Indicates whether the device is tampered\",\"name\":\"tamper\",\"property\":\"tamper\",\"type\":\"binary\",\"value_off\":false,\"value_on\":true},{\"access\":1,\"description\":\"Remaining battery in %\",\"name\":\"battery\",\"property\":\"battery\",\"type\":\"numeric\",\"unit\":\"%\",\"value_max\":100,\"value_min\":0},{\"access\":3,\"name\":\"sensitivity\",\"property\":\"sensitivity\",\"type\":\"enum\",\"values\":[\"low\",\"medium\",\"high\"]},{\"access\":1,\"name\":\"smoke_density\",\"property\":\"smoke_density\",\"type\":\"numeric\"},{\"access\":2,\"name\":\"selftest\",\"property\":\"selftest\",\"type\":\"enum\",\"values\":[\"\"]},{\"access\":1,\"description\":\"Link quality (signal strength)\",\"name\":\"linkquality\",\"property\":\"linkquality\",\"type\":\"numeric\",\"unit\":\"lqi\",\"value_max\":255,\"value_min\":0}],\"model\":\"JTYJ-GD-01LM/BW\",\"vendor\":\"Xiaomi\"},\"endpoints\":{\"1\":{\"bindings\":[],\"clusters\":{\"input\":[\"genBasic\",\"genIdentify\",\"genMultistateInput\",\"ssIasZone\",\"genAnalogInput\",\"genPowerCfg\"],\"output\":[\"genOta\"]},\"configured_reportings\":[]}},\"friendly_name\":\"0x00158d0003467624\",\"ieee_address\":\"0x00158d0003467624\",\"interview_completed\":true,\"interviewing\":false,\"model_id\":\"lumi.sensor_smoke\",\"network_address\":37965,\"power_source\":\"Battery\",\"software_build_id\":\"3000-0001\",\"supported\":true,\"type\":\"EndDevice\"}]"
    val parser = MqttMessageParser(message)
    println(parser.parseDevices)
  }

  it should "parse a flat mqtt message correctly" in {
    val payload = "{\"data\":{\"definition\":{\"description\":\"MiJia Honeywell smoke detector\",\"exposes\":[{\"access\":1,\"description\":\"Indicates whether the device detected smoke\",\"name\":\"smoke\",\"property\":\"smoke\",\"type\":\"binary\",\"value_off\":false,\"value_on\":true},{\"access\":1,\"description\":\"Indicates if the battery of this device is almost empty\",\"name\":\"battery_low\",\"property\":\"battery_low\",\"type\":\"binary\",\"value_off\":false,\"value_on\":true},{\"access\":1,\"description\":\"Indicates whether the device is tampered\",\"name\":\"tamper\",\"property\":\"tamper\",\"type\":\"binary\",\"value_off\":false,\"value_on\":true},{\"access\":1,\"description\":\"Remaining battery in %\",\"name\":\"battery\",\"property\":\"battery\",\"type\":\"numeric\",\"unit\":\"%\",\"value_max\":100,\"value_min\":0},{\"access\":3,\"name\":\"sensitivity\",\"property\":\"sensitivity\",\"type\":\"enum\",\"values\":[\"low\",\"medium\",\"high\"]},{\"access\":1,\"name\":\"smoke_density\",\"property\":\"smoke_density\",\"type\":\"numeric\"},{\"access\":2,\"name\":\"selftest\",\"property\":\"selftest\",\"type\":\"enum\",\"values\":[\"\"]},{\"access\":1,\"description\":\"Link quality (signal strength)\",\"name\":\"linkquality\",\"property\":\"linkquality\",\"type\":\"numeric\",\"unit\":\"lqi\",\"value_max\":255,\"value_min\":0}],\"model\":\"JTYJ-GD-01LM/BW\",\"vendor\":\"Xiaomi\"},\"friendly_name\":\"0x00158d0003467624\",\"ieee_address\":\"0x00158d0003467624\",\"status\":\"successful\",\"supported\":true},\"type\":\"device_interview\"}\n"
    val parser = MqttMessageParser(payload)
    val JArray(exposes) = parser.parsed \ "data" \ "definition" \ "exposes"
    exposes.foreach(println)
    println
    val validExposedProperties = Set("description", "property", "type", "unit", "values", "value_off","value_on", "value_max", "value_min")
    val stringProperties = Set("description", "property", "type", "unit")
    val intProperties = Set("value_max", "value_min", "access")
    val boolProperties = Set("value_on", "value_off")


    //types: binary, numeric, enum
    //properties ReadOnly:
    // smoke(bool),
    // battery_low(bool),
    // tamper(bool),
    // battery(0-100), unit=%
    // smoke_density(numeric)
    // linkquality (numeric), unit=lqi
    //properties with Write
    // sensitivity(low, medium, high)
    // selftest


    //{"ac_status":false,
    // "enrolled":false,"
    // "restore_reports":false,

    // "battery":100,
    // "battery_low":false,
    // linkquality":139,
    // "smoke":false,
    // "smoke_density":0,
    // "supervision_reports":false,
    // "tamper":false,
    // "trouble":false,
    // "voltage":3055}


    val l = exposes.map { case JObject(list) =>
      val map = list.flatMap { case (name, value) =>
        value match {
          case JString(s) if stringProperties.contains(name) =>
            List((name,s))
          case JInt(i) if intProperties.contains(name) =>
            List((name,i))
          case JBool(b) if boolProperties.contains(name) =>
            List((name,b))
          case JArray(l) if name == "values" =>
            val fixedList = l.map { case(JString(s)) => s }
            List((name, fixedList))
          case _ => List()
        }
      }.toMap

      val exposes = (map.get("type"), map.get("name")) match {
        case (Some(t), Some(n)) =>
          val desc = map.getOrElse("description","").asInstanceOf[String]
          val name = n.asInstanceOf[String]
          val access = map.getOrElse("access",1).asInstanceOf[Int]
          val inUpdates = access >= 2
          val readable = access >= 2
          val writable = access >= 3

          t match {
            case "binary" =>
              List(Binary(
                name = name,
                description = desc,
                inUpdates = inUpdates,
                readable = readable,
                writable = writable,
                value_off = map.getOrElse("value_off","").asInstanceOf[String],
                value_on = map.getOrElse("value_on","").asInstanceOf[String],
              ))

            case "numeric" =>
              val unit = map.getOrElse("unit","").asInstanceOf[String]
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
                case _ => Numeric(
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
              List(Enum(
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
    }

    exposes.foreach(println)
  }
}
