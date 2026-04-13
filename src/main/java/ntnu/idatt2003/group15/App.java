package ntnu.idatt2003.group15;

import com.sun.tools.javac.Main;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import ntnu.idatt2003.group15.controller.MainController;
import ntnu.idatt2003.group15.utilities.CsvUtil;
import ntnu.idatt2003.group15.utilities.TaskUtil;
import ntnu.idatt2003.group15.view.ExceptionDialog;
import ntnu.idatt2003.group15.view.MainMenu;
import ntnu.idatt2003.group15.view.NewsDialog;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * The main entry point for the Millions stock simulation application.
 */
public class App extends Application {

    private TaskUtil taskUtil;
    private Consumer<Throwable> errorHandler;
    private CsvUtil csvUtil;

    @Override
    public void start(Stage stage) {

        setUpDependencies();

        StackPane root =  new StackPane();
        Scene scene = new Scene(root);

        MainController mainController = new MainController(root, errorHandler, csvUtil, taskUtil);
        mainController.showMainMenu();

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/MainMenuStyle.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style/DialogStyle.css")).toExternalForm());
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
        // infrastructure
        taskUtil = new TaskUtil();
        try {
            taskUtil.init(Runtime.getRuntime().availableProcessors());
        } catch (IllegalArgumentException e) {
            exceptionPopUp(e);
        }
        csvUtil = new CsvUtil();
        errorHandler = this::exceptionPopUp;
    }

    private void exceptionPopUp(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception :(");
        alert.setHeaderText(null);
        String message = e.getMessage();
        alert.setContentText(message == null || message.isBlank()
            ? "An unexpected error occurred."
            : message);
        alert.showAndWait();
    }

}
