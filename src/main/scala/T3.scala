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
    val possibleFrom = List(
      (targetTo._1 - 2, targetTo._2),
      (targetTo._1 + 2, targetTo._2),
      (targetTo._1, targetTo._2 - 2),
      (targetTo._1, targetTo._2 + 2)
    )
    // Find encontra a primeira coordeanda que satisfaz lógica
    val valid = possibleFrom.find(coord => {
        val isInside = coord._1 >= 0 && coord._1 < 8 && coord._2 >= 0 && coord._2 < 8

        if (isInside) {
          val middle = ((coord._1 + targetTo._1) / 2, (coord._2 + targetTo._2) / 2)

          (board.get(coord), board.get(middle)) match {
            case (Some(s), Some(m)) if s == player && m == opponent => true
            case _ => false
          }
        } else {
          false
        }
      })
      valid
  }

}
