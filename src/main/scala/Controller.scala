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
        iniciarNovoJogo()// Arranca o jogo
    }

    def iniciarNovoJogo(): Unit = { //metodo que inicia o tabuleiro e arrnaca o jogo

        val inicialGame = Game.initBoard(ROWS, COLS) // Usamos a função Game.initBoard feita na primeira parte do trabalho PROVAVALEMENTE VAI MUDAR PARA O FICHIERO LOGIC COMO DICA DO PROF

        // Posições  DAS PEDRAS DO CENTRO, FALTA FAZER A ESCOLHA DE TIRAR DAS PONTAS!
        val midR = ROWS / 2
        val midC = COLS / 2

        currentBoard = inicialGame.board - (midR, midC) - (midR, midC - 1) //Aqui é onde tiramos as peças mesmo do tabuleiro, tiramos a peça do meio, e a à direita da do meio
        currentOpenCoords = List((midR, midC), (midR, midC - 1))//atualiza a lista de posições livres para ser as posições do meio de onde retirámos as peças
        currentPlayer = Stone.Black //começa as peças pretas
        selectedCoord = None //ao ínicio nenhuma peça está selecionada

        renderBoard() //chama a função que "desenha" o visual do tabuleiro
        lblStatus.setText("Jogo iniciado! Vez das Pretas.")
    }

    // Função que atualiza o lado visual
    def renderBoard(): Unit = {
        cells.foreach { case (coord, pane) =>
            pane.getChildren.clear() //retiro qualquer coisa que possa estar naquele painel ao ínicio do jogo
            pane.setStyle("-fx-border-color: #cccccc;") // cira uma borda subtil para vermos as grelhas

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
    def handleCellClick(event: MouseEvent): Unit = { //função definidina no scene builder para o que acontece ao carregar no botões
        val source = event.getSource.asInstanceOf[Pane]
        val id = source.getId
        // extrai as coordenadas clicadas
        val r = id.charAt(4).asDigit
        val c = id.charAt(5).asDigit
        val clickedCoord = (r, c)

        selectedCoord match { //ao carregar na peça de origem é o que a torna vede no fundo
            case None => //caso não exista nenhuma peça selecionada de origem
                currentBoard.get(clickedCoord) match { //vamos pegar na célula em que o jogador carregou
                    case Some(stone) if stone == currentPlayer => //se for do jogador atual
                        selectedCoord = Some(clickedCoord) // a selected coord passa a ser a peça em que se carregou
                        source.setStyle("-fx-background-color: rgba(0, 255, 0, 0.4); -fx-border-color: #cccccc;")//colocamos o fundo a verde
                        lblStatus.setText(s"Peça $clickedCoord selecionada. Escolhe o destino.") //coloca esta mensagem no painel de jogo

                    case Some(_) => //caso a peça que selecionada para jogar seja do adversário, enviamos uma mensagem a avisar o jogador
                        lblStatus.setText("Esta peça é do teu adversário!")

                    case None => //caso o jogador selecione um painel que não contenha uma peça, enviamos uma mensagem avisar disso
                        lblStatus.setText("Casa vazia! Escolhe uma peça tua para mover.")
                }

            case Some(fromCoord) => //caso esteja a carregar numa coordenada de origem que já estava selecionada como coordenada de origem
                if (clickedCoord == fromCoord) { //caso a coordenada carregada coreresponda a uma coordenada que já foi definida como a de origem
                    selectedCoord = None //retiramos esta peça como sendo a peça de origem, ou seja, carregar uma vez, a posição passa a ser a de origem, carrego uma segunda vez na mesma posição cancelo aquela posição como posição de origem
                    renderBoard()//desenha novamente o board, para o fundo deixar de estar verde
                    lblStatus.setText(if (currentPlayer == Stone.Black) "Vez das Pretas." else "Vez das Brancas.")//manda uma mensagem ao jogador para saber quem é a peça atual a ser jogada
                } else { // se carreguei numa outra coordenada, posso fazer  a jogada
                    val (optBoard, newOpenCoords) = Logic.play(
                        currentBoard, currentPlayer, fromCoord, clickedCoord, currentOpenCoords //chamo a lógica do play feita na primeira parte do trabaho
                    )
                    optBoard match {
                        case Some(newBoard) => //caso a jogada tenha sido válida
                            currentBoard = newBoard //criamos um novo tabuleiro, para retirar as peças da posição em que estavam
                            currentOpenCoords = newOpenCoords //atualiza a lista de posições vazias
                            currentPlayer = Game.opponent(currentPlayer)//muda de jogador usando a função opponent que já estava feita no ficheiro Game da primeira parte do trabalho
                            selectedCoord = None //meto a coordenada selecionada a None
                            renderBoard() //desenho o tabuleiro
                            val nomeJogador = if (currentPlayer == Stone.Black) "Pretas" else "Brancas"
                            lblStatus.setText(s"Jogada válida! Vez das $nomeJogador.") //envio uma mensagem para o jogador a informar de quem é a vez

                        case None => //caso o play não ocorra com sucesso (logo não devolve nenhum tabuleiro)
                            lblStatus.setText("Salto inválido! Escolhe novamente.") //enviamos uma mensagem de erro para o jogador
                            selectedCoord = None //metemos a peça selecionada a None
                            renderBoard() // limpo o verde da seleção
                    }
                }
        }
    }
}