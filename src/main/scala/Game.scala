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
    private def draw(board: Board, r: Int, c: Int): Unit = {
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

    def initBoard(): Game = {
        val tabuleiro = (0 until 8).toList.flatMap { r => //flatMap em vez de criar uma lista de listas cria um lista de 64 elementos (8por8)
            // Para cada linha, criamos 0 a 7 (colunas)
            (0 until 8).map { c => //aqui crio colunas dentro de cada linha, aqui decidi map pq queremos extamente um par coordenada-> pedra
                val stone = if ((r + c) % 2 == 0) Stone.White else Stone.Black
                (r, c) -> stone
            }
        }.to(scala.collection.parallel.immutable.ParMap)

        Game(tabuleiro, Stone.Black, "Jogo iniciado com flatMap/map!")
    }
}

object KonaneGame extends App {

    @tailrec
    def gameLoop(estado: Game): Unit = {
        println("\n--- ESTADO DO TABULEIRO ---")
        Game.render(estado.board)

        println(s"Mensagem: ${estado.message}")
        val playerStr = if(estado.currentPlayer == Stone.White) "Brancas" else "Pretas"
        println(s"Vez do Jogador: $playerStr")

        print("\nEscolha a peça (linha) ou -1 para sair: ")
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

            // Lógica de Troca de Turno (Usando o Enum Stone)
            val proximoJogador = Game.opponent(estado.currentPlayer)

            val novoEstado = estado.copy(
                currentPlayer = proximoJogador,
                message = s"O jogador $playerStr tentou mover de ($l1, $c1) para ($l2, $c2)"
            )

            gameLoop(novoEstado)
        }
    }

    // Iniciar o loop com o objeto Game completo
    val estadoInicial = Game.initBoard()
    gameLoop(estadoInicial)
}