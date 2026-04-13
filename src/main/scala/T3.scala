import scala.annotation.tailrec

class T3 {

  def playRandomly(board: Board,
                   r: MyRandom, player: Stone, lstOpenCoords: List[Coord2D],
                   f: (List[Coord2D], MyRandom) => (Coord2D, MyRandom))
  :(Option[Board], MyRandom, List[Coord2D], Option[Coord2D]) = {

    // Criamos uma função auxiliar recursiva aqui dentro!
    @tailrec
    def tentarJogar(currentRand: MyRandom, buracosATestar: List[Coord2D]): (Option[Board], MyRandom, List[Coord2D], Option[Coord2D]) = {

      // Se já testámos todos e não deu nenhum, falha.
      if (buracosATestar.isEmpty) {
        return (None, currentRand, lstOpenCoords, None)
      }

      // 1. Obtemos a coordenada de destino à sorte a partir da lista TEMPORÁRIA
      val (coordTo, nextRand) = f(buracosATestar, currentRand)

      // 2. Tentamos encontrar uma peça que consiga saltar para lá
      val coordFromOpt = findValidCoordFrom(board, player, coordTo)

      coordFromOpt match {
        case Some(coordFrom) =>
          // SUCESSO! Passamos a lista ORIGINAL (lstOpenCoords) para o Logic.play
          val resultadoJogada = Logic.play(board, player, coordFrom, coordTo, lstOpenCoords)
          (resultadoJogada._1, nextRand, resultadoJogada._2, Some(coordTo))

        case None =>
          // FALHOU! Tiramos este buraco da lista temporária e tentamos de novo
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