import javafx.fxml.FXML
import javafx.scene.layout.{GridPane, Pane}
import javafx.scene.shape.Circle
import javafx.scene.paint.Color
import javafx.scene.control.{Button, Label}
import javafx.scene.input.MouseEvent


class Controller {

    @FXML var boardGrid: GridPane = _
    @FXML var lblStatus: Label = _ //controla as mensagens que vamos mandando aos jogadores ao longo do jogo
    @FXML var btnRestart: Button = _
    @FXML var btnRandom: Button = _
    @FXML var btnUndo: Button = _
    @FXML var lblTimer: Label = _ //controla o tempo de cada jogada

    var showHints: Boolean = true // Fácil e Médio mostram jogadas para onde o jogador pode ir
    var timeLimit: Int = 90 // Fácil = 90s, Médio/Difícil = 30s
    var timer: javafx.animation.Timeline = _ //faz a animação do relógio e do tempo a passar
    var tempoRestante: Int = 90
    var cells: Map[Coord2D, Pane] = Map()
    var history: List[(Game, List[Coord2D])] = List() //guarda o estado da jogada para ser possível fazer o undo
    //aqui em baixo meti as vaiáveis necessárias para o board, como o board na GUI tem uma dimensão fixa, o valor de rows e de cols são "val"
    val ROWS = 6
    val COLS = 6
    var currentBoard: Board = _
    var currentOpenCoords: List[Coord2D] = List() //a lista de coordenadas vazia está incialmente vazia
    var currentPlayer: Stone = Stone.Black //começam as peças pretas
    var selectedCoord: Option[Coord2D] = None //ao ínicio n\ao existe nenhuma peça selecionada
    var currentRand: MyRandom = MyRandom(System.currentTimeMillis()) //variável para as jogadas random

    @FXML
    def initialize(): Unit = {//este metodo serve para mapear os panes colocados no scenebuilder pelo seu ID de forma a poder inicializar o tabuleiro
        boardGrid.getChildren.forEach { node =>
            node match {
                case pane: Pane => //verifico se local onde estou é um pane
                    Option(pane.getId) match {//verifico o seu id
                        case Some(id) if id.startsWith("cell") => //se o id começar com um "cell" ou seja representa uma célula (pq foi o nome que escolhemos dar no javaFX)
                            val r = id.charAt(4).asDigit //o numero da linha é o que está na quarta posição do ID (começa no 0)
                            val c = id.charAt(5).asDigit //o numero da coluna é o que está na quinta posição do ID (começa no 0)
                            cells += (r, c) -> pane
                        case _ => () //caso não exista nenhum Id do pane, n devolve nada
                    }
                case _ => () //caso n encontre nenum objeto pane
            }
        }
        btnRestart.setOnAction(_ => iniciarNovoJogo())// Configuro os botões, neste caso o botão com o id btn.Restart chama o metodo iniciarNovoJogo()
        btnUndo.setText("Undo")
        btnUndo.setOnAction(_ => undoJogada())//o botão undo chega o metodo undoJogada() que volta a jogada atrás
        btnRandom.setText("Jogar Random")
        btnRandom.setOnAction(_ => fazerJogadaRandom())//o botão Jogar Random, usa o metodo fazerJogadaRandom()
        iniciarNovoJogo()// Arranca o jogo
    }
    def iniciarNovoJogo(): Unit = { //metodo que inicia o tabuleiro e arrnaca o jogo
        history = List()
        val inicialGame = Game.initBoard(ROWS, COLS) // Usamos a função Game.initBoard feita na primeira parte do trabalho PROVAVALEMENTE VAI MUDAR PARA O FICHIERO LOGIC COMO DICA DO PROF
        // Para permitir a escolha de tirar das pontas ou centro, o tabuleiro começa CHEIO
        currentBoard = inicialGame.board
        currentOpenCoords = List() // Começa vazia para entrar na fase de remoção
        currentPlayer = Stone.Black
        selectedCoord = None
        renderBoard() //chama a função que "desenha" o visual do tabuleiro
        iniciarTimer() //inicia o timer de cada jogada, dependo do nível de dificuldade escolhido
        lblStatus.setText("Jogo iniciado! Pretas: tirem uma peça do Centro ou Canto.")
    }
    // Função que atualiza o lado visual
    def renderBoard(): Unit = {
        cells.foreach {
            case (coord, pane) =>
            pane.getChildren.clear() //retiro qualquer coisa que possa estar naquele painel ao ínicio do jogo
            pane.setStyle("-fx-border-color: #cccccc; -fx-background-color: transparent;") // cira uma borda  para vermos as grelhas

            currentBoard.get(coord).foreach {
                stone => //para cada coordenada do tabuleiro
                val circle = new Circle(20) // desenhamos uma peça , para começar desenhamos só o criculo com tamanho 20
                circle.setFill(if (stone == Stone.Black) Color.BLACK else Color.WHITE) //depois peenchemos esse círculo dependendo da cor da peça naquele painel
                circle.setStroke(Color.DARKGRAY) //linha de fora do círculo fica a cizento escuro
                circle.setStrokeWidth(2.0) //metemos a largura da linha a 2.0 para ser minimamente vísivel
                // as linhas de baixo são as que permitem que a peça no fique centro da célula
                circle.centerXProperty().bind(pane.widthProperty().divide(2))
                circle.centerYProperty().bind(pane.heightProperty().divide(2))
                pane.getChildren.add(circle)//acrescento o círculo criado ao painel correspondente
            }
        }
    }

