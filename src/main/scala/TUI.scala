import Stone.Black

import scala.io.StdIn.*
import scala.annotation.tailrec

object TUI {
    case class GameConfig(rows: Int, cols: Int, timerSeconds: Long, difficulty: Int)

    def start(): Unit = {
        println("=== BEM-VINDO AO KÖNANE (TUI) ===")
        mainMenu(GameConfig(8,8,30,1)) //valores aleatórios- padrão
    }

    @tailrec
    def mainMenu(config: GameConfig): Unit = {
        println(s"\n[MENU] Tabuleiro: ${config.rows}x${config.cols} | Tempo: ${config.timerSeconds}s | Dificuldade: ${config.difficulty}")
        println("1. Jogo local")
        println("2. Jogo contra Computador")
        println("3. Alterar Dimensões")
        println("4. Definir Tempo Limite")
        println("5. Definir Dificuldade")
        println("0. Sair")
        print("Opção: ")

        readLine() match {
            case "1" =>
                val game = Game.initBoard(config.rows, config.cols)
                gameLoop(game, List(), List(), config, contraPC = false)
                mainMenu(config)
            case "2" =>
                val game = Game.initBoard(config.rows, config.cols)
                gameLoop(game, List(), List(), config, contraPC = true)
                mainMenu(config)
            case "3" =>
                print("Linhas: ")
                val li = readInt()
                print("Colunas: ")
                val col = readInt()
                mainMenu(config.copy(rows = li, cols = col))
            case "4" =>
                print("Tempo máx de jogada: ")
                mainMenu(config.copy(timerSeconds = readLong()))
            case "5" =>
                println("1. Fácil | 2. Médio | 3.Difícil")
                mainMenu(config.copy(difficulty = readInt()))
            case "0" => println("A fechar...")
            case _ =>
                println("Opção Inválida!")
                mainMenu(config)
        }
    }

    @tailrec
    def gameLoop(state: Game, open: List[Coord2D], history: List[Game], config: GameConfig, contraPC: Boolean): Unit = {
        Game.render(state)
        val pStr = state.currentPlayer match {
            case Stone.Black => "Pretas"
            case Stone.White => "Brancas"
        }
        println(s"\nMensagem: ${state.message}")
        println(s"Vez das: $pStr")

        open.length match {
            case 0 =>
                println("Pretas: Escolha uma peça para remover (Centro ou canto)")
                val coord = (readInt(), readInt())
                (Logic.isCenterOrCorner(coord, config.rows, config.cols), state.board.get(coord)) match {
                    case (true, Some(Stone.Black)) =>
                        val nState = state.copy(board = state.board - coord, currentPlayer = Stone.White, message = "Peça removida")
                        gameLoop(nState, List(coord), state :: history, config, contraPC)
                    case _ =>
                        gameLoop(state.copy(message = "Inválido. Escolha uma peça preta no centro ou canto"), open, history, config, contraPC)
                }
            case 1 =>
                println("Brancas: Escolham (Linha Coluna) adjacente ao buraco:")
                val coord = (readInt(), readInt())
                (Logic.isAdjacent(coord, open.head), state.board.get(coord)) match {
                    case (true, Some(Stone.White)) =>
                        val nState = state.copy(board = state.board - coord, currentPlayer = Stone.Black, message = "Jogo Iniciado.")
                        gameLoop(nState, coord :: open, state :: history, config, contraPC)
                    case _ =>
                        gameLoop(state.copy(message = "Inválido. Escolha uma branca adjacente."), open, history, config, contraPC)
                }
            case _ => //o jogo mesmo
                /* Logic.verificarVencedor(state.board, state.currentPlayer, open) match {   VERIFICAR VENCEDOR: T5
                    case Some(v) =>
                        println(s"Fim do jogo. O vencedor é: $v")
                        start()
                    case None =>
                        VEM PARA AQUI O CÓDIGO QUE COMEÇA A SEGUIR
                } */
                (contraPC, state.currentPlayer) match { //vamos pedir a jogada
                    case (true, Stone.White) =>
                        println("A processar")
                        val (optBoard, _, newList, _) = Logic.playRandomly(state.board, MyRandom(System.currentTimeMillis()), state.currentPlayer, open, Logic.randomMove)
                        optBoard match {
                            case Some(n) => gameLoop(state.copy(board = n, currentPlayer = Stone.Black, message = "Jogada aleatória feita"), newList, state :: history, config, contraPC)
                            case None => println("Sem movimentos possíveis"); ()
                        }
                    case _ =>
                        println("Comandos: [M] Move | [U] Undo | [R] Restart | [S] Sair")
                        readLine().toUpperCase() match {
                            case "M" => realizarMovimento(state, open, history, config, contraPC)
                            case "U" =>
                                /*Logic.undo(history) match {
                                    case Some((estadoAnterior, novoHistorico)) =>
                                        println("Undo sucedido")
                                        gameLoop(estadoAnterior._1, estadoAnterior._2, novoHistorico, config, contraPC)
                                    case None =>
                                        println("Sem jogadas para anular")
                                */
                                println("O undo está em desenvolvimento")
                                gameLoop(state, open, history, config, contraPC)

                            case "R" => start()
                            case "S" => ()
                            case _ => gameLoop(state, open, history, config, contraPC)
                        }
                }
        }
    }
    def realizarMovimento(state: Game, openCoords: List[Coord2D], history: List[Game], config: GameConfig, contraPC: Boolean): Unit = {
        print("Origem (L C): ")
        val coordFrom = (readInt(), readInt())
        print("Destino (L C): ")
        val coordTo = (readInt(), readInt())

        state.board.get(coordFrom) match {
            case Some(s) if s == state.currentPlayer =>
                val (res, newList) = Logic.play(state.board, state.currentPlayer, coordFrom, coordTo, openCoords)
                res match {
                    case Some(newBoard) =>
                        println("Capturou. Continuar a caputar? (s/n)")
                        readLine().toLowerCase() match {
                            case "s" => gameLoop(state.copy(board = newBoard, message = "Capturou múltiplas peças!"), newList, state :: history, config, contraPC)
                            case _ => gameLoop(state.copy(board = newBoard, currentPlayer = Game.opponent(state.currentPlayer)), newList, state :: history, config, contraPC)
                        }
                    case None => println("Salto inválido")
                    gameLoop(state, openCoords, history, config, contraPC)
                }
            case _ => println("Seleção inválida!")
            gameLoop(state, openCoords, history, config, contraPC)
        }
    }

    start()
}
