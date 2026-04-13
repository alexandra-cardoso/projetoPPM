import scala.annotation.tailrec

object Logic {
    def play(board: Board, player: Stone, coordFrom: Coord2D, coordTo: Coord2D, lstOpenCoords: List[Coord2D]):(Option[Board], List[Coord2D])= {
        val dif: Coord2D = (coordTo._1 - coordFrom._1, coordTo._2 - coordFrom._2) //quanto vai andar
        // Nota: Some é o que usamos pra contrariar o None, quando damos um valor, usa se o Some.
        val coordMeio: Option[Coord2D] = dif match { //este match vai devolver a coordenada do meio se a coordenada final dessa jogada estiver livre e se a posição do meio tiver uma peça adversária
            case (2, 0) | (0, 2) | (-2, 0) | (0, -2) if lstOpenCoords.contains(coordTo) => //se for direção válida, e for andar 2 casas
                val coord = ((coordFrom._1 + coordTo._1) / 2, (coordFrom._2 + coordTo._2) / 2) //a pedra que vai ser comida
                board.get(coord) match { //vamos ao tabuleiro ver a coordenada e perceber o que lá está
                    case Some(s) if s != player =>
                        Some(coord) //se houver uma peça adversária, podemos guardar esta coordenada, porque é válida: para fazer a peça desaparecer do tabuleiro
                    case _ =>
                        None
                }
            case _ => None
        }
        coordMeio match {
            case Some(m) =>
                val novaLista = (coordFrom :: m :: lstOpenCoords).filterNot(_ == coordTo) //a nova lista das posições vazias vai ter a coord de onde a peça saiu, a do meio, que foi comida e NÃO VAI TER a posição para onde a peça ia (que antes estava nessa lista)
                val novoBoard = board - coordFrom - m + (coordTo->player) //o novo tabuleiro tem um novo par, com a coordenada para onde a peça foi e a cor, e sem a coord de onde saiu e a do meio
                (Some(novoBoard), novaLista) //retorna-se este par
            case _ => (None, lstOpenCoords) //se não, retorna este
        }

    }

    def playRandomly(board: Board,
                     r: MyRandom, player: Stone, lstOpenCoords: List[Coord2D],
                     f: (List[Coord2D], MyRandom) => (Coord2D, MyRandom))
    : (Option[Board], MyRandom, List[Coord2D], Option[Coord2D]) = {

        @tailrec
        def contarLinhas(r: Int): Int = {
            board.get((r, 0)) match {
                case Some(_) => contarLinhas(r + 1) // Tem peça, continua a descer
                case None =>
                    if (lstOpenCoords.contains((r, 0))) contarLinhas(r + 1) // É um buraco válido, continua a descer
                    else r
            }
        }

        @tailrec
        def contarColunas(c: Int): Int = {
            board.get((0, c)) match {
                case Some(_) => contarColunas(c + 1) // Tem peça, continua para a direita
                case None =>
                    if (lstOpenCoords.contains((0, c))) contarColunas(c + 1) // É um buraco válido, continua
                    else c
            }
        }

        val rows = contarLinhas(0)
        val cols = contarColunas(0)

        @tailrec
        def tentarJogar(currentRand: MyRandom, buracosATestar: List[Coord2D]): (Option[Board], MyRandom, List[Coord2D], Option[Coord2D]) = {

            // Se já testámos todos e não deu nenhum, falha.
            if (buracosATestar.isEmpty) {
                return (None, currentRand, lstOpenCoords, None)
            }
            val (coordTo, nextRand) = f(buracosATestar, currentRand)
            val coordFromOpt = findValidCoordFrom(board,rows,cols, player, coordTo)

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

    def findValidCoordFrom(board: Board,rows:Int, cols:Int, player: Stone, targetTo: Coord2D): Option[Coord2D] = {
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
                val valid = coord._1 >= 0 && coord._1 < rows && coord._2 >= 0 && coord._2 < cols
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

    def randomMove(lstOpenCoords: List[Coord2D], rand: MyRandom): (Coord2D, MyRandom) = {
        // Gerar um índice aleatório entre 0 e o tamanho da lista - 1
        val (index, nextRand) = rand.nextInt(lstOpenCoords.length)
        // para garantir que devolvemos um MyRandom
        val nextMyRandom = nextRand match {
            case mr: MyRandom => mr
            case _ => MyRandom(0L)
        }
        val chosenCoord = lstOpenCoords(index) // a coordenada escolhida vai ser a no index escolhido aleatóriamente
        (chosenCoord, nextMyRandom)
    }
}