    @FXML
    def handleCellClick(event: MouseEvent): Unit = { //função definida no scene builder para o que acontece ao carregar no botões
        val source = event.getSource.asInstanceOf[Pane]
        val id = source.getId
        // extrai as coordenadas clicadas usando a mesma lógica que usamos no initialize()
        val r = id.charAt(4).asDigit
        val c = id.charAt(5).asDigit
        val clickedCoord = (r, c)
        // remove-se as peças iniciais
        currentOpenCoords match {
            case Nil => // Caso a lista de coordenadas vazia esteja vazia (1ª peça das Pretas)
                (Logic.isCenterOrCorner(clickedCoord, ROWS, COLS), currentBoard.get(clickedCoord)) match {
                    case (true, Some(Stone.Black)) => //caso seja uma peça do centro ou do canto e seja a vez da peças pretas
                        removerPecaInicial(clickedCoord) //remove a peça inicial em que se carregou
                        lblStatus.setText("Brancas: tirem uma peça adjacente.")
                    case _ =>
                        lblStatus.setText("Inválido! Escolhe Centro ou Canto (Preto).")
                }
                return

            case firstRemoved :: Nil => // Caso exista apenas uma coordenada na lista (2ª peça das Brancas)
                (Logic.isAdjacent(clickedCoord, firstRemoved), currentBoard.get(clickedCoord)) match {
                    case (true, Some(Stone.White)) => //caso seja uma peça adjacente e uma peça branca
                        removerPecaInicial(clickedCoord)//remove a peça branca escolhida
                        lblStatus.setText("Jogo normal! Vez das Pretas.")
                    case _ =>
                        lblStatus.setText("Inválido! Escolhe uma peça branca adjacente.")
                }
                return

            case _ => // Jogo normal
                selectedCoord match { //ao carregar na peça de origem é o que a torna vede no fundo
                    case None => //caso não exista nenhuma peça selecionada de origem
                        currentBoard.get(clickedCoord) match { //vamos pegar na célula em que o jogador carregou
                            case Some(stone) if stone == currentPlayer => //se for do jogador atual
                                selectedCoord = Some(clickedCoord) // a selected coord passa a ser a peça em que se carregou
                                source.setStyle("-fx-background-color: rgba(0, 255, 0, 0.4); -fx-border-color: #cccccc;") //colocamos o fundo a verde
                                lblStatus.setText(s"Peça $clickedCoord selecionada. Escolhe o destino.") //coloca esta mensagem no painel de jogo

                                if (showHints) {
                                    val destinos = obterDestinosValidos(clickedCoord, currentPlayer, currentBoard)
                                    destinos.foreach(d => destacarCelula(d, "rgba(255, 0, 0, 0.4)"))
                                }

                            case Some(_) => //caso a peça que selecionada para jogar seja do adversário, enviamos uma mensagem a avisar o jogador
                                lblStatus.setText("Esta peça é do teu adversário!")

                            case None => //caso o jogador selecione um painel que não contenha uma peça, enviamos uma mensagem avisar disso
                                lblStatus.setText("Casa vazia! Escolhe uma peça tua para mover.")
                        }

                    case Some(fromCoord) => //caso esteja a carregar numa coordenada de origem que já estava selecionada como coordenada de origem
                        if (clickedCoord == fromCoord) { //caso a coordenada carregada corresponda a uma coordenada que já foi definida como a de origem
                            selectedCoord=None
                            renderBoard() //desseleciono a peça
                        } else { // se carreguei numa outra coordenada, posso fazer  a jogada
                            val (optBoard, newOpenCoords) = Logic.play(
                                currentBoard, currentPlayer, fromCoord, clickedCoord, currentOpenCoords //chamo a lógica do play feita na primeira parte do trabaho[cite: 3]
                            )
                            optBoard match {
                                case Some(newBoard) => //caso a jogada tenha sido válida
                                    history = (Game(currentBoard, ROWS, COLS, currentPlayer, "Undo realizado"), currentOpenCoords) :: history
                                    currentBoard = newBoard //criamos um novo tabuleiro, para retirar as peças da posição em que estavam
                                    currentOpenCoords = newOpenCoords //atualiza a lista de posições vazias

                                    // Verifica se pode saltar novamente com a MESMA peça
                                    if (podeSaltarMais(newBoard, clickedCoord, currentPlayer)) {
                                        selectedCoord = Some(clickedCoord) // Mantém a peça selecionada no novo lugar
                                        renderBoard()
                                        destacarCelula(clickedCoord, "rgba(255, 255, 0, 0.4)") // coloquei a amarelo para indicar que pode continuar

                                        val proximosDestinos = obterDestinosValidos(clickedCoord, currentPlayer, newBoard)
                                        proximosDestinos.foreach(d => destacarCelula(d, "rgba(255, 0, 0, 0.4)"))

                                        lblStatus.setText("Captura múltipla! Continua ou clica na peça para terminar.")
                                    } else {
                                        finalizarTurno()
                                    }

                                case None => //caso o play não ocorra com sucesso (logo não devolve nenhum tabuleiro)
                                    lblStatus.setText("Salto inválido! Escolhe novamente.") //enviamos uma mensagem de erro para o jogador
                                    // Se não houver salto múltiplo disponível, limpamos a seleção
                                    if (!podeSaltarMais(currentBoard, fromCoord, currentPlayer)) {
                                        selectedCoord = None
                                        renderBoard()
                                    }
                            }
                        }
                }
        }
    }
    def finalizarTurno(): Unit = {
        Logic.verificarVencedor(currentBoard, ROWS, COLS, currentPlayer, currentOpenCoords) match {
            case Some(vencedor) =>
                if (timer != null) timer.stop()
                lblTimer.setText("")
                return
            case None => ()
        }

        currentPlayer = Game.opponent(currentPlayer)
        selectedCoord = None
        renderBoard()
        val nomeJogador = if (currentPlayer == Stone.Black) "Pretas" else "Brancas"
        lblStatus.setText(s"Vez das $nomeJogador.")
        iniciarTimer() // <-- acrescenta esta linha
    }

