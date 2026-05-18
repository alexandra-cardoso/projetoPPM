import Stone.Black
import scala.io.StdIn.*
import scala.annotation.tailrec

object TUI extends App {
    case class GameConfig(rows: Int, cols: Int, timerSeconds: Long, difficulty: Int)
    
    @tailrec
    def makeHeader(current: Int, max: Int): Unit = { // faz a primeira linha do tabuleiro dinamicamente para acompanhar o tamanho definido pelo jogador
        current < max match {
            case true =>
                current == 0 match {
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
                col == 0 match {
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

    def start(): Unit = { //inicia a TUI
        println("=== BEM-VINDO AO KÖNANE (TUI) ===")
        mainMenu(GameConfig(8, 8, 90, 1)) //valores padrão, definidos por nós
    }

    @tailrec
    def mainMenu(config: GameConfig): Unit = { //isto é o menu que é logo chamado assim que começa o jogo, onde o jogador define as coisas que quiser mudar
        println(s"\n[MENU] Tabuleiro: ${config.rows}x${config.cols} | Tempo: ${config.timerSeconds}s | Dificuldade: ${config.difficulty}")
        println("1. Jogo local") //contra outro player no mesmo pc
        println("2. Jogo contra Computador") //contra jogadas aleatórias
        println("3. Alterar Dimensões") //altera as dimensões do board
        println("4. Definir Tempo Limite") //mudar o tempo max de jogada
        println("5. Definir Dificuldade") //mudar a dificuldade
        println("0. Sair")
        print("Opção: ")

        readLine() match {
            case "1" =>
                val game = Game.initBoard(config.rows, config.cols) //inicia o tabuleiro
                gameLoop(game, List(), List(), config, contraPC = false) //começa o jogo assim
                mainMenu(config) //para quando acabar o jogo
            case "2" =>
                val game = Game.initBoard(config.rows, config.cols) //iniciamos o board
                gameLoop(game, List(), List(), config, contraPC = true) //começamos o jogo com contraPC a true para ele saber que vai jogar aleatoriamente
                mainMenu(config)
            case "3" => //pede os valores de linhas e colunas
                print("Linhas: ")
                val li = readInt()
                print("Colunas: ")
                val col = readInt()
                mainMenu(config.copy(rows = li, cols = col)) //altera a configuração do jogo
            case "4" =>
                print("Tempo máx de jogada: ")
                mainMenu(config.copy(timerSeconds = readLong())) //altera a config do jogo
            case "5" => //mostra o que cada dificuldade faz
                println("\nEscolhe a dificuldade:")
                println("1. Fácil: Dá 90s por jogada")
                println("2. Médio: Dá 60s por jogada")
                println("3. Difícil: Dá 30s por jogada e o Undo deixa de ser possível")
                print("Opção: ")

                readInt() match { //muda a config do jogo de acordo com a resposta do user
                    case 1 => mainMenu(config.copy(difficulty = 1, timerSeconds = 90))
                    case 2 => mainMenu(config.copy(difficulty = 2, timerSeconds = 60))
                    case 3 => mainMenu(config.copy(difficulty = 3, timerSeconds = 30))
                    case _ =>
                        println("Dificuldade inválida")
                        mainMenu(config)
                }
            case "0" => println("A fechar...") //fecha o jogo
            case _ =>
                println("Opção Inválida!")
                mainMenu(config) //volta ao menu
        }
    }

    @tailrec
    def gameLoop(state: Game, open: List[Coord2D], history: List[(Game, List[Coord2D])], config: GameConfig, contraPC: Boolean): Unit = {
        render(state) //desenha completamente o tabuleiro: header e o tabuleiro em si (draw)
        val pStr = state.currentPlayer match { //vê que player é apenas para lhe imprimir o "nome" (no caso a cor)
            case Stone.Black => "Pretas"
            case Stone.White => "Brancas"
        }
        println(s"\nMensagem: ${state.message}")
        println(s"Vez das: $pStr")

        open.length match { //verifica quantas posições estão abertas
            case 0 => //se não houver nenhuma, as pretas têm de tirar uma peça dos cantos ou meio
                println("Pretas: Escolha uma peça para remover (Centro ou canto)")
                val coord = (readInt(), readInt()) //lê a coordenada da peça que vai sair
                (Game.isCenterOrCorner(coord, config.rows, config.cols), state.board.get(coord)) match { //verificamos com uma fç auxiliar se é uma peça que possa ser retirada(canto ou centro)
                    case (true, Some(Stone.Black)) => //se der e for peça preta
                        val nState = state.copy(board = state.board - coord, currentPlayer = Stone.White, message = "Peça removida") //fazemos um novo estado em que ela foi removida corretamente
                        gameLoop(nState, List(coord), (state, open) :: history, config, contraPC) //e continua o gameLoop(no caso, recomeça)
                    case _ => //caso contrário
                        gameLoop(state.copy(message = "Inválido. Escolha uma peça preta no centro ou canto"), open, history, config, contraPC)
                }
            case 1 => //após a primeira peça ser removida, as peças brancas têm de retirar uma adjacente
                println("Brancas: Escolham (Linha Coluna) adjacente ao buraco:")
                val coord = (readInt(), readInt()) //lemos a coordenada
                (Game.isAdjacent(coord, open.head), state.board.get(coord)) match { //verificamos se é adjacente à peça preta que foi removida
                    case (true, Some(Stone.White)) =>
                        val nState = state.copy(board = state.board - coord, currentPlayer = Stone.Black, message = "Jogo Iniciado.")
                        gameLoop(nState, coord :: open, (state, open) :: history, config, contraPC) //recomeçamos o loop agora com os 2 lugares livres
                    case _ => //caso contrário
                        gameLoop(state.copy(message = "Inválido. Escolha uma branca adjacente."), open, history, config, contraPC)
                }
            case _ => //o jogo mesmo
                Game.verificarVencedor(state.board, config.rows, config.cols, state.currentPlayer, open) match { //verificamos logo a condição de paragem = alguém venceu
                    case Some(v) => //se houver vencedor
                        val nome = v match {
                            case Stone.White => "Brancas"
                            case Stone.Black => "Pretas"
                        }
                        println(s"Fim do jogo. O vencedor é: $nome") //imprimimos o nome e recomeçamos, com o start.
                        start()
                    case None => //caso não haja, joga-se normalmente
                        (contraPC, state.currentPlayer) match { //vamos pedir a jogada
                            case (true, Stone.White) => //estamos a jogar contra um pc e é o pc a jogar (brancas). Isto, porque assumimos sempre que as peças pretas começam o jogo
                                println("A processar")
                                val (optBoard, _, newList, _) = Game.playRandomly(state.board, MyRandom(System.currentTimeMillis()), state.currentPlayer, open, Game.randomMove) //chamamos a função desenvolvida na primeira parte
                                optBoard match { //verificamos o tabuleiro, se é válido ou não
                                    case Some(n) => gameLoop(state.copy(board = n, currentPlayer = Stone.Black, message = "Jogada aleatória feita"), newList, (state, open) :: history, config, contraPC) //recomeçamos o loop com outra jogada (mudámos de player)
                                    case None => println("Sem movimentos possíveis"); ()
                                }
                            case _ => //pelo contrário, se estou a jogar contra outro player no mesmo sítio
                                config.difficulty match {
                                    case 3 => println("Comandos: [M] Move | [R] Restart | [S] Sair") //se a dificuldade for 3, o undo fica inacessível
                                    case _ => println("Comandos: [M] Move | [U] Undo | [R] Restart | [S] Sair") //damos uma opção de comandos possíveis
                                }

                                readLine().toUpperCase() match {
                                    case "M" => realizarMovimento(state, open, history, config, contraPC) //com M, realizamos o movimento pedido na função
                                    case "U" => //caso queira fazer undo
                                        config.difficulty == 3 match {
                                            case true => //caso esteja em dificuldade "Difícil"
                                                println("Estás em dificuldade 3. Não podes fazer Undo")
                                                gameLoop(state, open, history, config, contraPC) //recomeça o turno, com tudo igual
                                            case false => //caso não esteja
                                                Game.undo(history) match { //vamos ao histórico
                                                    case Some(((estadoAnterior, openAnterior), novoHistorico)) => //se existir histórico de jogadas
                                                        println("Undo sucedido")
                                                        gameLoop(estadoAnterior, openAnterior, novoHistorico, config, contraPC) //recomeçamos o loop, tendo desfeito uma jogada
                                                    case None => //se não existir
                                                        println("Sem jogadas para anular")
                                                        gameLoop(state, open, history, config, contraPC) //recomeçamos o loop, igual ao que estava antes
                                                }
                                        }

                                    case "R" => start() //recomeçamos tudo
                                    case "S" => () //acaba o programa
                                    case _ => gameLoop(state, open, history, config, contraPC) //recomeçamos o loop no mesmo estado em que estava, se o utilizador escrever algo diferente
                                }
                        }
                }
        }
    }

    @tailrec
    def realizarMovimento(state: Game, openCoords: List[Coord2D], history: List[(Game, List[Coord2D])], config: GameConfig, contraPC: Boolean, coordFromOpt: Option[Coord2D] = None): Unit = { //a função que realmente vai mexer as peças
        val tempoI = System.currentTimeMillis() / 1000 //começamos por guardar já o tempo atual

        //vamos ver se este movimento é a continuação dum salto com muitos movimentos ou se é o inicial da jogada
        val coordFrom = coordFromOpt match { //se for uma continuação de jogada, recebe um coordFromOpt com valores
            case None => //se não tivermos coordenada guardada na variável, é a 1ª jogada
                print("Origem (L C): ")
                (readInt(), readInt()) //as coordenadas são a combinação do escrito no terminal
            case Some(pos) => //se já houver posição, estamos a continuar a jogar, apenas
                println(s"\n(Captura Múltipla) A tua peça está em $pos. Podes continuar a capturar")
                pos //assim sendo, a coordenada de inicio é a que chegou de argumento à função
        }
        print("Destino (L C): ") //vamos buscar o destino / novo destino
        val coordTo = (readInt(), readInt()) //vamos buscar a coordenada escrita

        val tempoF = System.currentTimeMillis() / 1000 //anotamos agora o tempo final
        val tempoDec = tempoF - tempoI //fazemos a diferença

        tempoDec <= config.timerSeconds match { //verificamos se o tempo de jogada ainda não ultrapassou o tempo limite
            case false => //passou o tempo limite
                val proxEstado = state.copy(currentPlayer = Game.opponent(state.currentPlayer), message = "Passou a vez por excesso de tempo") //trocamos o jogador, passamos a vez, se o tempo se tiver esgotado
                gameLoop(proxEstado, openCoords, history, config, contraPC) //recomeçamos o loop

            case true => //ainda podemos jogar
                state.board.get(coordFrom) match { //vamos ver a coord de onde vimos
                    case Some(s) if s == state.currentPlayer =>
                        Game.play(state.board, state.currentPlayer, coordFrom, coordTo, openCoords) match { //vamos jogar
                            case (Some(newBoard), newList) => //se devolver uma jogada feita = newBoard e lista de coords livres nova
                                val estadoComSalto = state.copy(board = newBoard, message = s"Peça movida para $coordTo") // temos um novo estado
                                render(estadoComSalto) //e desenhamos esse novo estado no terminal

                                //calculamos se dá para capturar mais
                                Game.podeSaltarMais(newBoard, coordTo, state.currentPlayer, config.rows, config.cols) match { //chamamos a fç auxiliar para isso
                                    case true =>
                                        println("\nPodes fazer mais capturas com esta peça. Queres continuar? (s/n)")
                                        readLine().toLowerCase() match {
                                            case "s" => realizarMovimento(estadoComSalto, newList, (state, openCoords) :: history, config, contraPC, Some(coordTo)) //se quero continuar a capturar, vou recursivamente a esta função para capturar mais, com o novo estado e passando a coordenada para onde nos movemos como coordFromOpt
                                            case _ => gameLoop(estadoComSalto.copy(currentPlayer = Game.opponent(state.currentPlayer), message = "Turno terminado"), newList, (state, openCoords) :: history, config, contraPC) //se não quiser, passa a vez
                                        }
                                    case false => //se não podermos saltar mais, nem se pergunta
                                        println("\nCaptura feita!") //confirmamos que capturámos
                                        gameLoop(estadoComSalto.copy(currentPlayer = Game.opponent(state.currentPlayer)), newList, (state, openCoords) :: history, config, contraPC) //e passamos a vez
                                }

                            case (None, _) => //se não pudemos fazer o salto
                                println("Salto inválido")
                                realizarMovimento(state, openCoords, history, config, contraPC, coordFromOpt) //voltamos a pedir uma jogada válida, recursivamente
                        }
                    case _ => //inválido = recomeçamos o loop
                        println("Seleção inválida!")
                        gameLoop(state, openCoords, history, config, contraPC)
                }
        }
    }

    start() //isto tudo começa com o start()
}