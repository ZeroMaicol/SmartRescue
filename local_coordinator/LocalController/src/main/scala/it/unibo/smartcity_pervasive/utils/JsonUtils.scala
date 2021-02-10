package it.unibo.smartcity_pervasive.utils

object JsonUtils {
  def caseClassToMap(cc: AnyRef): Map[String, Any] =
    cc.getClass.getDeclaredFields.foldLeft(Map.empty[String, Any]) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc))
    }
}