    def undoJogada(): Unit = {
        Logic.undo(history) match {
            case None =>
                lblStatus.setText("Não há mais jogadas para desfazer!")
            case Some(((oldGame, oldOpenCoords), remainingHistory)) =>
                // Atualiza as variáveis do Controller com os dados do objeto Game recuperado
                currentBoard = oldGame.board
                currentPlayer = oldGame.currentPlayer
                // Nota: ROWS e COLS são 'val' no  controller, por isso não mudam,
                //Atualiza a lista de buracos e o histórico
                currentOpenCoords = oldOpenCoords
                history = remainingHistory
                //Limpa seleções visuais e redesenha o tabuleiro
                selectedCoord = None
                renderBoard()
                // Atualiza a mensagem na UI
                val nome = if (currentPlayer == Stone.Black) "Pretas" else "Brancas"
                lblStatus.setText(s"Desfeito! Vez das $nome.")
                iniciarTimer()
        }
    }
    def removerPecaInicial(coord: Coord2D): Unit = { //metodo que remove visualmente a peça do tabuleiro
        history = (Game(currentBoard, ROWS, COLS, currentPlayer, "Undo realizado"), currentOpenCoords) :: history
        currentBoard = currentBoard - coord
        currentOpenCoords = coord :: currentOpenCoords //adiciono essa coordernada à lista de coordenadas livres
        currentPlayer = Game.opponent(currentPlayer)//troco de jogador
        renderBoard()
    }

    def obterDestinosValidos(pos: Coord2D, p: Stone, board: Board): List[Coord2D] = {//metodo que verifica os destinos validos para poder assinala-los a vermelho lá em cima
        val directions = List((2, 0), (-2, 0), (0, 2), (0, -2)) //crio uma lista de direções com as 4 direções que me posso mover
        directions.flatMap { case (dr, dc) =>
            val target = (pos._1 + dr, pos._2 + dc)
            val mid = (pos._1 + dr / 2, pos._2 + dc / 2)

            val dentro = target._1 >= 0 && target._1 < ROWS && target._2 >= 0 && target._2 < COLS
            if (dentro && !board.contains(target) && board.get(mid).exists(_ != p)) {
                Some(target) // Destino válido encontrado
            } else None
        }
    }

