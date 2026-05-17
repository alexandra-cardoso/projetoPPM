import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

class HelloWorld extends Application {
    override def start(primaryStage: Stage): Unit = {
        // IMPORTANTE: Agora carregamos primeiro o menu.fxml
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
        Application.launch(classOf[HelloWorld], args: _*)
    }
}