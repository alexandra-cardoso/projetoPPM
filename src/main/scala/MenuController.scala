import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage
import javafx.event.ActionEvent
import javafx.application.Platform

class MenuController {

    @FXML
    def handleSim(event: ActionEvent): Unit = {

        val stage = event.getSource.asInstanceOf[javafx.scene.Node].getScene.getWindow.asInstanceOf[Stage]
        val fxmlLoader = new FXMLLoader(getClass.getResource("/Controller.fxml"))
        val root: Parent = fxmlLoader.load()
        stage.getScene.setRoot(root)
        stage.setTitle("Kōnane - Jogo em Curso")
    }

    @FXML
    def handleNao(event: ActionEvent): Unit = {

        Platform.exit()
        System.exit(0)
    }
}