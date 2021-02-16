import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Environment {

  final case class Position(long: Double, lat: Double)
  final case class Person(name: String, position: Position)
  final case class Rectangle(topLeft: Position, bottomRight: Position) {
    /**
     * Find if a point is inside the rectangle
     * @param point the point to be inside
     * @return true if inside, false otherwise
     */
    def isPointInside(point: Position): Boolean =
      if (topLeft.long <= point.long && point.long <= bottomRight.long && topLeft.lat <= point.lat && point.lat <= bottomRight.lat) true else false
  }
  final case class Path(rectangle: Rectangle, sensors: Map[String, NamedShadow], actuators: Map[String, NamedShadow], persons: List[Person])

  final case class BuildingZone(id: String, path: Path) {
    /**
     * Check if a zone of the building is safe
     * @return true if safe, false otherwise
     */
    def isSafe(): Boolean = {
      //Check quello che devi checkare
      if () true else false
    }
  }

  final case class Building(id: String, zones: List[BuildingZone])



  //Idea:
  //Environment conterrà una rappresentazione di quello che c'è nell'edificio

  //Inizializzazione:
  //- definisci la lista di path sotto forma di rettangoli di coordinate topLeft bottomRight.
  //- prendi tutti i device dell'edificio, per ognuno controlli in quale rettangolo appartiene e ce lo metti, bisogna mettere posizione anche in shadow.


  main {
    var sensors: Map[String, NamedShadow] = Map()
    var actuators: Map[String, NamedShadow] = Map()
    var persons: ListBuffer[Person] = mutable.ListBuffer[Person]()
    var zones: List[BuildingZone]
  }
}