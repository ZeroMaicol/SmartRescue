package it.unibo.smartcity_pervasive.utils

import it.unibo.smartcity_pervasive.Controller.args

import scala.annotation.tailrec
import scala.sys.exit

object ArgsUtils {
  val usage =
    """
    Usage:
    --help        This message\n"
    --thingName   The name of the IoT thing\n"
    --buildingId   The identifier of the building\n"
    --localMqttAddress The URL of the local mosquitto instance\n
    --clientId    The Client ID to use when connecting\n
    -e|--endpoint AWS IoT service endpoint hostname\n"
    -r|--rootca   Path to the root certificate\n"
    -c|--cert     Path to the IoT thing certificate\n"
    -k|--key      Path to the IoT thing private key\n"
    -p|--port     Port to use (optional)"
  """

  def printUsage(): Unit = println(usage)

    val mustSet: Set[String] = Set("thingName", "endpoint", "rootca", "cert", "key", "localMqttAddress", "buildingId")
    val options: Set[String] = mustSet ++ Set("help")
//  val settables: Set[String] = Set()

  def validateArgs(args: Array[String]): Unit =  if (args.length == 0) println(usage)


  type ArgMap = Map[String, String]
  @tailrec
  def argMap(map: ArgMap, list: List[String]): ArgMap = {
    list match {
      case Nil => map
      case option :: value :: tail if option.substring(0, 2) == "--" && options.contains(option.substring(2)) =>
        argMap(map + (option.substring(2) -> value), tail)
      case "--help" :: _ =>
        println(usage)
        exit
      //      case string :: Nil =>  optionMap(map + ("infile" -> string), list.tail)
      case arg :: _ => println("Unknown arg " + arg)
        exit
    }
  }
}
