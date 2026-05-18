import scala.annotation.tailrec
import scala.io.StdIn.readInt
// Notas de primeira entrega:
// Criar teste na interface para o random move
// Mais comentários
// FindCoordFrom devia retornar a lista para poder escolher a jogada de forma random ou com alguma verificação
// por exemplo escolher a peçá que podia capturar mais do que uma vez.


object Logic {
    def play(board: Board, player: Stone, coordFrom: Coord2D, coordTo: Coord2D, lstOpenCoords: List[Coord2D]): (Option[Board], List[Coord2D]) = {
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
                val novoBoard = board - coordFrom - m + (coordTo -> player) //o novo tabuleiro tem um novo par, com a coordenada para onde a peça foi e a cor, e sem a coord de onde saiu e a do meio
                (Some(novoBoard), novaLista) //retorna-se este par
            case _ => (None, lstOpenCoords) //se não, retorna este
        }

    }

    def playRandomly(board: Board, r: MyRandom, player: Stone, lstOpenCoords: List[Coord2D], f: (List[Coord2D], MyRandom) => (Coord2D, MyRandom)): (Option[Board], MyRandom, List[Coord2D], Option[Coord2D]) = {

        //tabuleiro dinâmico- vamos buscar as linhas e colunas
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
            val (coordTo, nextRand) = f(buracosATestar, currentRand) //usamos a função RandomMove basicamente, para descobrir, nesta lista de buracos, um aleatório
            val coordFromOpt = findValidCoordFrom(board, rows, cols, player, coordTo) //usamos uma função auxiliar para ver se o buraco dado é jogável

            coordFromOpt match {
                case Some(coordFrom) => //temos sucesso (retorno da fç auxiliar é um Option ent se houver Some, temos casa pra onde ir)
                    val resultadoJogada = Logic.play(board, player, coordFrom, coordTo, lstOpenCoords) //fazemos a jogada
                    (resultadoJogada._1, nextRand, resultadoJogada._2, Some(coordTo)) //retornamos o novo tabuleiro, a lista de buracos atualizada e para onde jogámos (e o myRand mas isso é pra irmos guardando os estados deste objeto)

                case None => //não dava pra jogar pra este buraco aleatório
                    val restantes = buracosATestar.filterNot(_ == coordTo) //tiramos da lista de buracos o buraco q testou
                    tentarJogar(nextRand, restantes) //vamos tentar outra vez
            }
        }

        tentarJogar(r, lstOpenCoords)
    }

    def findValidCoordFrom(board: Board, rows: Int, cols: Int, player: Stone, targetTo: Coord2D): Option[Coord2D] = {
        val opponent = if (player == Stone.Black) Stone.White else Stone.Black

        val options = List(
            (targetTo._1 - 2, targetTo._2),
            (targetTo._1 + 2, targetTo._2),
            (targetTo._1, targetTo._2 - 2),
            (targetTo._1, targetTo._2 + 2)
        )

        @tailrec
        def checkCoords(coords: List[Coord2D]): Option[Coord2D] = coords match {
            case Nil => None //ficou sem coordenadas para testar a jogada
            case coord :: tail => //encontrou uma coordenada na head da lista
                val valid = coord._1 >= 0 && coord._1 < rows && coord._2 >= 0 && coord._2 < cols //verifica se ela sequer existe no tabuleiro
                if (valid) { //se existir
                    val middle = ((coord._1 + targetTo._1) / 2, (coord._2 + targetTo._2) / 2) //calcula a posição onde deve estar uma peça adversária
                    (board.get(coord), board.get(middle)) match {
                        case (Some(s), Some(m)) if s == player && m == opponent => Some(coord) //vê se é de uma peça adversária
                        case _ => checkCoords(tail)
                    }
                } else {
                    checkCoords(tail) //volta a fazer para outra coordenada
                }
        }

        checkCoords(options) //verifica se alguma dá
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

    def isCenterOrCorner(c: Coord2D, rows: Int, cols: Int): Boolean = {
        val centers = List((rows / 2 - 1, cols / 2 - 1), (rows / 2 - 1, cols / 2), (rows / 2, cols / 2 - 1), (rows / 2, cols / 2))
        val corners = List((0, 0), (0, cols - 1), (rows - 1, 0), (rows - 1, cols - 1))
        centers.contains(c) || corners.contains(c)
    }

    def isAdjacent(c1: Coord2D, c2: Coord2D): Boolean = { //metodo se vê se a posição é adjacente a outra que recebe, tambêm para a verificação de remoção incial
        Math.abs(c1._1 - c2._1) + Math.abs(c1._2 - c2._2) == 1
    }

    def podeSaltarMais(board: Board, pos: Coord2D, p: Stone, rows: Int, cols: Int): Boolean = { //metodo que verifica a captura multipla
        val directions = List((2, 0), (-2, 0), (0, 2), (0, -2))

        // exists verifica se pelo menos uma direção permite o salto
        directions.exists {
            case (dr, dc) =>
                val target = (pos._1 + dr, pos._2 + dc)
                val mid = (pos._1 + dr / 2, pos._2 + dc / 2)

                val dentro = target._1 >= 0 && target._1 < rows && target._2 >= 0 && target._2 < cols

                if (dentro) {
                    //Verificamos se o destino está contido na lista de buracos (currentOpenCoords)
                    // Se estiver no Board, não está vazio.
                    val destinoVazio = !board.contains(target)
                    val pecaNoMeio = board.get(mid)

                    // O inimigo tem de existir (Some) e ser de cor diferente do jogador atual (p)
                    val temInimigoNoMeio = pecaNoMeio match {
                        case Some(s) => s != p
                        case None => false
                    }

                    if (destinoVazio && temInimigoNoMeio) {
                        println(s"Salto extra disponível para $p de $pos para $target")
                        true
                    } else false
                } else false
        }
    }


    // T5: Verifica se o jogador atual não tem movimentos possíveis, devolve vencedor.
    def verificarVencedor(board: Board, rows: Int, cols: Int, currentPlayer: Stone, open: List[Coord2D]): Option[Stone] = {
        val directions = List((2, 0), (-2, 0), (0, 2), (0, -2))
        val opponent = Game.opponent(currentPlayer)

        // Verifica se alguma peça do currentPlayer tem pelo menos um salto válido
        val temMovimento = board.exists { case (pos, stone) =>
            stone == currentPlayer && directions.exists { case (dr, dc) =>
                val target = (pos._1 + dr, pos._2 + dc)
                val mid = (pos._1 + dr / 2, pos._2 + dc / 2)
                val dentroLimites = target._1 >= 0 && target._1 < rows && target._2 >= 0 && target._2 < cols
                dentroLimites && open.contains(target) && board.get(mid).contains(opponent)
            }
        }

        if (temMovimento) None else Some(opponent) // oponente ganha quando currentPlayer está bloqueado
    }

    // T6: Undo — retira o último estado do histórico (par estado+openCoords).
    def undo(history: List[(Game, List[Coord2D])]): Option[((Game, List[Coord2D]), List[(Game, List[Coord2D])])] =
        history match {
            case Nil => None
            case head :: tail => Some((head, tail))

        }

    def obterDestinosValidos(pos: Coord2D, p: Stone, board: Board, rows: Int, cols: Int): List[Coord2D] = { //metodo que verifica os destinos validos para poder assinala-los a vermelho lá em cima
        val directions = List((2, 0), (-2, 0), (0, 2), (0, -2)) //crio uma lista de direções com as 4 direções que me posso mover
        directions.flatMap {
            case (dr, dc) =>
                val target = (pos._1 + dr, pos._2 + dc) // crio a target como a coordenada onde tamos mais a direção selecionada
                val mid = (pos._1 + dr / 2, pos._2 + dc / 2) // e a do meio vai ser a atual masi metade da direção selecionada

                val dentro = target._1 >= 0 && target._1 < rows && target._2 >= 0 && target._2 < cols // se estiver dentro do tabuleiro vai sempre estar tanto as linhas como as colunas entre 0 e 6
                (dentro, board.contains(target), board.get(mid)) match { // se estou dentro, o board tem a posição target e a posição do meio é uma peça oponente
                    case (true, false, Some(mid_stone)) if mid_stone != p => Some(target) //então devolvo a posição de destino selecionada como válida
                    case _ => None //se n respeitar a condição n devolvo nenhuma coordenada pq n existe nenhuma válida
                }
        }
    }    

}