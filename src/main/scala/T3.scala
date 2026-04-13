import scala.annotation.tailrec

class T3 {

  def playRandomly(board: Board,
                   r: MyRandom, player: Stone, lstOpenCoords: List[Coord2D],
                   f: (List[Coord2D], MyRandom) => (Coord2D, MyRandom))
  :(Option[Board], MyRandom, List[Coord2D], Option[Coord2D]) = {

    @tailrec
    def tentarJogar(currentRand: MyRandom, buracosATestar: List[Coord2D]): (Option[Board], MyRandom, List[Coord2D], Option[Coord2D]) = {

      // caso paragem
      if (buracosATestar.isEmpty) {
        return (None, currentRand, lstOpenCoords, None)
      }

      val (coordTo, nextRand) = f(buracosATestar, currentRand)

      val coordFromOpt = findValidCoordFrom(board, player, coordTo)

      // Só jogar quando encontra Some(coordFrom)
      coordFromOpt match {
        case Some(coordFrom) =>
          val resultadoJogada = Logic.play(board, player, coordFrom, coordTo, lstOpenCoords)
          (resultadoJogada._1, nextRand, resultadoJogada._2, Some(coordTo))

        case None =>
          val restantes = buracosATestar.filterNot(_ == coordTo)
          tentarJogar(nextRand, restantes)
      }
    }
    
    tentarJogar(r, lstOpenCoords)
  }
  
  def findValidCoordFrom(board: Board, player: Stone, targetTo: Coord2D): Option[Coord2D] = {
    val opponent = if (player == Stone.Black) Stone.White else Stone.Black

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
          val middle = ((coord._1 + targetTo._1) / 2, (coord._2 + targetTo._2) / 2)
          (board.get(coord), board.get(middle)) match {
            case (Some(s), Some(m)) if s == player && m == opponent => Some(coord)
            case _ => checkCoords(tail)
          }
        } else {
          checkCoords(tail)
        }
    }

    checkCoords(options)
  }
}
