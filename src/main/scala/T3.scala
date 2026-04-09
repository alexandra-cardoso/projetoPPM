import scala.annotation.tailrec
class T3 {

  def playRandomly(board: Board,
                   r: MyRandom, player: Stone, lstOpenCoords: List[Coord2D],
                   f: (List[Coord2D], MyRandom) => (Coord2D, MyRandom))
                    :(Option[Board], MyRandom, List[Coord2D], Option[Coord2D]) = {
    val (coordTo, nextRand) = f(lstOpenCoords, r)
    val coordFrom = findValidCoordFrom(board, player, coordTo)
    val play:(Option[Board],List[Coord2D]) = play(board, player,coordFrom, coordTo,lstOpenCoords)
    (play._1, nextRand, play._2, Some(coordTo))
  }

  def findValidCoordFrom(board: Board, player: Stone, targetTo: Coord2D): Option[Coord2D] = {
    val opponent = if (player == Stone.Black) Stone.White else Stone.Black

    // 4 posiçoes onde a peça que captura pode estar
    val options = List(
      (targetTo._1 - 2, targetTo._2),
      (targetTo._1 + 2, targetTo._2),
      (targetTo._1, targetTo._2 - 2),
      (targetTo._1, targetTo._2 + 2)
    )
  
    @tailrec
    def checkCoords(coords: List[Coord2D]): Option[Coord2D] = coords match {
      case Nil => None
      case coord :: tail =>
        val valid = coord._1 >= 0 && coord._1 < 8 && coord._2 >= 0 && coord._2 < 8
        
        if (valid) {
          // Peça a ser capturada
          val middle = ((coord._1 + targetTo._1) / 2, (coord._2 + targetTo._2) / 2)
          // Ver se captura é possivel (peças de cores diferentes)
          (board.get(coord), board.get(middle)) match {
            case (Some(s), Some(m)) if s == player && m == opponent => Some(coord)
            case _ => checkCoords(tail) // continua procura para outra direçao
          }
        } else {
          checkCoords(tail)  // continua procura para outra direçao
        }
    }

  checkCoords(options) // Inicio do LOOP
}

}
