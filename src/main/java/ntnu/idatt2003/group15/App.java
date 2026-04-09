package ntnu.idatt2003.group15;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * The main entry point for the Millions stock simulation application.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        StackPane root =  new StackPane();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
