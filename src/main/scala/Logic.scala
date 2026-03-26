object Logic {
    def play(board: Board, player: Stone, coordFrom: Coord2D, coordTo: Coord2D, lstOpenCoords: List[Coord2D]):(Option[Board], List[Coord2D])= {
        val dif: Coord2D = (coordTo._1 - coordFrom._1, coordTo._2 - coordFrom._2) //quanto vai andar

// Nota: Some é o que usamos pra contrariar o None, quando damos um valor, usa se o Some.
        val coordMeio: Option[Coord2D] = dif match { //este match vai devolver a coordenada do meio se a coordenada final dessa jogada estiver livre e se a posição do meio tiver uma peça adversária
            case (2, 0) | (0, 2) | (-2, 0) | (0, -2) if lstOpenCoords.contains(coordTo) => //se for direção válida, e for andar 2 casas
                val coord = (coordFrom._1 - coordTo._1 / 2, coordFrom._1 - coordTo._2) //a pedra que vai ser comida
                board.get(coord) match { //vamos ao tabuleiro ver a coordenada e perceber o que lá está
                    case Some(s) if s != player => Some(coord) //se houver uma peça adversária, podemos guardar esta coordenada, porque é válida: para fazer a peça desaparecer do tabuleiro
                    case _ => None
                }
            case _ => None
        }

    }
}