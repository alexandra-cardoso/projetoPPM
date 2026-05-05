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
    @FXML var btnUndo: Button = _

    var cells: Map[Coord2D, Pane] = Map()

    //aqui em baixo meti as vaiáveis necessárias para o board, como o board na GUI tem uma dimensão fixa, o valor de rows e de cols são "val"
    val ROWS = 6
    val COLS = 6
    var currentBoard: Board = _
    var currentOpenCoords: List[Coord2D] = List() //a lista de coordenadas vazia está incialmente vazia
    var currentPlayer: Stone = Stone.Black //começam as peças pretas
    var selectedCoord: Option[Coord2D] = None //ao ínicio n\ao existe nenhuma peça selecionada

    // Variável para a Tarefa T1/T3
    var currentRand: MyRandom = MyRandom(System.currentTimeMillis())

    @FXML
    def initialize(): Unit = {//este metodo serve para mapear os paines colocados no scenebuilder pelo seu ID de forma a poder inicializar o tabuleiro
        boardGrid.getChildren.forEach { node => //no fxml, cada painel foi identificado da forma cellRC (R=Row,C=Col)
            if (node.isInstanceOf[Pane]) {
                val pane = node.asInstanceOf[Pane]
                val id = pane.getId
                if (id != null && id.startsWith("cell")) { //do nome dado, extraí a linha e a coluna onde está.
                    val r = id.charAt(4).asDigit
                    val c = id.charAt(5).asDigit
                    cells += (r, c) -> pane
                }
            }
        }
        btnRestart.setOnAction(_ => iniciarNovoJogo())// Configuro os botões, neste caso o botão com o id btn.Restart chama o metodo iniciarNovoJogo()

        // neste momento o botão undo chama-se Jogar Random, quando o rui fizer o undo acrescento outro botão para a jogada random fácil
        btnUndo.setText("Jogar Random")
        btnUndo.setOnAction(_ => fazerJogadaRandom())

        iniciarNovoJogo()// Arranca o jogo
    }

    def iniciarNovoJogo(): Unit = { //metodo que inicia o tabuleiro e arrnaca o jogo

        val inicialGame = Game.initBoard(ROWS, COLS) // Usamos a função Game.initBoard feita na primeira parte do trabalho PROVAVALEMENTE VAI MUDAR PARA O FICHIERO LOGIC COMO DICA DO PROF

        // Para permitir a escolha de tirar das pontas ou centro, o tabuleiro começa CHEIO
        currentBoard = inicialGame.board
        currentOpenCoords = List() // Começa vazia para entrar na fase de remoção
        currentPlayer = Stone.Black
        selectedCoord = None

        renderBoard() //chama a função que "desenha" o visual do tabuleiro
        lblStatus.setText("Jogo iniciado! Pretas: tirem uma peça do Centro ou Canto.")
    }

    // Função que atualiza o lado visual
    def renderBoard(): Unit = {
        cells.foreach { case (coord, pane) =>
            pane.getChildren.clear() //retiro qualquer coisa que possa estar naquele painel ao ínicio do jogo
            pane.setStyle("-fx-border-color: #cccccc; -fx-background-color: transparent;") // cira uma borda subtil para vermos as grelhas

            currentBoard.get(coord).foreach { stone => //para cada coordenada do tabuleiro
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
        // extrai as coordenadas clicadas
        val r = id.charAt(4).asDigit
        val c = id.charAt(5).asDigit
        val clickedCoord = (r, c)

        // remove-se as peças iniciais
        currentOpenCoords match {
            case Nil => // Caso a lista de coordenadas vazia esteja vazia (1ª peça das Pretas)
                if (isCenterOrCorner(clickedCoord) && currentBoard.get(clickedCoord).contains(Stone.Black)) {
                    removerPecaInicial(clickedCoord)
                    lblStatus.setText("Brancas: tirem uma peça adjacente.")
                } else {
                    lblStatus.setText("Inválido! Escolhe Centro ou Canto (Preto).")
                }
                return

            case firstRemoved :: Nil => // Caso exista apenas uma coordenada na lista (2ª peça das Brancas)
                if (isAdjacent(clickedCoord, firstRemoved) && currentBoard.get(clickedCoord).contains(Stone.White)) {
                    removerPecaInicial(clickedCoord)
                    lblStatus.setText("Jogo normal! Vez das Pretas.")
                } else {
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

                                // NOVO: Mostrar destinos válidos a vermelho (Tarefa T8)[cite: 1]
                                val destinos = obterDestinosValidos(clickedCoord, currentPlayer, currentBoard)
                                destinos.foreach(d => destacarCelula(d, "rgba(255, 0, 0, 0.4)"))

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
        currentPlayer = Game.opponent(currentPlayer) //muda de jogador
        selectedCoord = None //meto a coordenada selecionada a None
        renderBoard() //desenho o tabuleiro
        val nomeJogador = if (currentPlayer == Stone.Black) "Pretas" else "Brancas"
        lblStatus.setText(s"Vez das $nomeJogador.")
    }

    def removerPecaInicial(coord: Coord2D): Unit = { //metodo que remove visualmente a peça do tabuleiro
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
        // Função interna recursiva para permitir que  realize saltos múltiplos
        def realizarMovimentosIA(board: Board, rand: MyRandom, open: List[Coord2D], lastTo: Option[Coord2D]): Unit = {
            val (optBoard, nextRand, newList, coordTo) = lastTo match {
                case None => Logic.playRandomly(board, rand, currentPlayer, open, Logic.randomMove)
                case Some(pos) =>
                    // Procura um destino válido a partir da posição atual da peça que está a saltar[cite: 4]
                    val possibleTo = List((pos._1+2, pos._2), (pos._1-2, pos._2), (pos._1, pos._2+2), (pos._1, pos._2-2))
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
                            // continua a saltar se houver mais capturas disponíveis
                            realizarMovimentosIA(nb, nextRand, newList, Some(to))
                        case _ => finalizarTurno()
                    }
                case None => if (lastTo.isEmpty) lblStatus.setText("IA não encontrou jogadas!") else finalizarTurno()
            }
        }
        realizarMovimentosIA(currentBoard, currentRand, currentOpenCoords, None)
    }

    def isCenterOrCorner(c: Coord2D): Boolean = {//metodo que faz a verificação inicial de ser uma ponta ou um centro para saber se posso etirar a pedra
        val centers = List((2,2), (2,3), (3,2), (3,3))
        val corners = List((0,0), (0,5), (5,0), (5,5))
        centers.contains(c) || corners.contains(c)
    }

    def isAdjacent(c1: Coord2D, c2: Coord2D): Boolean = {//metodo se vê se a posição é adjacente a outra que recebe, tambêm para a verificação de remoção incial
        Math.abs(c1._1 - c2._1) + Math.abs(c1._2 - c2._2) == 1
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

}