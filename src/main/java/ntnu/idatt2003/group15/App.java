package ntnu.idatt2003.group15;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ntnu.idatt2003.group15.view.MainMenu;

import java.util.Objects;

/**
 * The main entry point for the Millions stock simulation application.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        StackPane root =  new StackPane();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style/MainMenuStyle.css").toExternalForm());
        MainMenu mainMenu = new MainMenu();
        root.getChildren().add(mainMenu.getView());
        stage.setWidth(900);
        stage.setHeight(700);
        stage.setScene(scene);
        stage.show();
    }
}
