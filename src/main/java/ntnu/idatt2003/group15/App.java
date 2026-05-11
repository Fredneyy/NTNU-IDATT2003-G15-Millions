package ntnu.idatt2003.group15;

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
import ntnu.idatt2003.group15.view.SettingsView;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * The main entry point for the Millions stock simulation application.
 */
public class App extends Application {

    private TaskUtil taskUtil;
    private Consumer<Throwable> errorHandler;
    private CsvUtil csvUtil;
    private ExceptionDialog exceptionDialog;
    private StackPane root;

    @Override
    public void start(Stage stage) {
        setUpDependencies();

        root =  new StackPane();
        Scene scene = new Scene(root);

        MainController mainController = new MainController(root, errorHandler, csvUtil, taskUtil);
        mainController.showMainMenu();

        // SettingsView settingsView = new SettingsView(root);
        // root.getChildren().setAll(settingsView.getView());

        // exceptionPopUp(new NullPointerException("Shit"));
        scene.getStylesheets().add(getClass().getResource("/style/SettingsStyle.css").toExternalForm());
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
        exceptionDialog = new ExceptionDialog(Duration.millis(200), 2.0);
    }

    private void exceptionPopUp(Throwable e) {
        exceptionDialog.show(root, e.getClass().getSimpleName(), e.getMessage());
    }

}
