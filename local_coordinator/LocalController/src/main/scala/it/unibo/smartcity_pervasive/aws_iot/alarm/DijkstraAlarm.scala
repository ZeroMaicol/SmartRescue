package it.unibo.smartcity_pervasive.aws_iot.alarm

object DijkstraAlarm {
  private var alarm: Boolean = false

  def startAlarm(): Unit = {
    alarm = true

    // do stuff with timers/threads
  }

  def stopAlarm(): Unit = {
    alarm = false

    //stop timers / threads
  }
}
