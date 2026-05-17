import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage
import javafx.event.ActionEvent
import javafx.application.Platform
import javafx.scene.control.{Label, RadioButton, ToggleGroup}

// Enum simples de dificuldade — fica aqui ou num ficheiro próprio
enum Difficulty:
    case Facil, Medio, Dificil

class MenuController {

    @FXML var rbFacil:   RadioButton = _
    @FXML var rbMedio:   RadioButton = _
    @FXML var rbDificil: RadioButton = _
    @FXML var lblExplicacao: Label = _

    private val group = new ToggleGroup()

    @FXML
    def initialize(): Unit = {
        rbFacil.setToggleGroup(group)
        rbMedio.setToggleGroup(group)
        rbDificil.setToggleGroup(group)
        rbFacil.setSelected(true)
    }

    @FXML def selecionarFacil(e: ActionEvent): Unit  = ()
    @FXML def selecionarMedio(e: ActionEvent): Unit  = ()
    @FXML def selecionarDificil(e: ActionEvent): Unit = ()

    @FXML def explicarFacil(e: ActionEvent): Unit =
        lblExplicacao.setText("Fácil: 90 segundos por jogada. As casas disponíveis ficam destacadas a vermelho.")

    @FXML def explicarMedio(e: ActionEvent): Unit =
        lblExplicacao.setText("Médio: 30 segundos por jogada. As casas disponíveis ainda ficam destacadas.")

    @FXML def explicarDificil(e: ActionEvent): Unit =
        lblExplicacao.setText("Difícil: 30 segundos por jogada. Sem destaques — tens de encontrar as jogadas sozinho!")

    @FXML
    def handleSim(e: ActionEvent): Unit = {
        val diff = if rbFacil.isSelected then Difficulty.Facil
        else if rbMedio.isSelected then Difficulty.Medio
        else Difficulty.Dificil

        val stage = e.getSource.asInstanceOf[javafx.scene.Node].getScene.getWindow.asInstanceOf[Stage]
        val loader = new FXMLLoader(getClass.getResource("/Controller.fxml"))
        val root: Parent = loader.load()

        // Passa a dificuldade ao Controller ANTES de mostrar a cena
        val ctrl = loader.getController[Controller]()
        ctrl.setDifficulty(diff)

        stage.getScene.setRoot(root)
        stage.sizeToScene()
        stage.setTitle("Kōnane - Jogo em Curso")
    }

    @FXML
    def handleNao(e: ActionEvent): Unit = {
        Platform.exit()
        System.exit(0)
    }
}