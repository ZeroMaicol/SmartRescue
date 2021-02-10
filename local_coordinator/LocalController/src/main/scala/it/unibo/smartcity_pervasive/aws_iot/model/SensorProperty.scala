package it.unibo.smartcity_pervasive.aws_iot.model


sealed trait SensorProperty{
  val name: String
  val description: String
  val inUpdates: Boolean
  val writable: Boolean
  val readable: Boolean
}


case class Binary(name: String,
                  description: String = "",
                  inUpdates: Boolean,
                  readable: Boolean,
                  writable: Boolean, value_off:
                  String = "false",
                  value_on: String = "true") extends SensorProperty {
  override def toString: String = s"$name Binary"
}

case class Numeric(name: String,
                   description: String = "",
                   inUpdates: Boolean,
                   readable: Boolean,
                   writable: Boolean,
                   unit: String) extends SensorProperty {
  override def toString: String = s"$name: Numeric$unit"
}

case class NumericWithRange(name: String,
                            description: String = "",
                            inUpdates: Boolean,
                            readable: Boolean,
                            writable: Boolean,
                            unit: String = "",
                            value_min: Int,
                            value_max: Int) extends SensorProperty {
  override def toString: String = s"${super.toString} with Range [${value_min}-${value_max}]"

}

case class Enum(name: String,
                description: String = "",
                inUpdates: Boolean,
                readable: Boolean,
                writable: Boolean,
                values: List[String]) extends SensorProperty {
  override def toString: String = s"$name: Enum with Values: (${values.mkString(",")})"
}
