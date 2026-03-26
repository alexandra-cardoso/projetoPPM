import scala.io.StdIn.{readInt, readLine}
import scala.annotation.tailrec

case class Game(
                   board: List[List[Int]],    
                   currentPlayer: Int,
                   message: String = ""
               )

object Game {
    val empty = 0
    val white = 1
    val black = 2

    def printRocks(value: Int): String = value match {
        case Game.empty => ". "
        case Game.white => " W "
        case Game.black => " B "
    }

    def printRow(row: List[Int]): Unit = row match {
        case Nil =>
        case head :: tail =>
            print(printRocks(head))
            printRow(tail)
    }

    def renderBoard(board: List[List[Int]], rowNum: Int = 0): Unit = {
        if (rowNum == 0) println("   0  1  2  3  4  5  6  7\n  -------------------------")

        board match {
            case Nil => println("  -------------------------")
            case head :: tail =>
                print(s"$rowNum |")
                printRow(head)
                println("|")
                renderBoard(tail, rowNum + 1)
        }
    }

    def initBoard(): Game = {
        val initialBoard = (0 until 8).toList.map { l =>
            (0 until 8).toList.map { c =>
                if ((l + c) % 2 == 0) Game.white else Game.black
            }
        }
        Game(initialBoard, Game.black, "O jogo começou! Pretas jogam primeiro.")
    }
}

object KonaneGame extends App {

    @tailrec
    def gameLoop(estado: Game): Unit = {
        println("\n--- ESTADO DO TABULEIRO ---")
        Game.renderBoard(estado.board)

        println(s"Mensagem: ${estado.message}")
        val playerStr = if(estado.currentPlayer == Game.white) "Brancas" else "Pretas"
        println(s"Vez do Jogador: $playerStr")

        print("\nEscolha a peça (linha): ")
        val l1 = readInt()
        if (l1 == -1) {
            println("Fim de jogo!")
        } else {
            print("Escolha a peça (coluna): ")
            val c1 = readInt()
            print("Destino (linha): ")
            val l2 = readInt()
            print("Destino (coluna): ")
            val c2 = readInt()

            // AQUI: Lógica de processamento da jogada
            // Por agora, apenas trocamos o turno
            val proximoJogador = if (estado.currentPlayer == Game.white) Game.black else Game.white

            val novoEstado = estado.copy(
                currentPlayer = proximoJogador,
                message = s"Jogador moveu de ($l1, $c1) para ($l2, $c2)"
            )

            gameLoop(novoEstado)
        }
    }

    val estadoInicial = Game.initBoard()
    gameLoop(estadoInicial)
}