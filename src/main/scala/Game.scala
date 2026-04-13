import scala.io.StdIn.{readInt, readLine}
import scala.annotation.tailrec
import scala.collection.parallel.immutable.ParMap

case class Game(
                   board: Board,
                   rows: Int,
                   cols : Int,
                   currentPlayer: Stone, // ou as pedras pretas ou as brancas
                   message: String = ""

               )

object Game {
    def opponent(s: Stone): Stone = s match { //define o oponente vedo quem é o jogador atual
        case Stone.Black => Stone.White
        case Stone.White => Stone.Black
    }

    @tailrec
        def makeHeader(current:Int, max:Int): Unit = { // faz a primeira linha do tabuleiro dinamicamente para acompanhar o tamanho definido pelo jogador
        (current < max) match {
            case true =>
                (current == 0 ) match {
                    case true => print("    0")
                    case false => print(f"$current%3d")
                }
                makeHeader(current + 1, max)
            case false =>
                println()
        }
    }

    def render(game: Game): Unit = { //desenha efetivamente o board na consola chamando o draw que define o desenho do tabuleiro
        makeHeader(0, game.cols)
        println("  " + "-" * (game.cols *3+1))
        draw(game, 0, 0)
    }

    @tailrec
     def draw(game: Game, r: Int, c: Int): Unit = {
        (r, c) match {
            case (row, 0) if row == game.rows =>//caso esteja no final do tabuleiro
                println("  " + "-" * (game.cols *3+1))

            case (row, col)  if col == game.cols=> // para todas as linhas com y=8 vai meter um | no final da linha
                println("|")
                draw(game, r + 1, 0) // desenha o board até aí e recomeça na linha asseguir recursivamente
            case (row, col) => //caso não se saiba nenhum dos valores
                if (c == 0) print(s"$row |") // se a coluna for a de índice 0 ou seja inicio da linha ele imprime o número da linha + a |
                game.board.get((row, col)) match {
                    case Some(Stone.Black) => print(" B ")
                    case Some(Stone.White) => print(" W ")
                    case None => print(" . ")
                }
                draw(game, row, col + 1)
        }
    }

    @tailrec
    def getOpenCoords(game: Game, r: Int, c: Int, acc: List[Coord2D]): List[Coord2D] = {
        (r, c) match {
            case (row, 0) if row == game.rows => acc // Fim
            case (row, col) if col == game.cols => getOpenCoords(game, row + 1, 0, acc) // Salta linha
            case (row, col) =>
                val newAcc = game.board.get((row, col)) match {
                    case None => (row, col) :: acc // Se está vazio, adiciona
                    case _ => acc
                }
                getOpenCoords(game, row, col + 1, newAcc)
        }
    }

    def initBoard(rows:Int,cols:Int): Game = {
        val tabuleiro = (0 until rows).toList.flatMap { r => //flatMap em vez de criar uma lista de listas cria um lista de 64 elementos (8por8)
            // Para cada linha, criamos 0 a x colunas
            (0 until cols).map { c => //aqui crio colunas dentro de cada linha, aqui decidi map pq queremos extamente um par coordenada-> pedra
                val stone = if ((r + c) % 2 == 0) Stone.White else Stone.Black
                (r, c) -> stone
            }
        }.to(scala.collection.parallel.immutable.ParMap)

        Game(tabuleiro,rows,cols, Stone.Black, "Jogo iniciado!")
    }
}

object KonaneGame extends App {

    @tailrec
    def gameLoop(state: Game, lstOpenCoords: List[Coord2D]): Unit = {
        Game.render(state)
        println(s"\nMensagem: ${state.message}")

        val pStr = if (state.currentPlayer == Stone.White) "Brancas" else "Pretas"
        println(s"Vez de: $pStr")

        print("Origem (linha) ou -1 p/ sair: ")
        val l1 = readInt()
        if (l1 == -1) return println("Fim de jogo!")

        print("Origem (coluna): ")
        val c1 = readInt()
        print("Destino (linha): ")
        val l2 = readInt()
        print("Destino (coluna): ")
        val c2 = readInt()

        val starterStone = state.board.get((l1, c1))

        if (starterStone.contains(state.currentPlayer)) {
            val (res, newList) = Logic.play(state.board, state.currentPlayer, (l1, c1), (l2, c2), lstOpenCoords)

            res match {
                case Some(newBoard) =>
                    println(s"\nÚltima jogada: ($l1,$c1) para ($l2,$c2)")
                    val estadoAtualizado = state.copy(board = newBoard)
                    Game.render(estadoAtualizado)
                    print("\nQueres capturar novamente? s/n: ")
                    val res = readLine()
                    res match {
                        case "s" => gameLoop(
                            state.copy(board = newBoard, currentPlayer = state.currentPlayer, message = "Continua a capturar!"),
                            newList
                        )
                        case "n" => gameLoop(
                            state.copy(board = newBoard, currentPlayer = Game.opponent(state.currentPlayer), message = "Próximo jogador"),
                            newList
                        )
                        case _ => gameLoop(
                            state.copy(board = newBoard, currentPlayer = Game.opponent(state.currentPlayer), message = "Input inválido. Próximo jogador!"),
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

    println("Configuração do Tabuleiro Kōnane ")
    print("Número de linhas/colunas (ex: 8): ")
    val rows = readInt()
    val cols = rows
    val inicialGame = Game.initBoard(rows, cols)

    val midR = rows / 2
    val midC = cols / 2

    val boardWithoutMiddle = inicialGame.board - (midR, midC) - (midR, midC - 1)
    val inicialEmpty = List((midR, midC), (midR, midC - 1))

    gameLoop(inicialGame.copy(board = boardWithoutMiddle), inicialEmpty)
}