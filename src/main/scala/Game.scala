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

   

    @tailrec //compilador otimiza isto para funcionar como um ciclo while: evita stack overflow
    def getOpenCoords(game: Game, r: Int, c: Int, acc: List[Coord2D]): List[Coord2D] = { //usamos acumulador que guarda os valores vistos ao longo da fc recursiva
        (r, c) match { //vamos procurar as linhas e colunas
            case (row, 0) if row == game.rows => acc // Fim das linhas
            case (row, col) if col == game.cols => getOpenCoords(game, row + 1, 0, acc) // Salta linha
            case (row, col) => //coords sem limites de tabuleiro
                val newAcc = game.board.get((row, col)) match { //vamos ao tabuleiro ver se tem peça
                    case None => (row, col) :: acc // Se está vazio, adiciona
                    case _ => acc //se tiver peça, mantem o que já tinha
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
        }.to(scala.collection.parallel.immutable.ParMap) //pk o enunciado pediu

        Game(tabuleiro,rows,cols, Stone.Black, "Jogo iniciado!")
    }
}

