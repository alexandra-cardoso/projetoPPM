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
    val opponent = if (player == Black) White else Black
    
    
  }
  
}