    def fazerJogadaRandom(): Unit = { // Implementa a Tarefa T3
        currentOpenCoords match {
            case Nil => // Vez das Pretas removerem a 1ª peça (Centro ou Canto)
                val opcoes = for {
                    r <- 0 until ROWS
                    c <- 0 until COLS
                    coord = (r, c)
                    if Logic.isCenterOrCorner(coord, ROWS, COLS) && currentBoard.get(coord).contains(Stone.Black)
                } yield coord

                val (escolha, nextRand) = Logic.randomMove(opcoes.toList, currentRand)
                currentRand = nextRand
                removerPecaInicial(escolha)
                lblStatus.setText("Random removeu 1ª peça. Vez das Brancas.")

            case firstRemoved :: Nil => // Vez das Brancas removerem a 2ª peça (Adjacente)
                val opcoes = for {
                    r <- 0 until ROWS
                    c <- 0 until COLS
                    coord = (r, c)
                    if Logic.isAdjacent(coord, firstRemoved) && currentBoard.get(coord).contains(Stone.White)
                } yield coord

                val (escolha, nextRand) = Logic.randomMove(opcoes.toList, currentRand)
                currentRand = nextRand
                removerPecaInicial(escolha)
                lblStatus.setText("Random removeu 2ª peça. Jogo iniciado!")

            case _ => // Jogo normal (o código que já tinhas)
                history = (Game(currentBoard, ROWS, COLS, currentPlayer, "Undo realizado"), currentOpenCoords) :: history

                def realizarMovimentosRandom(board: Board, rand: MyRandom, open: List[Coord2D], lastTo: Option[Coord2D]): Unit = {
                    val (optBoard, nextRand, newList, coordTo) = lastTo match {
                        case None => Logic.playRandomly(board, rand, currentPlayer, open, Logic.randomMove)
                        case Some(pos) =>
                            val possibleTo = List((pos._1 + 2, pos._2), (pos._1 - 2, pos._2), (pos._1, pos._2 + 2), (pos._1, pos._2 - 2))
                                .filter(t => t._1 >= 0 && t._1 < ROWS && t._2 >= 0 && t._2 < COLS && !board.contains(t))

                            if (possibleTo.isEmpty) (None, rand, open, None)
                            else {
                                val (target, nr) = Logic.randomMove(possibleTo, rand)
                                val (nb, nl) = Logic.play(board, currentPlayer, pos, target, open)
                                (nb, nr, nl, Some(target))
                            }
                    }

                    optBoard match {
                        case Some(nb) =>
                            currentBoard = nb
                            currentRand = nextRand
                            currentOpenCoords = newList
                            renderBoard()
                            coordTo match {
                                case Some(to) if podeSaltarMais(nb, to, currentPlayer) =>
                                    realizarMovimentosRandom(nb, nextRand, newList, Some(to))
                                case _ => finalizarTurno()
                            }
                        case None =>
                            lblStatus.setText("Não há movimentos possíveis para o Random!")
                            // Se falhou o random, removemos do histórico para o undo não bugar
                            history = history.tail
                    }
                }

                realizarMovimentosRandom(currentBoard, currentRand, currentOpenCoords, None)
        }
    }
    def podeSaltarMais(board: Board, pos: Coord2D, p: Stone): Boolean = {//metodo que verifica a captura multipla
        val directions = List((2, 0), (-2, 0), (0, 2), (0, -2))

        // exists verifica se pelo menos uma direção permite o salto
        directions.exists { case (dr, dc) =>
            val target = (pos._1 + dr, pos._2 + dc)
            val mid = (pos._1 + dr / 2, pos._2 + dc / 2)

            val dentro = target._1 >= 0 && target._1 < ROWS && target._2 >= 0 && target._2 < COLS

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

    def destacarCelula(c: Coord2D, cor: String): Unit = { //mete o fundo da celula a verde
        cells.get(c).foreach(_.setStyle(s"-fx-background-color: $cor; -fx-border-color: #cccccc;"))
    }

    def setDifficulty(diff: Difficulty): Unit = {
        diff match {
            case Difficulty.Facil => showHints = true; timeLimit = 90
            case Difficulty.Medio => showHints = true; timeLimit = 30
            case Difficulty.Dificil => showHints = false; timeLimit = 30
        }
    }

    def iniciarTimer(): Unit = {
        if (timer != null) timer.stop()
        tempoRestante = timeLimit
        lblTimer.setText(s"⏱ $tempoRestante s")

        timer = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), _ => {
                tempoRestante -= 1
                val cor = if (tempoRestante <= 10) "-fx-text-fill: red;" else "-fx-text-fill: #333;"
                lblTimer.setStyle(s"-fx-font-size: 20px; -fx-font-weight: bold; $cor")
                lblTimer.setText(s"⏱ $tempoRestante s")
                if (tempoRestante <= 0) {
                    timer.stop()
                    lblTimer.setText("⏱ Tempo esgotado!")
                    finalizarTurno()
                }
            })
        )
        timer.setCycleCount(javafx.animation.Animation.INDEFINITE)
        timer.play()
    }
}