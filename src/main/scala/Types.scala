import scala.collection.parallel.immutable.ParMap

type Coord2D = (Int, Int) //(row, column)
type Board = ParMap[Coord2D, Stone] //um par map é tipo um map normal mas mais eficiente para grandes quantidades de dados. Ex: tens um baralho e cartas e tens três pessoas, divides o baralho pelas três e elas vão ser mais rápidas pois estão três pessoas diferentes a fazer ao mesmo tempo. 

enum Stone:
  case Black, White

