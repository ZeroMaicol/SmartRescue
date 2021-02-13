package it.unibo.smartcity_pervasive.aws_iot.alarm

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object DijkstraAlarm {

  final case class Path(vertex: String, from: String, weight: Int, label: Boolean)

  //Add to current weight the weight of the previous nodes
  def getWeight(s: mutable.Set[Path], sourceVertex: Path, vertex: Path): Int = {
    var currentVertex = s.filter(_.vertex.eq(vertex.from)).head
    var weight = 0
    while (currentVertex != sourceVertex) {
      weight += currentVertex.weight
      currentVertex = s.filter(_.vertex.eq(currentVertex.from)).head
    }
    weight + vertex.weight
  }

  def min(p1: Path, p2: Path): Path = if (p1.weight > p2.weight) p2 else p1

  /**
   * Dijkstra Algorithm to get the min path from source to destination.
   * @param from the source
   * @param to the destination
   * @param nodeMap the graph
   * @return a List of node if there is a path, an empty list otherwise
   */
  def runDijkstra(from: String, to: String, nodeMap: ListBuffer[Path]): Option[ListBuffer[String]] = {
    //Choose the source vertex
    val sourceVertex = nodeMap.filter(_.vertex.eq(from)).filter(_.from.eq(from)).head
    println("sourceVertex: "+sourceVertex)

    //Define a set of vertices and initialize it to empty
    val s = mutable.ListBuffer.empty[Path]
    var current = mutable.ListBuffer.empty[Path]
    //Label the source vertex with 0 and insert it into S
    s += sourceVertex
    println("S with source: "+s)

    //Consider each vertex not in S connected by an edge from the newly inserted Vertex
    current = nodeMap.filter(_.from.eq(s.last.vertex)).filter(!_.vertex.eq(s.last.vertex)).map(x => Path(x.vertex,x.from,0, x.label))
    println("current: "+current)

    //Label the vertex not in S with the label of the newly inserted vertex + the length of the edge
    //If the vertex not in S was already labeled, its new label will be minimal
    current = current.map(x => {
      if (!x.label) {
        Path(
          x.vertex,
          x.from,
          nodeMap.filter(_.vertex.eq(x.vertex)).filter(_.from.eq(x.from)).head.weight + s.last.weight,
          label = true
        )
      } else {
        val oldValue = current.filter(_.vertex.eq(x.vertex)).filter(_.from.eq(x.from)).head
        val newValue = x.weight + s.last.weight
        Path(
          x.vertex ,
          if (oldValue.weight > newValue) x.from else oldValue.from,
          if (oldValue.weight > newValue) newValue else oldValue.weight,
          label = true
        )
      }
    })
    println("current: "+current)
    //Pick a vertex not in S with the smallest label and add it to S.
    val minVertex = current.reduceLeft(min)
    s += minVertex
    current.remove(current.indexOf(minVertex))
    println("s: " + s)
    println("current: " + current)
    println("-------------")


    //Repeat from step 4, until the destination vertex is in S or there are no labeled vertices not in S
    nodeMap.filter(_.from.eq(s.last.vertex)).filter(!_.vertex.eq(s.last.vertex)).map(x => Path(x.vertex,x.from,0, x.label)).foreach(current+=_)
    println("current: " + current)
    while (current.nonEmpty && s.count(_.vertex.eq(to)) == 0) {
      current = dijkstraCycle(nodeMap, current, s)
    }

    //If the end node is not present in the list, then there is no path
    if (s.count(_.vertex.eq(to)) == 0)
      return Option.empty
    //otherwise
    var value = s.filter(_.vertex.eq(to)).head
    var list = ListBuffer[String]()
    //get the path from end to start
    while (value.vertex != from){
      list += value.vertex
      value = s.filter(_.vertex.eq(value.from)).head
    }
    list += from
    Option(list)
  }

  def dijkstraCycle(nodeMap: ListBuffer[Path], current: ListBuffer[Path], s: ListBuffer[Path]): ListBuffer[Path] = {
    //Label the vertex not in S with the label of the newly inserted vertex + the length of the edge
    //If the vertex not in S was already labeled, its new label will be minimal
    val newCurrent = current.map(x => {
      if (!x.label) {
        Path(
          x.vertex,
          x.from,
          nodeMap.filter(_.vertex.eq(x.vertex)).filter(_.from.eq(x.from)).head.weight + s.last.weight,
          label = true
        )
      } else {
        val oldValue = current.filter(_.vertex.eq(x.vertex)).filter(_.from.eq(x.from)).head
        val newValue = x.weight + s.head.weight
        Path(
          x.vertex ,
          if (oldValue.weight > newValue) x.from else oldValue.from,
          if (oldValue.weight > newValue) newValue else oldValue.weight,
          label = true
        )
      }
    })
    println("current: "+newCurrent)
    //Pick a vertex not in S with the smallest label and add it to S.
    val minVertex = newCurrent.reduceLeft(min)
    System.out.println("min: "+minVertex)
    if (s.count(_.vertex.eq(minVertex.vertex)) == 0) {
      s += minVertex
    } else {
      if (s.filter(_.vertex.eq(minVertex.vertex)).head.weight > minVertex.weight) {
        s.remove(s.indexOf(s.filter(_.vertex.eq(minVertex.vertex)).head))
        s += minVertex
      }
    }
    newCurrent.remove(newCurrent.indexOf(minVertex))
    println("s: " + s)
    println("current: " + newCurrent)
    println("-------------")

    //Repeat from step 4, until the destination vertex is in S or there are no labeled vertices not in S
    nodeMap.filter(_.from.eq(s.last.vertex)).filter(!_.vertex.eq(s.last.vertex)).map(x => Path(x.vertex,x.from,0, x.label)).foreach(newCurrent+=_)
    println("newCurrent: " + newCurrent)

    newCurrent
  }

  def main(args: Array[String]): Unit = {

    val nodeMap: ListBuffer[Path] = mutable.ListBuffer[Path](
      Path("start","start",0, label = true),
      Path("a","start",2,label = false),
      Path("b","start",3, label = false),
      Path("c","start",2, label = false),
      Path("b","a",4, label = false),
      Path("b","c",3, label = false),
      Path("d","b",2, label = false),
      Path("e","d",3, label = false),
      Path("f","e",3, label = false),
      Path("g","e",4, label = false)
    )

    /*val nodeMap: ListBuffer[Path] = mutable.ListBuffer[Path](
      Path("Start", "Start", 0, label = true),
      Path("C", "Start", 2, label = false),
      Path("B", "Start", 3, label = false),
      Path("A", "B", 4, label = false),
      Path("Exit","A", 5, label = false),
      //Path("Exit", "C", 3, label = false)
    )*/

    //val result = runDijkstra("Start","Exit", nodeMap)
    val result = runDijkstra("start","f", nodeMap)
    println("result: "+result)
  }

}
