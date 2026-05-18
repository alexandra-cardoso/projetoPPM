import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

class InitialPage extends Application { //é a classe que permite mostrar o ecrã inicial onde o jogador eexolhe se quer jogar e a dificuldade com que quer jogar
    override def start(primaryStage: Stage): Unit = {
        val fxmlLoader = new FXMLLoader(getClass.getResource("/menu.fxml"))
        val menuRoot: Parent = fxmlLoader.load()
        val scene = new Scene(menuRoot)
        primaryStage.setTitle("Kōnane - Bem-vindo!")
        primaryStage.setScene(scene)
        primaryStage.setResizable(false)
        primaryStage.show()
    }
}

object HelloWorld {
    def main(args: Array[String]): Unit = {
        Application.launch(classOf[InitialPage], args: _*)
    }
}