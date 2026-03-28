import scala.io.StdIn.{readInt, readLine}
import scala.annotation.tailrec
import scala.collection.parallel.immutable.ParMap


case class Game(
                   board: Board,
                   currentPlayer: Stone, // ou as pedras pretas ou as brancas
                   message: String = ""

               )

object Game {
    def opponent(s: Stone): Stone = s match {
        case Stone.Black => Stone.White
        case Stone.White => Stone.Black
    }

    def render(board: Board): Unit = {
        println("\n   0  1  2  3  4  5  6  7")
        println("  -------------------------") //para indicar o número na parte de cima do tabuleiro
        draw(board, 0, 0)
    }

    @tailrec
     def draw(board: Board, r: Int, c: Int): Unit = {
        (r, c) match {
            case (8, 0) => //caso esteja no final do tabuleiro
                println("  -------------------------")
            case (r, 8) => // para todas as linhas com y=8 vai meter um | no final da linha
                println("|")
                draw(board, r + 1, 0) // desenha o board até aí e recomeça na linha asseguir recursivamente
            case (r, c) => //caso não se saiba nenhum dos valores
                if (c == 0) print(s"$r |") // se a coluna for a de índice 0 ou seja inicio da linha ele imprime o número da linha + a |
                board.get((r, c)) match {
                    case Some(Stone.Black) => print(" B ")
                    case Some(Stone.White) => print(" W ")
                    case None => print(" . ")
                }
                draw(board, r, c + 1)
        }
    }

    @tailrec
    def getOpenCoords(board: Board, r: Int, c: Int, acc: List[Coord2D]): List[Coord2D] = {
        (r, c) match {
            case (8, 0) => acc // Fim
            case (r, 8) => getOpenCoords(board, r + 1, 0, acc) // Salta linha
            case (r, c) =>
                val newAcc = board.get((r, c)) match {
                    case None => (r, c) :: acc // Se está vazio, adiciona
                    case _ => acc
                }
                getOpenCoords(board, r, c + 1, newAcc)
        }
    }

    def initBoard(): Game = {
        val tabuleiro = (0 until 8).toList.flatMap { r => //flatMap em vez de criar uma lista de listas cria um lista de 64 elementos (8por8)
            // Para cada linha, criamos 0 a 7 (colunas)
            (0 until 8).map { c => //aqui crio colunas dentro de cada linha, aqui decidi map pq queremos extamente um par coordenada-> pedra
                val stone = if ((r + c) % 2 == 0) Stone.White else Stone.Black
                (r, c) -> stone
            }
        }.to(scala.collection.parallel.immutable.ParMap)

        Game(tabuleiro, Stone.Black, "Jogo iniciado!")
    }
}

object KonaneGame extends App {

        @tailrec
        def gameLoop(state: Game, lstOpenCoords: List[Coord2D]): Unit = {
            Game.render(state.board)
            println(s"\nMensagem: ${state.message}")

            val pStr = if (state.currentPlayer == Stone.White) "Brancas" else "Pretas"
            println(s"Vez de: $pStr")

            print("Origem (linha) ou q p/ sair: ")

            val l1 = readInt()
            if (l1 == -1) return println("Fim de jogo!")

            print("Origem (coluna): ")
            val c1 = readInt()
            print("Destino (linha): ")
            val l2 = readInt()
            print("Destino (coluna): ")
            val c2 = readInt()

            val openCoords = Game.getOpenCoords(state.board, l1, c1, inicialEmpty)
            val starterStone = state.board.get((l1, c1))
            if (starterStone.contains(state.currentPlayer)) {
                val (result, newList) = Logic.play(state.board, state.currentPlayer, (l1, c1), (l2, c2), lstOpenCoords)

                result match {
                    case Some(newBoard) =>
                        print(s"Última jogada: ($l1,$c1) para ($l2,$c2)")
                        print(" Queres jogar novamente? s/n")
                        val res = readLine()
                        res match {
                            case "s" => gameLoop(
                                state.copy(board = newBoard, currentPlayer = state.currentPlayer, message = "jogada válida"),
                                newList
                            )
                            case "n" =>  gameLoop(
                                state.copy(board = newBoard, currentPlayer = Game.opponent(state.currentPlayer), message = "próximo jogador"),
                                newList
                            )
                        }
                    case None =>
                        gameLoop(state.copy(message = "SALTO INVÁLIDO! Tenta outra vez."), lstOpenCoords)
                }
            } else {
                gameLoop(state.copy(message = "Essa peça não é tua ou a casa está vazia!"), lstOpenCoords)
            }
        }
        //  casas do meio (3,3) e (3,4) já estão vazias.
        val inicialGame = Game.initBoard()
        val boardWithoutMiddle = inicialGame.board - (3, 3) - (3, 4)
        val inicialEmpty = List((3, 3), (3, 4))

        gameLoop(inicialGame.copy(board = boardWithoutMiddle), inicialEmpty)
}