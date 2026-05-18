import Stone.Black
import scala.io.StdIn.*
import scala.annotation.tailrec

object TUI extends App {
    case class GameConfig(rows: Int, cols: Int, timerSeconds: Long, difficulty: Int)
    
    @tailrec
    def makeHeader(current: Int, max: Int): Unit = { // faz a primeira linha do tabuleiro dinamicamente para acompanhar o tamanho definido pelo jogador
        (current < max) match {
            case true =>
                (current == 0) match {
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
        println("  " + "-" * (game.cols * 3 + 1))
        draw(game, 0, 0)
    }

    @tailrec
    def draw(game: Game, r: Int, c: Int): Unit = {
        (r, c) match {
            case (row, 0) if row == game.rows => //caso esteja no final do tabuleiro
                println("  " + "-" * (game.cols * 3 + 1))

            case (row, col) if col == game.cols => // para todas as linhas com y=8 vai meter um | no final da linha
                println("|")
                draw(game, r + 1, 0) // desenha o board até aí e recomeça na linha asseguir recursivamente
            case (row, col) => //caso não se saiba nenhum dos valores
                (col == 0) match {
                    case true => print(s"$row |")
                    case false => ()
                } // se a coluna for a de índice 0 ou seja inicio da linha ele imprime o número da linha + a |
                game.board.get((row, col)) match {
                    case Some(Stone.Black) => print(" B ")
                    case Some(Stone.White) => print(" W ")
                    case None => print(" . ")
                }
                draw(game, row, col + 1)
        }
    }

    def start(): Unit = {
        println("=== BEM-VINDO AO KÖNANE (TUI) ===")
        mainMenu(GameConfig(8, 8, 90, 1)) //valores padrão, definidos por nós
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
                println("\nEscolhe a dificuldade:")
                println("1. Fácil: Dá 90s por jogada")
                println("2. Médio: Dá 60s por jogada")
                println("3. Difícil: Dá 30s por jogada")
                print("Opção: ")

                readInt() match {
                    case 1 => mainMenu(config.copy(difficulty = 1, timerSeconds = 90))
                    case 2 => mainMenu(config.copy(difficulty = 2, timerSeconds = 60))
                    case 3 => mainMenu(config.copy(difficulty = 3, timerSeconds = 30))
                    case _ =>
                        println("Dificuldade inválida")
                        mainMenu(config)
                }
            case "0" => println("A fechar...")
            case _ =>
                println("Opção Inválida!")
                mainMenu(config)
        }
    }

    @tailrec
    def gameLoop(state: Game, open: List[Coord2D], history: List[(Game, List[Coord2D])], config: GameConfig, contraPC: Boolean): Unit = {
        render(state)
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
                (Game.isCenterOrCorner(coord, config.rows, config.cols), state.board.get(coord)) match {
                    case (true, Some(Stone.Black)) =>
                        val nState = state.copy(board = state.board - coord, currentPlayer = Stone.White, message = "Peça removida")
                        gameLoop(nState, List(coord), (state, open) :: history, config, contraPC)
                    case _ =>
                        gameLoop(state.copy(message = "Inválido. Escolha uma peça preta no centro ou canto"), open, history, config, contraPC)
                }
            case 1 =>
                println("Brancas: Escolham (Linha Coluna) adjacente ao buraco:")
                val coord = (readInt(), readInt())
                (Game.isAdjacent(coord, open.head), state.board.get(coord)) match {
                    case (true, Some(Stone.White)) =>
                        val nState = state.copy(board = state.board - coord, currentPlayer = Stone.Black, message = "Jogo Iniciado.")
                        gameLoop(nState, coord :: open, (state, open) :: history, config, contraPC)
                    case _ =>
                        gameLoop(state.copy(message = "Inválido. Escolha uma branca adjacente."), open, history, config, contraPC)
                }
            case _ => //o jogo mesmo
                Game.verificarVencedor(state.board, config.rows, config.cols, state.currentPlayer, open) match {
                    case Some(v) =>
                        val nome = v match {
                            case Stone.White => "Brancas"
                            case Stone.Black => "Pretas"
                        }
                        println(s"Fim do jogo. O vencedor é: $nome")
                        start()
                    case None =>
                        (contraPC, state.currentPlayer) match { //vamos pedir a jogada
                            case (true, Stone.White) =>
                                println("A processar")
                                val (optBoard, _, newList, _) = Game.playRandomly(state.board, MyRandom(System.currentTimeMillis()), state.currentPlayer, open, Logic.randomMove)
                                optBoard match {
                                    case Some(n) => gameLoop(state.copy(board = n, currentPlayer = Stone.Black, message = "Jogada aleatória feita"), newList, (state, open) :: history, config, contraPC)
                                    case None => println("Sem movimentos possíveis"); ()
                                }
                            case _ =>
                                println("Comandos: [M] Move | [U] Undo | [R] Restart | [S] Sair")
                                readLine().toUpperCase() match {
                                    case "M" => realizarMovimento(state, open, history, config, contraPC)
                                    case "U" =>
                                        Game.undo(history) match {
                                            case Some(((estadoAnterior, openAnterior), novoHistorico)) =>
                                                println("Undo sucedido")
                                                gameLoop(estadoAnterior, openAnterior, novoHistorico, config, contraPC)
                                            case None =>
                                                println("Sem jogadas para anular")
                                                gameLoop(state, open, history, config, contraPC)
                                        }
                                    case "R" => start()
                                    case "S" => ()
                                    case _ => gameLoop(state, open, history, config, contraPC)
                                }
                        }
                }
        }
    }


