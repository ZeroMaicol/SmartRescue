package it.unibo.smartcity_pervasive.utils

import it.unibo.smartcity_pervasive.Controller.args

import scala.sys.exit

object ArgsUtils {
  val usage =
    """
    Usage:
    --help        This message\n"+
    --thingName   The name of the IoT thing\n"+
    --clientId    The Client ID to use when connecting
    -e|--endpoint AWS IoT service endpoint hostname\n"
    -r|--rootca   Path to the root certificate\n"+
    -c|--cert     Path to the IoT thing certificate\n"
    -k|--key      Path to the IoT thing private key\n"
    -p|--port     Port to use (optional)"
  """

  def printUsage(): Unit = println(usage)

  //  val settables = Set("help", "thingName", "clientId", "endpoint", "rootca", "cert", "key", "port", "localMqttAddress")
  val settables: Set[String] = Set()

  def validateArgs(args: Array[String]) =  if (args.length == 0) println(usage)


  type ArgMap = Map[String, String]
  def argMap(map: ArgMap, list: List[String]): ArgMap = {
    list match {
      case Nil => map
      case option :: value :: tail if option.substring(0, 2) == "--" && settables.contains(option.substring(2)) =>
        argMap(map + (option.substring(2) -> value), tail)
      case "--help" :: _ =>
        println(usage)
        exit
      //      case string :: Nil =>  optionMap(map + ("infile" -> string), list.tail)
      case arg :: _ => println("Unknown arg " + arg)
        exit(1)
    }
  }
}
