package ntnu.idatt2003.group15;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ntnu.idatt2003.group15.utilities.TaskUtil;
import ntnu.idatt2003.group15.view.MainMenu;

import java.util.Objects;

/**
 * The main entry point for the Millions stock simulation application.
 */
public class App extends Application {

    TaskUtil taskUtil;

    @Override
    public void start(Stage stage) throws Exception {

        setUpDependencies();

        StackPane root =  new StackPane();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/MainMenuStyle.css")).toExternalForm());
        MainMenu mainMenu = new MainMenu(root);
        root.getChildren().add(mainMenu.getView());
        root.getStyleClass().add("scene-root");
        stage.setFullScreen(true);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (taskUtil != null) {
            taskUtil.shutdown();
        }
    }

    /**
     * This method is called by the Launcher class.
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void setUpDependencies() {
        taskUtil = new TaskUtil();
        try {
            taskUtil.init(Runtime.getRuntime().availableProcessors());
        } catch (IllegalArgumentException e) {
            exceptionPopUp("Error loading dependencies",
                "the program will still work, but can feel laggier");
        }
    }

    private void exceptionPopUp(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