    def realizarMovimento(state: Game, openCoords: List[Coord2D], history: List[(Game, List[Coord2D])], config: GameConfig, contraPC: Boolean, coordFromOpt: Option[Coord2D] = None): Unit = {
        val tempoI = System.currentTimeMillis() / 1000

        //vamos ver se este movimento é a continuação dum salto com muitos movimentos ou se é o inicial da jogada
        val coordFrom = coordFromOpt match { //se for uma continuação de jogada, recebe um coordFromOpt com valores
            case None => //se não tivermos coordenada guardada na variável, é a 1ª jogada
                print("Origem (L C): ")
                (readInt(), readInt()) //as coordenadas são a combinação do escrito no terminal
            case Some(pos) =>
                println(s"\n(Captura Múltipla) A tua peça está em $pos. Podes continuar a capturar")
                pos
        }
        print(coordFromOpt match {
            case None => "Destino (L C): "
            case Some(_) => "Novo Destino (L C): "
        })
        val coordTo = (readInt(), readInt()) //vamos buscar a coordenada escrita

        val tempoF = System.currentTimeMillis() / 1000
        val tempoDec = tempoF - tempoI

        tempoDec <= config.timerSeconds match { //verificamos se o tempo de jogada ainda não ultrapassou o tempo limite
            case false => //passou o tempo limite
                val proxEstado = state.copy(currentPlayer = Game.opponent(state.currentPlayer), message = "Passou a vez por excesso de tempo")
                gameLoop(proxEstado, openCoords, history, config, contraPC)

            case true => //ainda podemos jogar
                state.board.get(coordFrom) match {
                    case Some(s) if s == state.currentPlayer =>
                        Game.play(state.board, state.currentPlayer, coordFrom, coordTo, openCoords) match {
                            case (Some(newBoard), newList) =>
                                val estadoComSalto = state.copy(board = newBoard, message = s"Peça movida para $coordTo")
                                render(estadoComSalto)

                                //calculamos se dá para capturar mais
                                Game.podeSaltarMais(newBoard, coordTo, state.currentPlayer, config.rows, config.cols) match {
                                    case true =>
                                        println("\nPodes fazer mais capturas com esta peça. Queres continuar? (s/n)")
                                        readLine().toLowerCase() match {
                                            case "s" => realizarMovimento(estadoComSalto, newList, (state, openCoords) :: history, config, contraPC, Some(coordTo))
                                            case _ => gameLoop(estadoComSalto.copy(currentPlayer = Game.opponent(state.currentPlayer), message = "Turno terminado"), newList, (state, openCoords) :: history, config, contraPC)
                                        }
                                    case false =>
                                        println("\nCaptura feita!")
                                        gameLoop(estadoComSalto.copy(currentPlayer = Game.opponent(state.currentPlayer)), newList, (state, openCoords) :: history, config, contraPC)
                                }

                            case (None, _) =>
                                println("Salto inválido")
                                realizarMovimento(state, openCoords, history, config, contraPC, coordFromOpt)
                        }
                    case _ =>
                        println("Seleção inválida!")
                        gameLoop(state, openCoords, history, config, contraPC)
                }
        }
    }
    start()
}