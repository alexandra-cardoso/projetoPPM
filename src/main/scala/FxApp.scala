import javafx.application.Application

object FxApp {
    def main(args: Array[String]): Unit = {
        // Isto liga o motor do JavaFX e abre a tua classe HelloWorld
        Application.launch(classOf[HelloWorld], args: _*)
    }
}