import scala.collection.parallel.imutable.ParMap

type Coord2D = (Int, Int) //(row, column)
type Board = ParMap[Coord2D, Stone]

enum Stone:
  case Black, White

